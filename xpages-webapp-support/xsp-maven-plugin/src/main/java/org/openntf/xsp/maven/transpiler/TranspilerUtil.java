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
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.w3c.dom.Document;

import com.ibm.commons.util.io.StreamUtil;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;
import com.ibm.xsp.page.compiled.PageToClassNameUtil;

public enum TranspilerUtil {
	;
	
	public static Class<?> loadClass(ClassLoader classLoader, String className) {
		try {
			return classLoader.loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static Object newInstance(ClassLoader classLoader, String className, Class<?>[] parameterTypes, Object... parameters) {
		try {
			Class<?> clazz = classLoader.loadClass(className);
			Constructor<?> ctor = clazz.getConstructor(parameterTypes);
			return ctor.newInstance(parameters);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T call(Object obj, String methodName) {
		return call(obj, methodName, new Class<?>[0]);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T call(Object obj, String methodName, Class<?>[] parameterTypes, Object... parameters) {
		try {
			Method method = obj.getClass().getMethod(methodName, parameterTypes);
			return (T)method.invoke(obj, parameters);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T call(Class<?> clazz, String staticMethodName) {
		return call(clazz, staticMethodName, new Class<?>[0]);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T call(Class<?> clazz, String staticMethodName, Class<?>[] parameterTypes, Object... parameters) {
		try {
			Method method = clazz.getMethod(staticMethodName, parameterTypes);
			return (T)method.invoke(null, parameters);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String readFile(Path path) {
		try(InputStream is = Files.newInputStream(path)) {
			return StreamUtil.readString(is);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Document readXml(Path file) {
		try(InputStream is = Files.newInputStream(file)) {
			return DOMUtil.createDocument(is);
		} catch(IOException | XMLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void deltree(Collection<Path> paths) throws IOException {
		for(Path path : paths) {
			deltree(path);
		}
	}
	
	public static void deltree(Path path) throws IOException {
		if(Files.isDirectory(path)) {
			Files.list(path)
			    .forEach(t -> {
					try {
						deltree(t);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
		}
		try {
			Files.deleteIfExists(path);
		} catch(IOException e) {
			// This is likely a Windows file-locking thing. In this case,
			//   punt and hand it off to File#deleteOnExit
			path.toFile().deleteOnExit();
		}
	}
	
	@SuppressWarnings("nls")
	public static Path xspPathToJavaPath(Path xspFile) {
		if(xspFile.isAbsolute()) {
			throw new IllegalArgumentException("xspFile must be relative");
		}
		String className = PageToClassNameUtil.getClassNameForPage(xspFile.toString());
		String outputFileName = className.replace('.', File.separatorChar) + ".java"; //$NON-NLS-1$
		return Paths.get(outputFileName);
	}
}
