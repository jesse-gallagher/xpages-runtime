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
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;

import org.openntf.xpages.runtime.xsp.LibraryWeightComparator;
import org.w3c.dom.Document;

import com.ibm.commons.extension.ExtensionManager;
import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;
import com.ibm.designer.runtime.domino.adapter.util.PageNotFoundException;
import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.extlib.interpreter.interpreter.Control;
import com.ibm.xsp.extlib.interpreter.interpreter.ControlFactory;
import com.ibm.xsp.extlib.interpreter.interpreter.XPagesInterpreter;
import com.ibm.xsp.extlib.interpreter.interpreter.parser.DefaultXPagesCache;
import com.ibm.xsp.extlib.interpreter.interpreter.parser.XPagesCache;
import com.ibm.xsp.extlib.interpreter.interpreter.parser.XPagesLoader;
import com.ibm.xsp.library.ClasspathResourceBundleSource;
import com.ibm.xsp.library.FacesClassLoader;
import com.ibm.xsp.library.LibraryServiceLoader;
import com.ibm.xsp.library.LibraryWrapper;
import com.ibm.xsp.library.XspLibrary;
import com.ibm.xsp.page.FacesPageDispatcher;
import com.ibm.xsp.page.FacesPageDriver;
import com.ibm.xsp.page.FacesPageException;
import com.ibm.xsp.page.compiled.DefaultPageErrorHandler;
import com.ibm.xsp.page.compiled.DispatcherParameter;
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

public class DynamicPageDriver implements FacesPageDriver {
	private static final Logger log = Logger.getLogger(DynamicPageDriver.class.getName());
	private static boolean initialized;
	private static final DefaultPageErrorHandler s_errorHandler = new DefaultPageErrorHandler();
			
	private XPagesInterpreter interpreter;
	
	public DynamicPageDriver() {
		
	}

	@Override
	public FacesPageDispatcher loadPage(FacesContext context, String pageName) throws FacesPageException {
		if(!initialized) {
			this.registerCustomControls();
			this.initLibrary();
			XPagesCache pageCache = new DefaultXPagesCache(getClass().getName(), 2048);
			XPagesLoader pageLoader = new URLConnectionPageLoader();
			ControlFactory controlFactory = new InterpretedControlFactory();
			this.interpreter = new XPagesInterpreter(
				null,
				controlFactory,
				null,
				pageLoader,
				pageCache
			);
			initialized = true;
		}
		URL url = findResource(pageName);
		if(url == null) {
			throw new PageNotFoundException(format("Unable to find XPage or Custom Control {0}; check WEB-INF/xpages and WEB-INF/controls", pageName));
		}
		
		Control c = interpreter.parseUri(url.toString());
		
		InterpretedPageDispatcher dispatcher = new InterpretedPageDispatcher(c);
		dispatcher.init(new DispatcherParameter(this, pageName, s_errorHandler));
		return dispatcher;
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
	
	private final IconUrlSource iconUrlSource = new IconUrlSource() {
		@Override public URL getIconUrl(String arg0) {
			// TODO ???
			return null;
		}
	};
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
				// TODO figure out, and maybe account for "wsjar"
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
