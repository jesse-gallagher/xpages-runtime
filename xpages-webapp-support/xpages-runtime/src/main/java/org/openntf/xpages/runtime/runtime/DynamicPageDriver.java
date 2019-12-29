package org.openntf.xpages.runtime.runtime;

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

import groovy.lang.GroovyClassLoader;

public class DynamicPageDriver implements FacesPageDriver {
	private static class PageHolder {
		private final long modified;
		private final FacesPageDispatcher page;
		
		public PageHolder(long modified, FacesPageDispatcher page) {
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
	private final GroovyClassLoader groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());
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

		// TODO consider a precompile process at app load
		URL url = findResource(pageName);
		if(url == null) {
			throw new PageNotFoundException("Unable to find XPage or Custom Control " + pageName + "; check WEB-INF/xpages and WEB-INF/controls");
		}
		
		try {
			URLConnection conn = url.openConnection();
			long mod = conn.getLastModified();
			
			// Check if it's already parsed
			PageHolder holder = pages.get(pageName);
			if(holder != null) {
				// Check for modifications
				if(mod > holder.modified) {
					// Then invalidate
					if(log.isLoggable(Level.INFO)) {
						log.info(StringUtil.format("Page {0} has been modified; recompiling", pageName));
					}
					pages.remove(pageName);
				}
			}
			return pages.computeIfAbsent(pageName, key -> {
				if(log.isLoggable(Level.FINE)) {
					log.fine("Looking for page " + pageName);
				}
				FacesSharableRegistry registry = ApplicationEx.getInstance().getRegistry();
				
				String xspSource;
				try(InputStream is = url.openStream()) {
					xspSource = StreamUtil.readString(is);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				String className = PageToClassNameUtil.getClassNameForPage(pageName);
				
				try {
					// TODO investigate using the Bazaar's interpreter instead of compilation
					String javaSource = dynamicXPageBean.translate(className, pageName, xspSource, registry);
					String groovySource = cleanSourceForGroovy(javaSource);
					// In JDK >= 9, it may be possible to do this with the REPL infrastructure
					Class<? extends AbstractCompiledPageDispatcher> compiled = groovyClassLoader.parseClass(groovySource);
					AbstractCompiledPageDispatcher page = compiled.newInstance();
					page.init(new DispatcherParameter(this, pageName, s_errorHandler));
					return new PageHolder(mod, page);
				} catch (RuntimeException e) {
					throw e;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}).page;
		} catch (IOException e) {
			throw new FacesPageException(e);
		}
	}
	
	private String cleanSourceForGroovy(String javaSource) {
		return javaSource.replace("$", "\\$") // Escape $ for GStrings
				.replace("ComponentInfo", "com.ibm.xsp.page.compiled.AbstractCompiledPage.ComponentInfo") // Groovy needs to be told about inner classes
				.replace(" {\"", " new String[] {\""); // String[][] initializers
	}
	
	private URL findResource(String pageName) {
		// TODO cache the URLs
		// TODO consider allowing XPages in the root of the app
		String path = PathUtil.concat("/WEB-INF/xpages", pageName, '/');
		URL is = Thread.currentThread().getContextClassLoader().getResource(path);
		if(is == null) {
			path = PathUtil.concat("/WEB-INF/controls", pageName, '/');
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
		URL controls = Thread.currentThread().getContextClassLoader().getResource("/WEB-INF/controls");
		if(controls != null) {
			if(log.isLoggable(Level.FINE)) {
				log.fine("searching for controls in " + controls);
			}
			ConfigParser configParser = ConfigParserFactory.getParserInstance();
			FacesSharableRegistry facesRegistry = ApplicationEx.getInstance().getRegistry();
			FacesProjectImpl facesProject = (FacesProjectImpl)facesRegistry.getLocalProjectList().get(0);
			FacesClassLoader facesClassLoader = FacesClassLoaderFactory.createContext(this.getClass());
			
			switch(StringUtil.toString(controls.getProtocol())) {
			case "file":
				try {
					Path path = Paths.get(controls.toURI());
					Files.find(path, 1, (file, attrs) -> file.getFileName().toString().endsWith(".xsp-config"), FileVisitOption.FOLLOW_LINKS)
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
			case "jar":
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
		Set<String> desired = new HashSet<>(Arrays.asList(StringUtil.splitString(ApplicationEx.getInstance().getProperty("xsp.library.depends", ""), ',')));
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
