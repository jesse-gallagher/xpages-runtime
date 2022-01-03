/*
 * Copyright © 2019-2022 Jesse Gallagher
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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;

import com.ibm.xsp.webapp.DesignerGlobalResourceServlet;

@WebServlet(urlPatterns="/xsp/.ibmxspres/*")
public class JakartaGlobalFacesResourceServlet extends DesignerGlobalResourceServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void service(ServletRequest var1, ServletResponse var2) throws ServletException, IOException {
		try {
			super.service(var1, var2);
		} catch(IOException e) {
			if("Broken pipe".equals(e.getMessage())) { //$NON-NLS-1$
				// Ignore
			} else {
				throw e;
			}
		} catch(Throwable t) {
			// May be wrapped
			if(t.getCause() instanceof IOException) {
				if("Broken pipe".equals(t.getCause().getMessage())) { //$NON-NLS-1$
					// Ignore
				} else {
					throw t;
				}
			} else {
				throw t;
			}
		}
	}
}
