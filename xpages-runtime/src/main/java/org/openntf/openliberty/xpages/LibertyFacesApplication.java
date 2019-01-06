package org.openntf.openliberty.xpages;

import com.ibm.xsp.application.ApplicationExImpl;
import com.ibm.xsp.application.DesignerApplicationEx;
import com.ibm.xsp.application.ViewHandlerEx;
import com.ibm.xsp.application.ViewHandlerExImpl;
import com.ibm.xsp.factory.FactoryLookup;
import com.ibm.xsp.library.FacesClassLoader;
import com.ibm.xsp.page.FacesPageDriver;

import javax.faces.application.Application;
import javax.faces.application.ViewHandler;

import org.openntf.openliberty.xpages.xsp.LibertyFacesClassLoader;
import org.openntf.openliberty.xpages.xsp.LibertyFacesPageDriver;
import org.openntf.openliberty.xpages.xsp.LibertyFactoryLookup;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class LibertyFacesApplication extends DesignerApplicationEx {
    private FactoryLookup factoryLookup;

    protected LibertyFacesApplication(Application application) {
        super(application);

        try {
            Field factoryLookupField = ApplicationExImpl.class.getDeclaredField("_factoryLookup");
            factoryLookupField.setAccessible(true);
            factoryLookupField.set(this, getFactoryLookup());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ViewHandler getViewHandler() {
        ViewHandler sup = super.getViewHandler();
        if(!(sup instanceof ViewHandlerEx)) {
            FacesClassLoader cl = new LibertyFacesClassLoader(getDesignerApplication());
            FacesPageDriver driver = new LibertyFacesPageDriver(cl);
            ViewHandlerExImpl result = new ViewHandlerExImpl(sup);
            result.setPageDriver(driver);
            return result;
        } else {
            return sup;
        }
    }

    @Override
    public FactoryLookup getFactoryLookup() {
        if(this.factoryLookup == null) {
            this.factoryLookup = new LibertyFactoryLookup(super.getFactoryLookup());
        }
        return this.factoryLookup;
    }

    @Override
    public String getApplicationProperty(String s, String s1) {
        String prop = super.getApplicationProperty(s, s1);
        return prop;
    }

    @Override
    protected ResourceBundle loadResourceBundle(String s, Locale locale) throws IOException {
        return super.loadResourceBundle(s, locale);
    }

    @Override
    public List<?> findServices(String s) {
        List<?> svc = super.findServices(s);
        return svc;
    }



}
