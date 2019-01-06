package org.openntf.openliberty.xpages;

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

import org.openntf.openliberty.xpages.adapter.ServletHttpServletResponseAdapter;
import org.openntf.openliberty.xpages.adapter.ServletHttpSessionAdapter;
import org.openntf.openliberty.xpages.wrapper.LibertyServletRequestWrapper;

import java.io.IOException;

@WebServlet(urlPatterns="*")
public class LibertyFacesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static ServletConfig servletConfig;

	private DesignerFacesServlet delegate;
	private LCDEnvironment lcdEnvironment;

	public LibertyFacesServlet() {
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		servletConfig = config;
		
		try {
			// TODO figure out what else to init to get it to work in an alternate context path.
			//   Currently, XPages are generated with absolute "/xsp" URLs
			BootstrapEnvironment.getInstance().setGlobalContextPath(config.getServletContext().getContextPath(), true);
			this.lcdEnvironment = new LCDEnvironment();
			this.lcdEnvironment.initialize();
		} catch(Throwable t) {
			t.printStackTrace();
			throw new ServletException(t);
		}

		this.delegate = new DesignerFacesServlet();
		delegate.init(config);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {	
		String pathInfo = req.getRequestURI();
		int nsfIndex = pathInfo.indexOf(".nsf");
		if(lcdEnvironment != null && nsfIndex > -1) {
			// Pass NSF requests to the stock LCD processor. The advantage here is that it takes care of
			//   everything. However, it also doesn't take into account the various Liberty adapters, so
			//   the actual environment is semi-forced down to a crappier Servlet level
			String contextPath = StringUtil.toString(req.getContextPath());
			String path = pathInfo.substring(contextPath.length());
			RequestContext requestContext = new RequestContext(contextPath, path);
			HttpSessionAdapter sessionAdapter = new ServletHttpSessionAdapter(req.getSession());
			HttpServletRequestAdapter requestAdapter = new LibertyServletRequestWrapper(req);
			HttpServletResponseAdapter responseAdapter = new ServletHttpServletResponseAdapter(resp);
			try {
				lcdEnvironment.service(requestContext, sessionAdapter, requestAdapter, responseAdapter);
			} catch(NoAccessSignal s) {
				// TODO see if this can signal the container for form-based auth
				resp.setHeader("WWW-Authenticate", "Basic realm=\"XPagesRuntime\"");
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You must log in");
			}
		} else {
			// In-app XPage
			delegate.service(new LibertyServletRequestWrapper(req), resp);
		}
	}

	@Override
	public void destroy() {
		delegate.destroy();
	}
}
