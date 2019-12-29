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
package org.openntf.xpages.runtime.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openntf.xpages.runtime.platform.JakartaPlatform;
import org.openntf.xpages.runtime.wrapper.JakartaServletConfigWrapper;
import org.openntf.xpages.runtime.wrapper.JakartaServletRequestWrapper;
import org.openntf.xpages.runtime.wrapper.JakartaServletResponseWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;
import com.ibm.commons.xml.XResult;
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
		
		ServletConfig conf = new JakartaServletConfigWrapper(config);
		JakartaPlatform.initContext(conf.getServletContext());
		delegate.init(conf);
		
		resources.addResourceProvider(new JavaResourceProvider("") {
			@Override
			protected String getResourcePath(HttpServletRequest req, String path) {
				if(!"/".equals(path) && Thread.currentThread().getContextClassLoader().getResourceAsStream(path) != null) {
					return path;
				} else {
					return null;
				}
			}
		});
		resources.init(conf);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = StringUtil.toString(req.getServletPath());
		if("/".equals(path)) {
			// Check the welcome-file-list
			path = PathUtil.concat("/", getIndex(), '/');
		}
		HttpServletResponse resWrap = new JakartaServletResponseWrapper(resp);
		int xspIndex = path.indexOf(".xsp");
		if(xspIndex > -1) {
			String pathInfo = path.substring(xspIndex+4);
			HttpServletRequest wrap = new JakartaServletRequestWrapper(req, path.substring(0, xspIndex+4), pathInfo.isEmpty() ? null : pathInfo);
			delegate.service(wrap, resWrap);
		} else {
			HttpServletRequest wrap = new JakartaServletRequestWrapper(req, "/", path);
			resources.service(wrap, resWrap);
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
				try(InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("/WEB-INF/web.xml")) {
					webXml = DOMUtil.createDocument(is);
				}
				XResult result = DOMUtil.evaluateXPath(webXml, "/*[name()='web-app']/*[name()='welcome-file-list']/*[name()='welcome-file']/text()");
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