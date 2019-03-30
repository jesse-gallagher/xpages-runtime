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
package org.openntf.xpages.runtime;

import com.ibm.designer.runtime.ApplicationException;
import com.ibm.designer.runtime.server.ServletExecutionContext;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

public class JakartaAppExecutionContext extends ServletExecutionContext {
	private String appDirectory;
	
    public JakartaAppExecutionContext(ServletContext servletContext) throws ApplicationException {
        super("Jakarta App", "jakartaApp", servletContext);
    }
    
	public String getApplicationDirectory() {
		ServletContext servletContext = getServletContext();
		if (this.appDirectory == null && servletContext != null) {
			this.appDirectory = servletContext.getRealPath(".");
			if (this.appDirectory != null) {
				File var1 = new File(this.appDirectory + File.separator + "WEB-INF");
				if (!var1.exists()) {
					try {
						URL var2 = servletContext.getResource(System.getProperty("user.dir"));
						this.appDirectory = this.appDirectory + var2.getFile();
					} catch (MalformedURLException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return this.appDirectory;
	}
}
