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
	public static ServletContext getServletContext() {
		return servletContext;
	}

	private File installationDirectory;
	private File userDataDirectory;
	private File propertiesDirectory;
	private File xspDirectory;
	private File nsfDirectory;
	private File styleKitsDirectory;
	private File serverDirectory;
	private Properties xspProperties;

	public JakartaPlatform() {
		super();

		installationDirectory = new File(System.getProperty("user.dir")); //$NON-NLS-1$
		userDataDirectory = new File(installationDirectory, "xpages"); //$NON-NLS-1$
		propertiesDirectory = new File(userDataDirectory, "properties"); //$NON-NLS-1$
		xspDirectory = new File(userDataDirectory, "xsp"); //$NON-NLS-1$
		nsfDirectory = new File(xspDirectory, "nsf"); //$NON-NLS-1$
		styleKitsDirectory = new File(nsfDirectory, "themes"); //$NON-NLS-1$
		serverDirectory = new File(userDataDirectory, StringUtil.replace("java/xsp", '/', File.separatorChar)); //$NON-NLS-1$
		this.xspProperties = this.loadStaticProperties();
		
		DojoLibraryFactory.initializeLibraries();
	}

	@Override
	public Object getObject(String s) {
		if("com.ibm.xsp.designer.ApplicationFinder".equals(s)) { //$NON-NLS-1$
			return new Application.IApplicationFinder() {
				@Override
				public Application get() {
					try {
						JakartaAppExecutionContext ctx = new JakartaAppExecutionContext(servletContext);
						return new JakartaApplication(ctx);
					} catch (ApplicationException e) {
						throw new RuntimeException(e);
					}
				}
			};
		} else {
			return super.getObject(s);
		}
	}
	
	@Override
	public File getGlobalResourceFile(String path) {
		if(path.startsWith("/stylekits/")) { //$NON-NLS-1$
			return new File(styleKitsDirectory, path.substring("/stylekits/".length())); //$NON-NLS-1$
		}
		if(path.startsWith("/server/")) { //$NON-NLS-1$
			return new File(serverDirectory, path.substring("/server/".length())); //$NON-NLS-1$
		}
		if(path.startsWith("/global/")) { //$NON-NLS-1$
			return new File(serverDirectory, path.substring("/global/".length())); //$NON-NLS-1$
		}
		if(path.startsWith("/properties/")) { //$NON-NLS-1$

			if (userDataDirectory != null) {
				File localFile = new File(userDataDirectory, "properties/" + path.substring("/properties/".length())); //$NON-NLS-1$ //$NON-NLS-2$
				if (localFile.exists()) {
					return localFile;
				}
			}

			return new File(propertiesDirectory, path.substring("/properties/".length())); //$NON-NLS-1$
		}
		return super.getGlobalResourceFile(path);
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
		if("Jakarta Web Application Server".equals(name)) { //$NON-NLS-1$
			return true;
		} else {
			return super.isPlatform(name);
		}
	}

	@Override
	public File getInstallationDirectory() {
		return installationDirectory;
	}

	@Override
	public File getResourcesDirectory() {
		return userDataDirectory;
	}

	protected Properties loadStaticProperties() {
		return new Properties();
	}
	
	@Override
	public String getName() {
		return "Jakarta Web Application Server"; //$NON-NLS-1$
	}
	
}
