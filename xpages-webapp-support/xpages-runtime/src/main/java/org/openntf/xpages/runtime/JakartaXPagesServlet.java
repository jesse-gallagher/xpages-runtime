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

import com.ibm.xsp.registry.config.XspRegistryLoader;
import com.ibm.xsp.registry.config.XspRegistryManager;
import com.ibm.xsp.webapp.DesignerFacesServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openntf.xpages.runtime.platform.JakartaPlatform;
import org.openntf.xpages.runtime.wrapper.JakartaServletConfigWrapper;
import org.openntf.xpages.runtime.wrapper.JakartaServletRequestWrapper;
import org.openntf.xpages.runtime.xsp.JakartaXspRegistryLoader;

import java.io.IOException;

@WebServlet(urlPatterns="*.xsp")
public class JakartaXPagesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static ServletConfig servletConfig;

	private DesignerFacesServlet delegate;

	public JakartaXPagesServlet() {
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		servletConfig = config;
		
		this.delegate = new DesignerFacesServlet();
		
		ServletConfig conf = new JakartaServletConfigWrapper(config);
		JakartaPlatform.initContext(conf.getServletContext());
		delegate.init(conf);

      XspRegistryLoader loader = new JakartaXspRegistryLoader();
      XspRegistryManager.initManager(loader, true);
      loader.initRegistryManager(XspRegistryManager.getManager());
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		delegate.service(new JakartaServletRequestWrapper(req), resp);
	}

	@Override
	public void destroy() {
		delegate.destroy();
	}
}
