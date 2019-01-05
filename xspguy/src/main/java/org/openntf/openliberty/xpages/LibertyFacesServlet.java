package org.openntf.openliberty.xpages;

import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.designer.runtime.domino.bootstrap.BootstrapEnvironment;
import com.ibm.designer.runtime.domino.bootstrap.RequestContext;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletRequestAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletResponseAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpSessionAdapter;
import com.ibm.domino.xsp.module.nsf.NSFService;
import com.ibm.xsp.webapp.DesignerFacesServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openntf.openliberty.xpages.adapter.ServletHttpServletResponseAdapter;
import org.openntf.openliberty.xpages.adapter.ServletHttpSessionAdapter;
import org.openntf.openliberty.xpages.platform.LibertyPlatform;
import org.openntf.openliberty.xpages.wrapper.LibertyServletConfigWrapper;
import org.openntf.openliberty.xpages.wrapper.LibertyServletRequestWrapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = "*.xsp")
public class LibertyFacesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final DesignerFacesServlet delegate;
	private final LCDEnvironment lcdEnvironment;

	public LibertyFacesServlet() {
		try {
			BootstrapEnvironment.getInstance().setGlobalContextPath("/", true);
			this.delegate = new DesignerFacesServlet();
			this.lcdEnvironment = new LCDEnvironment();
			this.lcdEnvironment.initialize();
		} catch(Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		delegate.init(config);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getRequestURI();
		int nsfIndex = pathInfo.indexOf(".nsf");
		if(nsfIndex > -1) {
//			String contextPath = pathInfo.substring(0, nsfIndex+4);
//			String path = pathInfo.substring(nsfIndex+4);
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
		
//		if(nsfService.isXspUrl(pathInfo, true)) {
//			System.out.println("Got an XSP url");
//		} else {
//			delegate.service(new LibertyServletRequestWrapper(req), resp);
//		}
	}

	@Override
	public void destroy() {
		delegate.destroy();
	}
}
