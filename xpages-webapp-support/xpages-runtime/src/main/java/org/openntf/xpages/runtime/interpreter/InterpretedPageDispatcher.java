package org.openntf.xpages.runtime.interpreter;

import com.ibm.xsp.core.Version;
import com.ibm.xsp.extlib.interpreter.interpreter.Control;
import com.ibm.xsp.page.compiled.AbstractCompiledPage;
import com.ibm.xsp.page.compiled.AbstractCompiledPageDispatcher;

public class InterpretedPageDispatcher extends AbstractCompiledPageDispatcher {
	private final ComponentInfoControlWrapper viewControl;

	public InterpretedPageDispatcher(Control viewControl) {
		super(Version.CurrentRuntimeVersion.toString());
		this.viewControl = new ComponentInfoControlWrapper(viewControl);
	}

	@Override
	protected AbstractCompiledPage createPage(int pageIndex) {
		return new InterpretedFacesPage(viewControl);
	}

}
