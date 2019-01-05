package org.openntf.openliberty.xpages;

import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.designer.runtime.domino.bootstrap.BootstrapEnvironment;
import com.ibm.designer.runtime.domino.bootstrap.RequestContext;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletRequestAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletResponseAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpSessionAdapter;
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

@WebServlet(urlPatterns = "/")
public class LibertyFacesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private DesignerFacesServlet delegate;
	private LCDEnvironment lcdEnvironment;

	public LibertyFacesServlet() {
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		try {
			BootstrapEnvironment.getInstance().setGlobalContextPath("/", true);
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
			String contextPath = "";
			String path = pathInfo;
			RequestContext requestContext = new RequestContext(contextPath, path);
			HttpSessionAdapter sessionAdapter = new ServletHttpSessionAdapter(req.getSession());
			HttpServletRequestAdapter requestAdapter = new LibertyServletRequestWrapper(req);
			HttpServletResponseAdapter responseAdapter = new ServletHttpServletResponseAdapter(resp);
			lcdEnvironment.service(requestContext, sessionAdapter, requestAdapter, responseAdapter);
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
