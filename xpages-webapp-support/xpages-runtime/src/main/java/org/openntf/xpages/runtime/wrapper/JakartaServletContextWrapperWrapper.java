/*
 * Copyright © 2019-2021 Jesse Gallagher
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
package org.openntf.xpages.runtime.wrapper;

import com.ibm.xsp.config.ServletContextWrapper;

import javax.servlet.ServletContext;

import org.openntf.xpages.runtime.util.XSPUtil;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class JakartaServletContextWrapperWrapper extends ServletContextWrapper {
	public JakartaServletContextWrapperWrapper(ServletContext delegate, List<String> configFiles, List<String> extraFiles) {
		super(delegate, configFiles, extraFiles);
	}
	
	@Override
	public URL getResource(String path) throws MalformedURLException {
		URL result = XSPUtil.getResource(path, Thread.currentThread().getContextClassLoader());
		if(result == null) {
			result = super.getResource(path);
		}
		return result;
	}
	
	@Override
	public InputStream getResourceAsStream(String path) {
		InputStream result = XSPUtil.getResourceAsStream(path, Thread.currentThread().getContextClassLoader());
		if(result == null) {
			result = super.getResourceAsStream(path);
		}
		return result;
	}
	
	
}
