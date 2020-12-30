/**
 * Copyright © 2019-2020 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.xpages.runtime.interpreter;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;

import org.openntf.xpages.runtime.xsp.JakartaXspSourceClassLoader;
import org.openntf.xpages.runtime.xsp.LibraryWeightComparator;
import org.w3c.dom.Document;

import com.ibm.commons.extension.ExtensionManager;
import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;
import com.ibm.designer.runtime.domino.adapter.util.PageNotFoundException;
import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.extlib.interpreter.DynamicXPageBean;
import com.ibm.xsp.library.ClasspathResourceBundleSource;
import com.ibm.xsp.library.FacesClassLoader;
import com.ibm.xsp.library.LibraryServiceLoader;
import com.ibm.xsp.library.LibraryWrapper;
import com.ibm.xsp.library.XspLibrary;
import com.ibm.xsp.page.FacesPageDispatcher;
import com.ibm.xsp.page.FacesPageDriver;
import com.ibm.xsp.page.FacesPageException;
import com.ibm.xsp.page.compiled.AbstractCompiledPageDispatcher;
import com.ibm.xsp.page.compiled.DefaultPageErrorHandler;
import com.ibm.xsp.page.compiled.DispatcherParameter;
import com.ibm.xsp.page.compiled.PageToClassNameUtil;
import com.ibm.xsp.registry.FacesProjectImpl;
import com.ibm.xsp.registry.FacesSharableRegistry;
import com.ibm.xsp.registry.SharableRegistryImpl;
import com.ibm.xsp.registry.config.FacesClassLoaderFactory;
import com.ibm.xsp.registry.config.IconUrlSource;
import com.ibm.xsp.registry.config.ResourceBundleSource;
import com.ibm.xsp.registry.config.SimpleRegistryProvider;
import com.ibm.xsp.registry.config.XspRegistryProvider;
import com.ibm.xsp.registry.parse.ConfigParser;
import com.ibm.xsp.registry.parse.ConfigParserFactory;

@SuppressWarnings("nls")
public class DynamicPageDriver implements FacesPageDriver {
	private static class PageHolder {
		private final long modified;
		private final Class<? extends AbstractCompiledPageDispatcher> page;
		
		public PageHolder(long modified, Class<? extends AbstractCompiledPageDispatcher> page) {
			this.modified = modified;
			this.page = page;
		}
	}
	
	private static final Logger log = Logger.getLogger(DynamicPageDriver.class.getName());
	private static final DefaultPageErrorHandler s_errorHandler = new DefaultPageErrorHandler();
	
	private final DynamicXPageBean dynamicXPageBean = new DynamicXPageBean() {
		protected JakartaXspSourceClassLoader createJavaSourceClassLoader() {
			return new JakartaXspSourceClassLoader(Thread.currentThread().getContextClassLoader(), Collections.emptyList(), new String[0]);
		}
	};
	private final Map<String, PageHolder> pages = new HashMap<>();
	private static boolean initialized;

	@SuppressWarnings("unchecked")
	@Override
	public FacesPageDispatcher loadPage(FacesContext context, String pageName) throws FacesPageException {
		if(!initialized) {
			this.registerCustomControls();
			this.initLibrary();
			initialized = true;
		}
		
		try {
			// Check if it's compiled as a class in the current loader
			if(!pages.containsKey(pageName)) {
				try {
					String className = PageToClassNameUtil.getClassNameForPage(pageName);
					Class<? extends AbstractCompiledPageDispatcher> existing = (Class<? extends AbstractCompiledPageDispatcher>) Class.forName(className);
					URL loc = existing.getProtectionDomain().getCodeSource().getLocation();
					String classPath = className.replace('.', '/') + ".class"; //$NON-NLS-1$
					loc = new URL(loc, classPath);
					URLConnection classConn = loc.openConnection();
					long classMod = classConn.getLastModified();
					pages.put(pageName, new PageHolder(classMod, existing));
				} catch(ClassNotFoundException e) {
					// That's fine - move along to load from source
				}
			}
			
			// See if there's a local resource version as well
			URL url = findResource(pageName);

			if(!pages.containsKey(pageName) && url == null) {
				// Then we have no fallback
				throw new PageNotFoundException(format("Unable to find XPage or Custom Control {0}; check WEB-INF/xpages and WEB-INF/controls", pageName));
			}
			

			// See if we need to invalidate an existing compiled version
			if (url != null) {
				URLConnection conn = url.openConnection();
				long mod = conn.getLastModified();
				if (pages.containsKey(pageName)) {
					PageHolder holder = pages.get(pageName);
					if (mod > holder.modified) {
						// Then invalidate
						if (log.isLoggable(Level.INFO)) {
							log.info(format("Page {0} has been modified; recompiling", pageName));
						}
						pages.remove(pageName);
						dynamicXPageBean.purgeCompiledPage(pageName);
					}
				}

				// Compile from source if needed
				pages.computeIfAbsent(pageName, key -> {
					if(log.isLoggable(Level.INFO)) {
						log.info(format("Looking for page {0}", pageName));
					}
					FacesSharableRegistry registry = ApplicationEx.getInstance().getRegistry();
					
					String xspSource;
					try(InputStream is = conn.getInputStream()) {
						xspSource = StreamUtil.readString(is);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					
					try {
						// TODO In JDK >= 9, it may be possible to do this with the REPL infrastructure
						Class<? extends AbstractCompiledPageDispatcher> compiled = (Class<? extends AbstractCompiledPageDispatcher>)dynamicXPageBean.compile(pageName, xspSource, registry);
						return new PageHolder(mod, compiled);
					} catch (RuntimeException e) {
						throw e;
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
			}
			
			AbstractCompiledPageDispatcher page = pages.get(pageName).page.newInstance();
			page.init(new DispatcherParameter(this, pageName, s_errorHandler));
			return page;
		} catch (IOException | InstantiationException | IllegalAccessException e) {
			throw new FacesPageException(e);
		}
	}
	
	private URL findResource(String pageName) {
		// TODO cache the URLs
		// TODO consider allowing XPages in the root of the app
		String path = PathUtil.concat("/WEB-INF/xpages", pageName, '/'); //$NON-NLS-1$
		URL is = Thread.currentThread().getContextClassLoader().getResource(path);
		if(is == null) {
			path = PathUtil.concat("/WEB-INF/controls", pageName, '/'); //$NON-NLS-1$
			is = Thread.currentThread().getContextClassLoader().getResource(path);
		}
		return is;
	}
	
	private final IconUrlSource iconUrlSource = iconName -> Thread.currentThread().getContextClassLoader().getResource(iconName);
	private final ResourceBundleSource resourceBundleSource = new ClasspathResourceBundleSource(Thread.currentThread().getContextClassLoader());
	
	private void registerCustomControls() {
		// TODO investigate reloading when resources change
		URL controls = Thread.currentThread().getContextClassLoader().getResource("/WEB-INF/controls"); //$NON-NLS-1$
		if(controls != null) {
			if(log.isLoggable(Level.FINE)) {
				log.fine(format("searching for controls in {0}", controls));
			}
			ConfigParser configParser = ConfigParserFactory.getParserInstance();
			FacesSharableRegistry facesRegistry = ApplicationEx.getInstance().getRegistry();
			FacesProjectImpl facesProject = (FacesProjectImpl)facesRegistry.getLocalProjectList().get(0);
			FacesClassLoader facesClassLoader = FacesClassLoaderFactory.createContext(this.getClass());
			
			switch(StringUtil.toString(controls.getProtocol())) {
			case "file": //$NON-NLS-1$
				try {
					Path path = Paths.get(controls.toURI());
					Files.find(path, 1, (file, attrs) -> file.getFileName().toString().endsWith(".xsp-config"), FileVisitOption.FOLLOW_LINKS) //$NON-NLS-1$
						.forEach(configPath -> {
							try {
								Document xspConfig;
								try(InputStream is = Files.newInputStream(configPath)) {
									xspConfig = DOMUtil.createDocument(is);
								}
								String namespace = StringUtil.trim(DOMUtil.evaluateXPath(xspConfig, "/faces-config/faces-config-extension/namespace-uri/text()").getStringValue()); //$NON-NLS-1$
								configParser.createFacesLibraryFragment(
										facesProject,
										facesClassLoader,
										path.resolve(configPath.getFileName().toString()).toString(),
										xspConfig.getDocumentElement(),
										resourceBundleSource,
										iconUrlSource,
										namespace
								);
							} catch (XMLException | IOException e) {
								throw new RuntimeException(e);
							}
						});
				} catch (IOException | URISyntaxException e) {
					throw new RuntimeException(e);
				}
				break;
			case "jar": //$NON-NLS-1$
			case "wsjar": //$NON-NLS-1$
				// TODO figure out
				//   It may be fair to expect the app to be unpacked at runtime, but maybe there could be
				//   controls in dependencies
				break;
			}
		}
	}

	private void initLibrary() {
		SharableRegistryImpl facesRegistry = (SharableRegistryImpl)ApplicationEx.getInstance().getRegistry();
		Set<String> existingLibIds = facesRegistry.getDepends().stream()
			.parallel()
			.map(lib -> lib.getId())
			.collect(Collectors.toSet());
		Set<String> desired = new HashSet<>(Arrays.asList(StringUtil.splitString(ApplicationEx.getInstance().getProperty("xsp.library.depends", ""), ','))); //$NON-NLS-1$ //$NON-NLS-2$
		List<Object> libraries = ExtensionManager.findServices((List<Object>)null, LibraryServiceLoader.class, "com.ibm.xsp.Library"); //$NON-NLS-1$
		libraries.stream()
			.filter(lib -> lib instanceof XspLibrary)
			.map(XspLibrary.class::cast)
			.filter(lib -> !existingLibIds.contains(lib.getLibraryId()))
			.filter(lib -> lib.isGlobalScope() || desired.contains(lib.getLibraryId()))
			.sorted(LibraryWeightComparator.INSTANCE)
			.map(lib -> new LibraryWrapper(lib.getLibraryId(), lib))
			.map(wrapper -> {
				SimpleRegistryProvider provider = new SimpleRegistryProvider();
				provider.init(wrapper);
				return provider;
			})
			.map(XspRegistryProvider::getRegistry)
			.forEach(facesRegistry::addDepend);
		facesRegistry.refreshReferences();
	}
}
