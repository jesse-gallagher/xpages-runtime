package org.openntf.openliberty.xpages.xsp;

import com.ibm.designer.runtime.Application;
import com.ibm.xsp.library.ApplicationFacesClassLoader;

public class LibertyFacesClassLoader extends ApplicationFacesClassLoader {
    public LibertyFacesClassLoader(Application application) {
        super(application);
    }


}
