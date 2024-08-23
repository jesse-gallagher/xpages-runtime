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
package org.openntf.xpages.runtime.servlet;

import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.openntf.xsp.jakartaee.servlet.ServletUtil;

import com.ibm.xsp.webapp.DesignerModuleResourceServlet;

@WebServlet(urlPatterns="/xsp/.ibmmodres/*")
public class ModuleResourceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final DesignerModuleResourceServlet delegate = new DesignerModuleResourceServlet();
	
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
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		try {
			delegate.service(ServletUtil.newToOld((HttpServletRequest)req), ServletUtil.newToOld((HttpServletResponse)res));
		} catch (javax.servlet.ServletException e) {
			throw new ServletException(e);
		}
	}
}
