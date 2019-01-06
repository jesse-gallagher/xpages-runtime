package org.openntf.openliberty.xpages;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.application.DesignerApplicationFactoryImpl;

import javax.faces.application.Application;

public class LibertyFacesApplicationFactory extends DesignerApplicationFactoryImpl {

    @Override
    protected ApplicationEx createApplicationInstance(Application application) {
        return new LibertyFacesApplication(application);
    }
}
