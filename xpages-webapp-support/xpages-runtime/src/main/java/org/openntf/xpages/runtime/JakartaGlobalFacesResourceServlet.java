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
package org.openntf.xpages.runtime;

import java.io.IOException;

import org.openntf.xsp.jakartaee.servlet.ServletUtil;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.ibm.xsp.webapp.DesignerGlobalResourceServlet;

@WebServlet(urlPatterns="/xsp/.ibmxspres/*")
public class JakartaGlobalFacesResourceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final DesignerGlobalResourceServlet delegate = new DesignerGlobalResourceServlet();
	
	@Override
	public void init() throws ServletException {
		super.init();
		try {
			delegate.init();
		} catch (javax.servlet.ServletException e) {
			throw new ServletException(e);
		}
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			delegate.init(ServletUtil.newToOld(config));
		} catch (javax.servlet.ServletException e) {
			throw new ServletException(e);
		}
	}
	
	@Override
	public void service(ServletRequest var1, ServletResponse var2) throws ServletException, IOException {
		try {
			delegate.service(ServletUtil.newToOld((HttpServletRequest)var1), ServletUtil.newToOld((HttpServletResponse)var2));
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
					throw new ServletException(t);
				}
			} else {
				throw new ServletException(t);
			}
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		delegate.destroy();
	}
}
