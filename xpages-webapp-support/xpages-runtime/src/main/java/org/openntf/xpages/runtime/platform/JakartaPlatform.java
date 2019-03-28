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
package org.openntf.xpages.runtime.platform;

import com.ibm.commons.platform.WebAppServerPlatform;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.Application;
import com.ibm.designer.runtime.ApplicationException;
import com.ibm.xsp.context.DojoLibraryFactory;

import javax.servlet.ServletContext;

import org.openntf.xpages.runtime.JakartaAppExecutionContext;
import org.openntf.xpages.runtime.JakartaApplication;

import java.io.File;
import java.util.Properties;

public class JakartaPlatform extends WebAppServerPlatform {
	private static ServletContext servletContext;

	public static void initContext(ServletContext servletContext) {
		JakartaPlatform.servletContext = servletContext;
	}

	private Properties xspProperties;

	public JakartaPlatform() {
		super();
		
		DojoLibraryFactory.initializeLibraries();
	}

	@Override
	public Object getObject(String s) {
		switch (StringUtil.toString(s)) {
		case "com.ibm.xsp.designer.ApplicationFinder":
			return (Application.IApplicationFinder) () -> {
				try {
					JakartaAppExecutionContext ctx = new JakartaAppExecutionContext(servletContext);
					return new JakartaApplication(ctx);
				} catch (ApplicationException e) {
					throw new RuntimeException(e);
				}
			};
		default:
			return super.getObject(s);
		}
	}
	
	@Override
	public String getProperty(String prop) {
		String var2;
		if (this.xspProperties != null) {
			var2 = this.xspProperties.getProperty(prop);
			if (StringUtil.isNotEmpty(var2)) {
				return var2;
			}
		}
		return super.getProperty(prop);
	}
	
	@Override
	public boolean isPlatform(String name) {
		switch(StringUtil.toString(name)) {
		case "Domino":
			// Sure I am
			return true;
		default:
			return super.isPlatform(name);
		}
	}

	@Override
	public File getInstallationDirectory() {
		return null;
	}

	@Override
	public File getResourcesDirectory() {
		return null;
	}
	
}
