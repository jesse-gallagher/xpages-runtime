package org.openntf.xpages.runtime.servlet;

import org.openntf.xpages.runtime.runtime.JakartaFacesControllerFactory;

import com.ibm.xsp.controller.FacesControllerFactory;
import com.ibm.xsp.webapp.DesignerFacesServlet;

public class JakartaFacesServlet extends DesignerFacesServlet {
	private final FacesControllerFactory factory = new JakartaFacesControllerFactory();
	
	@Override
	protected FacesControllerFactory getFacesControllerFactory() {
		return factory;
	}
}
