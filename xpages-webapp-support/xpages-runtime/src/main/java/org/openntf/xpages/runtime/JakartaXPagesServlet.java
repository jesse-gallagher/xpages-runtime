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

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.openntf.xpages.runtime.platform.JakartaPlatform;
import org.openntf.xpages.runtime.wrapper.JakartaServletConfigWrapper;
import org.openntf.xpages.runtime.wrapper.JakartaServletRequestWrapper;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;

import com.ibm.xsp.webapp.DesignerFacesServlet;

//@WebServlet(urlPatterns="*.xsp")
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
		
		javax.servlet.ServletConfig conf = new JakartaServletConfigWrapper(ServletUtil.newToOld(config));
		JakartaPlatform.initContext(conf.getServletContext());
		try {
			delegate.init(conf);
		} catch (javax.servlet.ServletException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		javax.servlet.http.HttpServletRequest wrap = new JakartaServletRequestWrapper(ServletUtil.newToOld(req));
		try {
			delegate.service(wrap, ServletUtil.newToOld(resp));
		} catch (javax.servlet.ServletException | IOException e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void destroy() {
		delegate.destroy();
	}
}
