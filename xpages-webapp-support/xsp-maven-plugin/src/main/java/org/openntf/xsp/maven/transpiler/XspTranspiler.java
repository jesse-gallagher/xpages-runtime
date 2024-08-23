/*
 * Copyright © 2020-2024 Jesse Gallagher
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
package org.openntf.xsp.maven.transpiler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.openntf.xpages.runtime.xsp.LibraryWeightComparator;
import org.w3c.dom.Document;

import com.ibm.commons.Platform;
import com.ibm.commons.extension.ExtensionManager;
import com.ibm.commons.platform.GenericPlatform;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;
import com.ibm.xsp.extlib.interpreter.DynamicFacesClassLoader;
import com.ibm.xsp.extlib.interpreter.DynamicXPageBean;
import com.ibm.xsp.extlib.javacompiler.JavaSourceClassLoader;
import com.ibm.xsp.library.ClasspathResourceBundleSource;
import com.ibm.xsp.library.FacesClassLoader;
import com.ibm.xsp.library.LibraryServiceLoader;
import com.ibm.xsp.library.LibraryWrapper;
import com.ibm.xsp.library.XspLibrary;
import com.ibm.xsp.page.compiled.PageToClassNameUtil;
import com.ibm.xsp.registry.CompositeComponentDefinitionImpl;
import com.ibm.xsp.registry.FacesLibraryImpl;
import com.ibm.xsp.registry.FacesProject;
import com.ibm.xsp.registry.FacesProjectImpl;
import com.ibm.xsp.registry.LibraryFragmentImpl;
import com.ibm.xsp.registry.SharableRegistryImpl;
import com.ibm.xsp.registry.UpdatableLibrary;
import com.ibm.xsp.registry.config.IconUrlSource;
import com.ibm.xsp.registry.config.ResourceBundleSource;
import com.ibm.xsp.registry.config.SimpleRegistryProvider;
import com.ibm.xsp.registry.parse.ConfigParser;
import com.ibm.xsp.registry.parse.ConfigParserFactory;
import com.ibm.xsp.registry.parse.ConfigParserImpl;

public class XspTranspiler {
	
	private final boolean debug;
	protected final SharableRegistryImpl facesRegistry = new SharableRegistryImpl(getClass().getName());
	protected final FacesProject facesProject;
	protected final DynamicXPageBean dynamicXPageBean = new DynamicXPageBean();
	protected ResourceBundleSource resourceBundleSource;
	protected final IconUrlSource iconUrlSource = icon -> getClass().getResource(icon);

	public XspTranspiler(boolean debug) {
		this.debug = debug;
		this.facesProject = new FacesProjectImpl(getClass().getName(), facesRegistry);
		
		// Ensure that IBM Commons's platform detection doesn't try to load an implementation that depends on a Notes runtime
		System.setProperty(Platform.PLATFORM_PROPERTY_KEY, GenericPlatform.class.getName());
		
		resourceBundleSource = new ClasspathResourceBundleSource(Thread.currentThread().getContextClassLoader());

		initializeRegistry();
	}
	
	public void defineCustomControls(Path ccSourceRoot) throws IOException {
		if(ccSourceRoot != null) {
			if(debug) {
				System.out.println("Initializing Custom Control definitions"); //$NON-NLS-1$
			}
			
			// Generate a classpath, which the CC library needs to find classes for property types
			Set<Path> cleanup = new HashSet<>();
			try {
				ConfigParser configParser = getConfigParser();
				
				try(JavaSourceClassLoader cl = new JavaSourceClassLoader(Thread.currentThread().getContextClassLoader(), Collections.emptyList(), null)) {
					FacesClassLoader facesClassLoader = new DynamicFacesClassLoader(dynamicXPageBean, cl);
					
					try(Stream<Path> ccConfigs = Files.find(ccSourceRoot, Integer.MAX_VALUE, (path, attr) -> attr.isRegularFile() && path.toString().toLowerCase().endsWith(".xsp-config"), FileVisitOption.FOLLOW_LINKS)) { //$NON-NLS-1$
						ccConfigs.forEach(ccConfig -> {
							Document xspConfig = TranspilerUtil.readXml(ccConfig);
							
							String namespace = StringUtil.trim(TranspilerDomUtil.node(xspConfig, "/faces-config/faces-config-extension/namespace-uri/text()").get().getNodeValue()); //$NON-NLS-1$
							Path fileName = ccSourceRoot.relativize(ccConfig);
							LibraryFragmentImpl fragment = (LibraryFragmentImpl)configParser.createFacesLibraryFragment(
									facesProject,
									facesClassLoader,
									fileName.toString(),
									xspConfig.getDocumentElement(),
									resourceBundleSource,
									iconUrlSource,
									namespace
							);
							UpdatableLibrary library = getLibrary(namespace);
							library.addLibraryFragment(fragment);
							
							// Load the definition to refresh its parent ref
							String controlName = StringUtil.trim(TranspilerDomUtil.node(xspConfig, "/faces-config/composite-component/composite-name/text()").get().getNodeValue()); //$NON-NLS-1$
							CompositeComponentDefinitionImpl def = (CompositeComponentDefinitionImpl)library.getDefinition(controlName);
							def.refreshReferences();
						});
					}
				}
			} finally {
				TranspilerUtil.deltree(cleanup);
			}
		}
	}
	
	private ConfigParser getConfigParser() {
		ConfigParser parser = new ConfigParserImpl();
		// Stash this in ConfigParserFactory for reuse. Using the static getter falls down into invalid Eclipse land
		try {
			Field s_parserInstance = ConfigParserFactory.class.getDeclaredField("s_parserInstance"); //$NON-NLS-1$
			s_parserInstance.setAccessible(true);
			s_parserInstance.set(null, parser);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		return parser;
	}

	private void initializeRegistry() {
		List<Object> libraries = ExtensionManager.findServices((List<Object>)null, LibraryServiceLoader.class, "com.ibm.xsp.Library"); //$NON-NLS-1$
		libraries.stream()
			.filter(lib -> lib instanceof XspLibrary)
			.map(lib -> (XspLibrary)lib)
			.peek(lib -> {
				if(debug) {
					System.out.println(MessageFormat.format("Adding XSP library: {0}", lib)); //$NON-NLS-1$
				}
			})
			.sorted(LibraryWeightComparator.INSTANCE)
			.map(lib -> new LibraryWrapper(lib.getLibraryId(), lib))
			.map(wrapper -> {
				SimpleRegistryProvider provider = new SimpleRegistryProvider();
				provider.init(wrapper);
				return provider;
			})
			.map(provider -> provider.getRegistry())
			.forEach(reg -> facesRegistry.addDepend(reg));
		facesRegistry.refreshReferences();
	}
	
	protected UpdatableLibrary getLibrary(String namespace) {
		UpdatableLibrary library = (UpdatableLibrary)facesRegistry.getLocalLibrary(namespace);
		if(library == null) {
			try {
				library = new FacesLibraryImpl(facesRegistry, namespace);
				// TODO this is probably properly done by creating a FacesProjectImpl
				// - it can then register the library fragments itself
				Field localLibsField = facesRegistry.getClass().getDeclaredField("_localLibs"); //$NON-NLS-1$
				localLibsField.setAccessible(true);
				@SuppressWarnings("unchecked")
				Map<String, UpdatableLibrary> localLibs = (Map<String, UpdatableLibrary>)localLibsField.get(facesRegistry);
				localLibs.put(namespace, library);
			} catch(NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return library;
	}
	
	/**
	 * @since 1.2.0
	 */
	private static final String[] OVERRIDE_METHOD_SIGNATURES = {
		"public int getComponentForId(String id)", //$NON-NLS-1$
		"public UIComponent createComponent(int id, FacesContext context,", //$NON-NLS-1$
		"protected void initIncluderAsRoot(FacesContext context,", //$NON-NLS-1$
		"protected AbstractCompiledPage createPage(int pageIndex)", //$NON-NLS-1$
		"protected String[][] getLibraryTagVersions()" //$NON-NLS-1$
	};
	
	public Map<Path, String> transpile(Path rootDir, Path xspFile) {
		try {
			String xspSource;
			try(InputStream is = Files.newInputStream(xspFile)) {
				xspSource = StreamUtil.readString(is);
			}
			
			Path relativeFile = rootDir.relativize(xspFile);
			String className = PageToClassNameUtil.getClassNameForPage(relativeFile.toString());
		
			String javaSource = dynamicXPageBean.translate(className, relativeFile.toString(), xspSource, facesRegistry);
			
			// Apply some fixes for pre-1.5-style source generation
			for(String methodSignature : OVERRIDE_METHOD_SIGNATURES) {
				javaSource = javaSource.replace(methodSignature, "@Override " + methodSignature); //$NON-NLS-1$
			}
			
			String outputFileName = className.replace('.', File.separatorChar) + ".java"; //$NON-NLS-1$
			Path outputFile = Paths.get(outputFileName);
			return Collections.singletonMap(outputFile, javaSource);
		} catch(Exception e) {
			throw new RuntimeException(MessageFormat.format("Exception processing page {0}", rootDir.relativize(xspFile)), e); //$NON-NLS-1$
		}
	}
}
