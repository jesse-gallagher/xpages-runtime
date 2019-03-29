package org.openntf.xpages.runtime.context;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import com.ibm.xsp.context.FacesContextExImpl;

public class JakartaFacesContext extends FacesContextExImpl {
	
	private ExternalContext externalContext;

	public JakartaFacesContext(FacesContext delegate) {
		super(delegate);
	}
	
	@Override
	public synchronized ExternalContext getExternalContext() {
		if(this.externalContext == null) {
			this.externalContext = new JakartaExternalContext(super.getExternalContext());
		}
		return this.externalContext;
	}
}
