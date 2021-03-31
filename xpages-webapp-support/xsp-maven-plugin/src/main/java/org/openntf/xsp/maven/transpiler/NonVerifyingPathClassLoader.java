/*
 * Copyright © 2020-2021 Jesse Gallagher
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.ibm.commons.util.io.StreamUtil;

/**
 * {@link ClassLoader} implementation that loads classes from a collection of file paths,
 * but does not verify signatures.
 * 
 * <p>This is useful to avoid trouble with OSGi-style split packages in different JARs that
 * may or may not have the same signer.</p>
 * 
 * @author Jesse Gallagher
 */
public class NonVerifyingPathClassLoader extends ClassLoader implements AutoCloseable {
	private final Collection<Path> paths;
	private final ClassLoader delegate;
	private final URLClassLoader resourceLoader;
	private final Map<Path, JarFile> jarFiles = new HashMap<>();
	
	public NonVerifyingPathClassLoader(Collection<Path> paths, ClassLoader delegate) {
		super();
		this.paths = Objects.requireNonNull(paths);
		this.resourceLoader = URLClassLoader.newInstance(
			paths.stream()
				.map(Path::toUri)
				.map(t -> {
					try {
						return t.toURL();
					} catch (MalformedURLException e) {
						throw new RuntimeException(e);
					}
				})
				.toArray(size -> new URL[size])
		);
		this.delegate = delegate;
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		String binaryName = String.valueOf(name)
			.replace('.', '/')
			+ ".class"; //$NON-NLS-1$
		for(Path path : paths) {
			if(Files.isRegularFile(path)) {
				try {
					JarFile f = jarFiles.computeIfAbsent(path, p -> {
						try {
							return new JarFile(p.toFile());
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});
					JarEntry entry = f.getJarEntry(binaryName);
					if(entry != null) {
						// Read in the class data
						try(InputStream is = f.getInputStream(entry)) {
							return defineClass(name, is);
						}
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else if(Files.isDirectory(path)) {
				Path classFile = path.resolve(binaryName.replace('/', File.separatorChar));
				if(Files.isRegularFile(classFile)) {
					// Read in the class data
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					try {
						Files.copy(classFile, baos);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					byte[] classData = baos.toByteArray();
					return defineClass(name, classData, 0, classData.length);
				}
			}
		}
		
		return delegate.loadClass(name);
	}
	
	public Class<?> defineClass(String name, InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		StreamUtil.copyStream(is, baos);
		byte[] classData = baos.toByteArray();
		return defineClass(name, classData, 0, classData.length);
	}

	@Override
	protected URL findResource(String name) {
		return resourceLoader.findResource(name);
	}
	
	@Override
	protected Enumeration<URL> findResources(String name) throws IOException {
		return resourceLoader.findResources(name);
	}
	
	@Override
	public void close() throws Exception {
		resourceLoader.close();
		for(JarFile f : jarFiles.values()) {
			f.close();
		}
	}
}
