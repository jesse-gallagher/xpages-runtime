package org.openntf.openliberty.xpages.xsp;

import com.ibm.xsp.library.FacesClassLoader;
import com.ibm.xsp.page.compiled.CompiledPageDriver;

public class LibertyFacesPageDriver extends CompiledPageDriver {
    public LibertyFacesPageDriver(FacesClassLoader facesClassLoader) {
        super(facesClassLoader);
    }
}
