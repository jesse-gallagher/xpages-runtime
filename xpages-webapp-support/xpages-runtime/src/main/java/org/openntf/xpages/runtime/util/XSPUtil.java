/**
 * Copyright © 2019 Jesse Gallagher
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
package org.openntf.xpages.runtime.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;

import com.ibm.commons.util.StringUtil;

public enum XSPUtil {
	;
	
	public static URL getResource(String path, ClassLoader... cls) {
		for(ClassLoader cl : cls) {
			URL result = cl.getResource(path);
			if(result == null && path != null && path.startsWith("/")) {
				// Try without the leading slash
				result = cl.getResource(path.substring(1));
			}
			if(result == null && path != null && !path.startsWith("/")) {
				result = cl.getResource("/" + path);
			}
			if(result != null) {
				return result;
			}
		}
		
		// Try with the thread classloader if it hasn't been included
		if(!Arrays.asList(cls).contains(Thread.currentThread().getContextClassLoader())) {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			URL result = cl.getResource(path);
			if(result == null && path != null && path.startsWith("/")) {
				result = cl.getResource(path.substring(1));
			}
			if(result == null && path != null && !path.startsWith("/")) {
				result = cl.getResource("/" + path);
			}
			if(result != null) {
				return result;
			}
		}
		
		return null;
	}
	
	public static InputStream getResourceAsStream(String path, ClassLoader... cls) {
		for(ClassLoader cl : cls) {
			InputStream result = cl.getResourceAsStream(path);
			if(result == null && path != null && path.startsWith("/")) {
				result = cl.getResourceAsStream(path.substring(1));
			}
			if(result == null && path != null && !path.startsWith("/")) {
				result = cl.getResourceAsStream("/" + path);
			}
			if(result != null) {
				return result;
			}
		}
		
		// Try with the thread classloader if it hasn't been included
		if(!Arrays.asList(cls).contains(Thread.currentThread().getContextClassLoader())) {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			InputStream result = cl.getResourceAsStream(path);
			if(result == null && path != null && path.startsWith("/")) {
				result = cl.getResourceAsStream(path.substring(1));
			}
			if(result == null && path != null && !path.startsWith("/")) {
				result = cl.getResourceAsStream("/" + path);
			}
			if(result != null) {
				return result;
			}
		}
		
		return null;
	}
	
	public static Enumeration<URL> getResources(ClassLoader cl, String p) throws IOException {
		String path = StringUtil.toString(p);
		
		Enumeration<URL> result = cl.getResources(path);
		if((result == null || !result.hasMoreElements()) && path != null && path.startsWith("/")) {
			result = cl.getResources(path.substring(1));
		}
		
		if((result == null || !result.hasMoreElements()) && path != null && path.startsWith("/")) {
			result = Thread.currentThread().getContextClassLoader().getResources(path.substring(1));
		}
		
		return result;
	}
}
