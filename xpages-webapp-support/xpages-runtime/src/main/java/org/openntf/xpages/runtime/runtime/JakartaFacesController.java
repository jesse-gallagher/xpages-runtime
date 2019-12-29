package org.openntf.xpages.runtime.runtime;

import com.ibm.xsp.controller.DesignerFacesController;
import com.ibm.xsp.page.FacesPageDriver;

public class JakartaFacesController extends DesignerFacesController {
	
	public JakartaFacesController() {
		super();
	}

	@Override
	protected FacesPageDriver createPageDriver() {
		return new DynamicPageDriver();
	}
}
