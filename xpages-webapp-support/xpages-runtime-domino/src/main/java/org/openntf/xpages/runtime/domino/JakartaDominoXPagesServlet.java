/**
 * Copyright © 2019-2020 Jesse Gallagher
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
package org.openntf.xpages.runtime.domino;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.designer.runtime.domino.bootstrap.BootstrapEnvironment;
import com.ibm.designer.runtime.domino.bootstrap.RequestContext;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletRequestAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletResponseAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpSessionAdapter;
import com.ibm.xsp.acl.NoAccessSignal;
import com.ibm.xsp.webapp.DesignerFacesServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openntf.xpages.runtime.domino.adapter.ServletHttpServletResponseAdapter;
import org.openntf.xpages.runtime.domino.adapter.ServletHttpSessionAdapter;
import org.openntf.xpages.runtime.domino.wrapper.JakartaDominoServletRequestWrapper;
import org.openntf.xpages.runtime.wrapper.JakartaServletRequestWrapper;

import java.io.IOException;

@WebServlet(urlPatterns="*.nsf")
public class JakartaDominoXPagesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static ServletConfig servletConfig;

	private DesignerFacesServlet delegate;
	private LCDEnvironment lcdEnvironment;

	public JakartaDominoXPagesServlet() {
		
//		try {
//			// TODO figure out what else to init to get it to work in an alternate context path.
//			//   Currently, XPages are generated with absolute "/xsp" URLs
//			// This could be done with UrlProcessors in RequestCustomizers
//			this.lcdEnvironment = new LCDEnvironment();
//			this.lcdEnvironment.initialize();
//		} catch(Throwable t) {
//			t.printStackTrace();
//			throw new RuntimeException(t);
//		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		servletConfig = config;
		BootstrapEnvironment.getInstance().setGlobalContextPath(config.getServletContext().getContextPath(), true);
		this.lcdEnvironment = LCDEnvironment.getInstance();

		this.delegate = new DesignerFacesServlet();
		delegate.init(config);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {	
		String pathInfo = req.getRequestURI();
		int nsfIndex = pathInfo.indexOf(".nsf"); //$NON-NLS-1$
		if(lcdEnvironment != null && (nsfIndex > -1 || true)) {
			// Pass NSF requests to the stock LCD processor. The advantage here is that it takes care of
			//   everything. However, it also doesn't take into account the various Liberty adapters, so
			//   the actual environment is semi-forced down to a crappier Servlet level
			String contextPath = StringUtil.toString(req.getContextPath());
			String path = pathInfo.substring(contextPath.length());
			RequestContext requestContext = new RequestContext(contextPath, path);
			HttpSessionAdapter sessionAdapter = new ServletHttpSessionAdapter(req.getSession());
			HttpServletRequestAdapter requestAdapter = new JakartaDominoServletRequestWrapper(req);
			HttpServletResponseAdapter responseAdapter = new ServletHttpServletResponseAdapter(resp);
			try {
				lcdEnvironment.service(requestContext, sessionAdapter, requestAdapter, responseAdapter);
			} catch(NoAccessSignal s) {
				// TODO see if this can signal the container for form-based auth
				resp.setHeader("WWW-Authenticate", "Basic realm=\"XPagesRuntime\""); //$NON-NLS-1$ //$NON-NLS-2$
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You must log in"); //$NON-NLS-1$
			}
		} else {
			// In-app XPage
			delegate.service(new JakartaServletRequestWrapper(req), resp);
		}
	}

	@Override
	public void destroy() {
		delegate.destroy();
	}
}
