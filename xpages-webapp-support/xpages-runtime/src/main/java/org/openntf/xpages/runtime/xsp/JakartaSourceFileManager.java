/*
 * Copyright © 2019-2024 Jesse Gallagher
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
package org.openntf.xpages.runtime.xsp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.javacompiler.JavaSourceClassLoader;
import com.ibm.xsp.extlib.javacompiler.impl.JavaFileObjectClass;
import com.ibm.xsp.extlib.javacompiler.impl.SourceFileManager;

public class JakartaSourceFileManager extends SourceFileManager {
	private final JavaFileManager fileManager;
	
	public JakartaSourceFileManager(JavaFileManager fileManager, JavaSourceClassLoader classLoader, String[] classPath, boolean resolve) {
		super(fileManager, classLoader, classPath, resolve);
		this.fileManager = fileManager;
	}
	
	@Override
	public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse)
			throws IOException {
		Iterable<JavaFileObject> sup = fileManager.list(location, packageName, kinds, recurse);
		if(sup.iterator().hasNext()) {
			return sup;
		} else {
			return getClasses().stream()
				.filter(c -> c.binaryName().startsWith(packageName + ".")) //$NON-NLS-1$
				.collect(Collectors.toList());
		}
	}
	
	private List<JavaFileObjectClass> classes;
	
	private List<JavaFileObjectClass> getClasses() {
		if(classes == null) {
			try {
				List<JavaFileObjectClass> list = new LinkedList<>();
				for(URL url : Collections.list(Thread.currentThread().getContextClassLoader().getResources("/"))) { //$NON-NLS-1$
					String protocol = StringUtil.toString(url.getProtocol());
					if("file".equals(protocol)) { //$NON-NLS-1$
						Path dir = Paths.get(url.toURI());
						if(Files.isDirectory(dir)) {
							Files.walk(dir, FileVisitOption.FOLLOW_LINKS)
								.filter(p -> Files.isRegularFile(p))
								.filter(p -> p.getFileName().toString().endsWith(JavaSourceClassLoader.CLASS_EXTENSION))
								.forEach(p -> {
									String rel = dir.relativize(p).toString();
									String binaryName = removeClassExtension(StringUtil.replace(rel, File.separatorChar, '.'));
									list.add(new JavaFileObjectClass(p.toUri(), binaryName));
								});
						}
					} else if(protocol.contains("jar")) { //$NON-NLS-1$
						String jarUrl = url.toString();
						jarUrl = jarUrl.substring(0, jarUrl.indexOf("!/")); //$NON-NLS-1$
						try(InputStream is = new URL(jarUrl).openStream()) {
							try(ZipInputStream jis = new ZipInputStream(is)) {
								ZipEntry entry = jis.getNextEntry();
								while(entry != null) {
									String name = entry.getName();
									if(name.endsWith(JavaSourceClassLoader.CLASS_EXTENSION)) {
										URI uri = new URI(jarUrl + "!/" + name); //$NON-NLS-1$
										String binaryName = removeClassExtension(StringUtil.replace(name, '/' , '.'));
										list.add(new JavaFileObjectClass(uri, binaryName));
									}
									
									entry = jis.getNextEntry();
								}
							}
						}
					}
				}
				this.classes = list;
			} catch(IOException | URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}
		return classes;
	}
	
	private static String removeClassExtension(String s) {
		return s.substring(0, s.length()-JavaSourceClassLoader.CLASS_EXTENSION.length());
	}
}
