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

import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.openntf.xpages.runtime.platform.JakartaPlatform;
import org.openntf.xpages.runtime.wrapper.JakartaServletConfigWrapper;
import org.openntf.xpages.runtime.wrapper.JakartaServletRequestWrapper;
import org.openntf.xpages.runtime.wrapper.JakartaServletResponseWrapper;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;
import com.ibm.commons.xml.XResult;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.webapp.FacesResourceServlet;
import com.ibm.xsp.webapp.resources.JavaResourceProvider;

@WebServlet(value="/")
public class JakartaServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static ServletConfig servletConfig;

	private JakartaFacesServlet delegate;
	private FacesResourceServlet resources = new FacesResourceServlet() {
		private static final long serialVersionUID = 1L;
		
	}; 

	public JakartaServlet() {
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		servletConfig = config;
		
		this.delegate = new JakartaFacesServlet();
		
		javax.servlet.ServletConfig conf = new JakartaServletConfigWrapper(ServletUtil.newToOld(config));
		JakartaPlatform.initContext(conf.getServletContext());
		try {
			delegate.init(conf);
		} catch (javax.servlet.ServletException e) {
			throw new ServletException(e);
		}
		
		resources.addResourceProvider(new JavaResourceProvider("") { //$NON-NLS-1$
			@Override
			protected String getResourcePath(javax.servlet.http.HttpServletRequest req, String path) {
				if(!"/".equals(path) && Thread.currentThread().getContextClassLoader().getResourceAsStream(path) != null) { //$NON-NLS-1$
					return path;
				} else {
					return null;
				}
			}
			
			@Override
			protected boolean shouldCacheResources() {
				return !ExtLibUtil.isDevelopmentMode();
			}
		});
		try {
			resources.init(conf);
		} catch (javax.servlet.ServletException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = StringUtil.toString(req.getServletPath());
		if("/".equals(path)) { //$NON-NLS-1$
			// Check the welcome-file-list
			path = PathUtil.concat("/", getIndex(), '/'); //$NON-NLS-1$
		}
		javax.servlet.http.HttpServletResponse resWrap = new JakartaServletResponseWrapper(ServletUtil.newToOld(resp));
		int xspIndex = path.indexOf(".xsp"); //$NON-NLS-1$
		if(xspIndex > -1) {
			String pathInfo = path.substring(xspIndex+4);
			javax.servlet.http.HttpServletRequest wrap = new JakartaServletRequestWrapper(ServletUtil.newToOld(req), path.substring(0, xspIndex+4), pathInfo.isEmpty() ? null : pathInfo);
			try {
				delegate.service(wrap, resWrap);
			} catch (javax.servlet.ServletException | IOException e) {
				throw new ServletException(e);
			}
		} else {
			javax.servlet.http.HttpServletRequest wrap = new JakartaServletRequestWrapper(ServletUtil.newToOld(req), "/", path); //$NON-NLS-1$
			try {
				resources.service(wrap, resWrap);
			} catch (javax.servlet.ServletException | IOException e) {
				throw new ServletException(e);
			}
		}
	}

	@Override
	public void destroy() {
		delegate.destroy();
	}
	
	private String index;
	private String getIndex() {
		if(this.index == null) {
			try {
				Document webXml;
				try(InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("/WEB-INF/web.xml")) { //$NON-NLS-1$
					webXml = DOMUtil.createDocument(is);
				}
				XResult result = DOMUtil.evaluateXPath(webXml, "/*[name()='web-app']/*[name()='welcome-file-list']/*[name()='welcome-file']/text()"); //$NON-NLS-1$
				if(!result.isEmpty()) {
					Node node = (Node)result.getNodes()[0];
					return node.getTextContent();
				}
			} catch(IOException | XMLException e) {
				throw new RuntimeException(e);
			}
		}
		return this.index;
	}
}