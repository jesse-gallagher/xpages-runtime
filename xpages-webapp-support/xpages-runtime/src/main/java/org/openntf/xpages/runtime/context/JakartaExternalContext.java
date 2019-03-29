package org.openntf.xpages.runtime.context;

import javax.faces.context.ExternalContext;

import org.openntf.xpages.runtime.platform.JakartaPlatform;

import com.ibm.xsp.context.ExternalContextEx;

public class JakartaExternalContext extends ExternalContextEx {
	public JakartaExternalContext(ExternalContext delegate) {
		super(delegate);
	}

	@Override
	public String encodeResourceURL(String var1) {
		String result = super.encodeResourceURL(var1);
		// Patch around the parent's "/xsp" prefix
		// TODO do this more intelligently
		if(result.startsWith("/xsp/")) {
			return JakartaPlatform.getServletContext().getContextPath() + result;
		} else {
			return result;
		}
	}

}
