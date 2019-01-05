package org.openntf.openliberty.xpages;

import com.ibm.domino.napi.c.C;
import com.ibm.xsp.context.DojoLibraryFactory;
import com.ibm.xsp.domino.context.DominoDojo;
import com.ibm.xsp.registry.config.XspRegistryLoader;
import com.ibm.xsp.registry.config.XspRegistryManager;
import com.ibm.xsp.webapp.DesignerFacesServlet;

import lotus.notes.NotesThread;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openntf.openliberty.xpages.osgi.OSGiPatcher;
import org.openntf.openliberty.xpages.platform.LibertyPlatform;
import org.openntf.openliberty.xpages.wrapper.LibertyClassLoaderWrapper;
import org.openntf.openliberty.xpages.wrapper.LibertyServletConfigWrapper;
import org.openntf.openliberty.xpages.wrapper.LibertyServletContextWrapper;
import org.openntf.openliberty.xpages.wrapper.LibertyServletRequestWrapper;
import org.openntf.openliberty.xpages.xsp.LiberyXspRegistryLoader;

import java.io.File;
import java.io.IOException;

@WebServlet(urlPatterns = "*.xsp")
public class LibertyFacesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private DesignerFacesServlet delegate;

	public LibertyFacesServlet() {
		this.delegate = new DesignerFacesServlet();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		
		ServletConfig conf = new LibertyServletConfigWrapper(config);
		LibertyPlatform.initContext(conf.getServletContext());
		
		
//        XspRegistryLoader loader = new LiberyXspRegistryLoader();
//        XspRegistryManager.initManager(loader, true);
//        loader.initRegistryManager(XspRegistryManager.getManager());

		delegate.init(conf);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		delegate.service(new LibertyServletRequestWrapper(req), resp);
	}

	@Override
	public void destroy() {
		delegate.destroy();
	}
}
