/*
 * Copyright © 2020-2022 Jesse Gallagher
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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.Scanner;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Transpiles XSP source to Java.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
@Mojo(
	name="transpile",
	defaultPhase=LifecyclePhase.GENERATE_SOURCES,
	requiresDependencyResolution=ResolutionScope.COMPILE_PLUS_RUNTIME,
	requiresDependencyCollection=ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class TranspileXspMojo extends AbstractMojo {
	@Parameter(defaultValue="${project}", readonly=true, required=false)
	protected MavenProject project;

	@Component
	protected WagonManager wagonManager;
	
	@Parameter(defaultValue="${plugin}", readonly=true)
	protected PluginDescriptor pluginDescriptor;
	
	@Parameter(defaultValue="${session}", readonly=true)
	protected MavenSession mavenSession;
	
	/**
	 * The root directory to search for XSP files.
	 */
	@Parameter(required=false, defaultValue="${project.basedir}/src/main/webapp/WEB-INF/xpages")
	private File xspSourceRoot;
	
	/**
	 * The root directory to search for XSP files.
	 */
	@Parameter(required=false, defaultValue="${project.basedir}/src/main/webapp/WEB-INF/controls")
	private File ccSourceRoot;
	
	@Parameter(required=true, defaultValue="${project.build.directory}/generated-sources/java")
	private File outputDirectory;
	
	@Component
	private BuildContext buildContext;

	private Log log;
	
	public TranspileXspMojo() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		log = getLog();

		Path xspSourceRoot = this.xspSourceRoot == null ? null : this.xspSourceRoot.toPath();
		if(xspSourceRoot != null) {
			if(!Files.exists(xspSourceRoot)) {
				xspSourceRoot = null;
			}
		}
		if(log.isInfoEnabled()) {
			log.info(MessageFormat.format(Messages.getString("TranspileXspMojo.usingXspSourceRoot"), xspSourceRoot)); //$NON-NLS-1$
		}
		
		Path ccSourceRoot = this.ccSourceRoot == null ? null : this.ccSourceRoot.toPath();
		if(ccSourceRoot != null) {
			if(!Files.exists(ccSourceRoot)) {
				ccSourceRoot = null;
			}
		}
		if(log.isInfoEnabled()) {
			log.info(MessageFormat.format(Messages.getString("TranspileXspMojo.usingCcSourceRoot"), ccSourceRoot)); //$NON-NLS-1$
		}
		
		// Check if a build is needed
		List<Path> changedXPages = Collections.emptyList();
		List<Path> deletedXPages = Collections.emptyList();
		if(xspSourceRoot != null) {
			Scanner scanner = buildContext.newScanner(xspSourceRoot.toFile());
			changedXPages = scanXspChanges(scanner, xspSourceRoot);
			
			scanner = buildContext.newDeleteScanner(xspSourceRoot.toFile());
			deletedXPages = scanXspChanges(scanner, xspSourceRoot);
		}
		
		List<Path> changedControls = Collections.emptyList();
		List<Path> deletedControls = Collections.emptyList();
		if(ccSourceRoot != null) {
			Scanner scanner = buildContext.newScanner(ccSourceRoot.toFile());
			changedControls = scanXspChanges(scanner, ccSourceRoot);
			
			scanner = buildContext.newDeleteScanner(ccSourceRoot.toFile());
			deletedControls = scanXspChanges(scanner, ccSourceRoot);
		}

		if(!changedXPages.isEmpty() || !changedControls.isEmpty() || !deletedXPages.isEmpty() || deletedControls.isEmpty()) {
			Path output = outputDirectory.toPath();
			
			Map<Path, String> toWrite = new HashMap<>();
			try(NonVerifyingPathClassLoader projectClassLoader = buildProjectClassLoader()) {
				// Shim TranspilerUtil into this classloader, as it's used in multiple places
				try(InputStream is = getClass().getResourceAsStream("/" + TranspilerUtil.class.getName().replace('.', '/') + ".class")) { //$NON-NLS-1$ //$NON-NLS-2$
					projectClassLoader.defineClass(TranspilerUtil.class.getName(), is);
				}
				
				// Handle deletions first, which may skip having to establish the full environment
				deleteChanges(projectClassLoader, deletedXPages, xspSourceRoot, output);
				deleteChanges(projectClassLoader, deletedControls, ccSourceRoot, output);
				
				// If needed, translate changes
				if(!changedXPages.isEmpty() || !changedControls.isEmpty()) {
					// Load our XspTranspiler class into the contained class loader
					Class<?> transpilerClass;
					try(InputStream is = getClass().getResourceAsStream("/" + XspTranspiler.class.getName().replace('.', '/') + ".class")) { //$NON-NLS-1$ //$NON-NLS-2$
						transpilerClass = projectClassLoader.defineClass(XspTranspiler.class.getName(), is);
					}
					
					ClassLoader tlcc = Thread.currentThread().getContextClassLoader();
					try {
						Thread.currentThread().setContextClassLoader(projectClassLoader);
						
						Object transpiler = transpilerClass.getConstructor(boolean.class).newInstance(log.isDebugEnabled());
						
						if(ccSourceRoot != null && Files.isDirectory(ccSourceRoot)) {
							TranspilerUtil.call(transpiler, "defineCustomControls", new Class<?>[] { Path.class }, ccSourceRoot); //$NON-NLS-1$
						}
						
						Method transpile = transpilerClass.getMethod("transpile", Path.class, Path.class); //$NON-NLS-1$
						
						for(Path xpage : changedXPages) {
							Map.Entry<Path, String> results = ((Map<Path, String>)transpile.invoke(transpiler, xspSourceRoot, xpage)).entrySet().iterator().next();
							Path dest = output.resolve(results.getKey());
							toWrite.put(dest, results.getValue());
						}
						for(Path control : changedControls) {
							Map.Entry<Path, String> results = ((Map<Path, String>)transpile.invoke(transpiler, ccSourceRoot, control)).entrySet().iterator().next();
							Path dest = output.resolve(results.getKey());
							toWrite.put(dest, results.getValue());
						}
					} finally {
						Thread.currentThread().setContextClassLoader(tlcc);
					}
					
					// Write out the files
					for(Map.Entry<Path, String> entry : toWrite.entrySet()) {
						Files.createDirectories(entry.getKey().getParent());
						try(OutputStream os = buildContext.newFileOutputStream(entry.getKey().toFile())) {
							try(Writer w = new OutputStreamWriter(os)) {
								w.write(entry.getValue());
							}
						}
					}
				}
			} catch (Exception e) {
				throw new MojoExecutionException(Messages.getString("TranspileXspMojo.exceptionTranspilingSource"), e); //$NON-NLS-1$
			}
		} else {
			if(log.isInfoEnabled()) {
				log.info(Messages.getString("TranspileXspMojo.noChangesDetected")); //$NON-NLS-1$
			}
		}
	}
	
	private NonVerifyingPathClassLoader buildProjectClassLoader() throws MojoExecutionException {
		// Build a class loader based on all project dependencies
		List<Path> dependencies;
		try {
			dependencies = project.getArtifacts().stream()
				.map(Artifact::getFile)
				.map(File::toPath)
				.peek(p -> {
					if(log.isDebugEnabled()) {
						log.debug(MessageFormat.format(Messages.getString("TranspileXspMojo.addingClasspathEntry"), p)); //$NON-NLS-1$
					}
				})
				.collect(Collectors.toCollection(LinkedList::new));
			
		} catch (Exception e) {
			throw new MojoExecutionException(Messages.getString("TranspileXspMojo.exceptionBuildingClasspath"), e); //$NON-NLS-1$
		}
		
		return new NonVerifyingPathClassLoader(dependencies, ClassLoader.getSystemClassLoader());
	}
	
	private List<Path> scanXspChanges(Scanner scanner, Path sourceRoot) {
		scanner.setIncludes(new String[] { "*.xsp" }); //$NON-NLS-1$
		scanner.scan();
		String[] changes = scanner.getIncludedFiles();
		if(changes != null) {
			return Arrays.stream(changes)
				.map(sourceRoot::resolve)
				.peek(p -> {
					if(log.isDebugEnabled()) {
						log.debug(MessageFormat.format(Messages.getString("TranspileXspMojo.foundChangedFile"), p)); //$NON-NLS-1$
					}
				})
				.collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}
	
	private void deleteChanges(ClassLoader projectClassLoader, List<Path> deletedFiles, Path sourceRoot, Path output) {
		Class<?> innerUtil = TranspilerUtil.loadClass(projectClassLoader, TranspilerUtil.class.getName());
		deletedFiles.stream()
			.map(p -> TranspilerUtil.call(innerUtil, "xspPathToJavaPath", new Class<?>[] { Path.class }, sourceRoot.relativize(p))) //$NON-NLS-1$
			.map(Path.class::cast)
			.map(output::resolve)
			.filter(Files::isRegularFile)
			.peek(p -> {
				if(log.isDebugEnabled()) {
					log.debug(MessageFormat.format(Messages.getString("TranspileXspMojo.clearingDeletedTranslation"), p)); //$NON-NLS-1$
				}
			})
			.forEach(p -> {
				File file = p.toFile();
				file.delete();
				buildContext.refresh(file);
			});
	}
}
