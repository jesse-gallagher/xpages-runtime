package org.openntf.xpages.runtime.interpreter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.ibm.xsp.extlib.builder.ControlBuilder.IControl;
import com.ibm.xsp.extlib.interpreter.interpreter.Control;
import com.ibm.xsp.extlib.interpreter.interpreter.ControlPassthoughTag;
import com.ibm.xsp.extlib.interpreter.interpreter.ControlPassthoughText;
import com.ibm.xsp.page.compiled.AbstractCompiledPage.ComponentInfo;

/**
 * Holder for a {@link Control} object that builds and provides
 * a {@link com.ibm.xsp.page.compiled.AbstractCompiledPage.ComponentInfo ComponentInfo}
 * array suitable for {@link com.ibm.xsp.page.compiled.AbstractCompiledPage AbstractCompiledPage}.
 * 
 * @author Jesse Gallagher
 */
public class ComponentInfoControlWrapper {
	public static class ControlComponentInfo extends ComponentInfo {
		private final Control control;
		public ControlComponentInfo(boolean markup, int[] childIndexes, Object[][] facets, Control control) {
			super(markup, childIndexes, facets);
			this.control = control;
			
		}
		public Control getControl() {
			return control;
		}
		
	}
	
	private ControlComponentInfo[] componentInfo;

	public ComponentInfoControlWrapper(Control control) {
		this.componentInfo = createComponentInfo(control);
	}
	
	public ControlComponentInfo[] getComponentInfo() {
		return componentInfo;
	}

	private static ControlComponentInfo[] createComponentInfo(Control viewControl) {
		// Component info is essentially a mapping of a rough type to an array of its children by index
		List<ControlComponentInfo> components = new LinkedList<>();
		
		List<Integer> childIds = new ArrayList<>();
		List<IControl> childList = viewControl.getChildren();
		if(childList != null) {
			for(IControl child : childList) {
				childIds.add(addChild((Control)child, components));
			}
		}
		
		// Add the view root itself
		int[] children = childIds.stream().mapToInt(Integer::intValue).toArray();
		components.add(new ControlComponentInfo(false, children, new Object[0][0], viewControl));
		
		return components.toArray(new ControlComponentInfo[components.size()]);
	}
	
	private static int addChild(Control child, List<ControlComponentInfo> components) {
		boolean markup = child instanceof ControlPassthoughTag || child instanceof ControlPassthoughText;
		
		List<Object[]> facetDefs = new LinkedList<>();
		Map<String, IControl> facets = child.getFacets();
		if(facets != null) {
			for(Map.Entry<String, IControl> facetEntry : facets.entrySet()) {
				int facetIndex = addChild((Control)facetEntry.getValue(), components);
				facetDefs.add(new Object[] { facetEntry.getKey(), facetIndex });
			}
		}
		
		List<Integer> children = new LinkedList<>();
		List<IControl> childChildren = child.getChildren();
		if(childChildren != null) {
			for(IControl nextChild : childChildren) {
				int childIndex = addChild((Control)nextChild, components);
				children.add(childIndex);
			}
		}
		
		Object[][] facetsArray = facetDefs.toArray(new Object[0][]);
		int[] childIndexes = children.stream().mapToInt(Integer::intValue).toArray();
		components.add(new ControlComponentInfo(markup, childIndexes, facetsArray, child));
		return components.size()-1;
	}
}
