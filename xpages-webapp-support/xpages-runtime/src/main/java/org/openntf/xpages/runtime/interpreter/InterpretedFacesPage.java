package org.openntf.xpages.runtime.interpreter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.openntf.xpages.runtime.interpreter.ComponentInfoControlWrapper.ControlComponentInfo;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.component.UIViewRootEx2;
import com.ibm.xsp.page.compiled.AbstractCompiledPage;
import com.ibm.xsp.page.compiled.NoSuchComponentException;
import com.ibm.xsp.page.compiled.PageExpressionEvaluator;

public class InterpretedFacesPage extends AbstractCompiledPage {
	private final ComponentInfoControlWrapper viewControl;

	public InterpretedFacesPage(ComponentInfoControlWrapper viewControl) {
		super(viewControl.getComponentInfo().length-1, viewControl.getComponentInfo());
		this.viewControl = viewControl;
	}
	
	@Override
	public int getComponentForId(String id) throws NoSuchComponentException {
		// ID is bare and lowercased
		
		ControlComponentInfo[] info = viewControl.getComponentInfo();
		for(int i = 0; i < info.length; i++) {
			if(StringUtil.toString(info[i].getControl().getId()).equals(id)) {
				return i;
			}
		}
		
		return -1;
	}

	@Override
	public UIComponent createComponent(int index, FacesContext facesContext, UIComponent parent, PageExpressionEvaluator evaluator)
			throws NoSuchComponentException {
		ControlComponentInfo[] info = viewControl.getComponentInfo();
		if(index < 0 || index >= info.length) {
			new NoSuchComponentException(index);
		}
		UIComponent result = info[index].getControl().getComponent();
		
		if(index == info.length-1) {
			// It's the view root
			initViewRoot((UIViewRootEx2)result);
		}
		
		return result;
	}
}
