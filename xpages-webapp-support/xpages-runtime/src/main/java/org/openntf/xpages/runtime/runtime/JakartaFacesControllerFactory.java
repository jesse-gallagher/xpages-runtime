package org.openntf.xpages.runtime.runtime;

import javax.servlet.ServletContext;

import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.controller.FacesController;
import com.ibm.xsp.controller.FacesControllerFactory;

public class JakartaFacesControllerFactory implements FacesControllerFactory {

	@Override
	public FacesController createFacesController(ServletContext servletContext) throws FacesExceptionEx {
		return new JakartaFacesController();
	}

}
