package org.openntf.xpages.runtime.interpreter;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.context.FacesContextEx;
import com.ibm.xsp.extlib.interpreter.interpreter.ComplexProperty;
import com.ibm.xsp.extlib.interpreter.interpreter.Control;
import com.ibm.xsp.extlib.interpreter.interpreter.ControlPassthoughTag;
import com.ibm.xsp.extlib.interpreter.interpreter.XPagesObject;
import com.ibm.xsp.extlib.interpreter.interpreter.parser.DefaultControlFactory;
import com.ibm.xsp.registry.FacesComplexDefinition;
import com.ibm.xsp.registry.FacesComponentDefinition;
import com.ibm.xsp.registry.FacesDefinition;
import com.ibm.xsp.registry.FacesSharableRegistry;

public class InterpretedControlFactory extends DefaultControlFactory {

	public InterpretedControlFactory() {
		super();
	}

	public InterpretedControlFactory(FacesContextEx ctx) {
		super(ctx);
	}

	public InterpretedControlFactory(FacesSharableRegistry registry) {
		super(registry);
	}
	
	@Override
	public XPagesObject createXPagesObject(String nsUri, String tagName) {
		// Check for passthrough tags
	    if(StringUtil.isEmpty(nsUri)) {
	        return new ControlPassthoughTag(tagName);
	    }
	    FacesDefinition def = getRegistry().findDef(nsUri, tagName);
	    if(def==null) {
	        throw new FacesExceptionEx(null,"Unknown control/complex property, namespace {0}, tag {1}", nsUri, tagName);
	    }
	    if(def instanceof FacesComponentDefinition) {
	        return new Control((FacesComponentDefinition)def);
	    }
        if(def instanceof FacesComplexDefinition) {
            return new ComplexProperty((FacesComplexDefinition)def);
        }
        throw new FacesExceptionEx(null, "Invalid definition type {0} for tag {1}:{2}", def.getClass(), nsUri, tagName);
	}

}
