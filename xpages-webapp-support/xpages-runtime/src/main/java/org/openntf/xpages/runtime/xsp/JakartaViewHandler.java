package org.openntf.xpages.runtime.xsp;

import javax.faces.application.ViewHandler;
import javax.faces.context.FacesContext;

import com.ibm.xsp.application.ViewHandlerExImpl;

public class JakartaViewHandler extends ViewHandlerExImpl {

	public JakartaViewHandler(ViewHandler delegate) {
		super(delegate);
	}

	@Override
	public String getResourceURL(FacesContext facesContext, String res) {
		String url = super.getResourceURL(facesContext, res);
		return url;
	}
}
