package org.openntf.xpages.runtime.context;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;

import com.ibm.xsp.context.FacesContextFactoryImpl;

public class JakartaFacesContextFactory extends FacesContextFactoryImpl {
	public JakartaFacesContextFactory() {
		super();
	}
	
	public JakartaFacesContextFactory(FacesContextFactory delegate) {
		super(delegate);
	}
	
	@Override
	public FacesContext getFacesContext(Object var1, Object var2, Object var3, Lifecycle var4) throws FacesException {
		FacesContext context = super.getFacesContext(var1, var2, var3, var4);
		return new JakartaFacesContext(context);
	}
}
