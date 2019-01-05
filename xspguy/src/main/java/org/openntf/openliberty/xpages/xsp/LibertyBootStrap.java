package org.openntf.openliberty.xpages.xsp;

import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.application.ApplicationFactoryEx;
import com.ibm.xsp.config.BootStrap;
import com.ibm.xsp.config.ServletContextWrapper;
import com.ibm.xsp.util.Delegation;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.openntf.openliberty.xpages.wrapper.LibertyServletContextWrapperWrapper;

import java.util.List;
import java.util.stream.Collectors;

public class LibertyBootStrap extends BootStrap {
    private ServletContextListener contextListener;
    private final ServletContext servletContext;

    public LibertyBootStrap(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void preloadFiles() {
        try {
            List var1 = this.getConfigFiles();
            List var2 = this.getExtraFiles();
            ServletContextWrapper var3 = new LibertyServletContextWrapperWrapper(servletContext, var1, var2);
            ServletContextEvent var4 = new ServletContextEvent(var3);
            this.getListener().contextInitialized(var4);
        } catch (Throwable var5) {
            throw new FacesExceptionEx(var5);
        }
    }

    protected void initContext(ServletContext var1) throws FacesException {
        try {
            List var2 = this.getConfigFiles();
            List var3 = this.getExtraFiles();
            ServletContextWrapper var4 = new LibertyServletContextWrapperWrapper(var1, var2, var3);
            ServletContextEvent var5 = new ServletContextEvent(var4);
            this.getListener().contextInitialized(var5);
            ApplicationFactoryEx var6 = (ApplicationFactoryEx)FactoryFinder.getFactory("javax.faces.application.ApplicationFactory");
            var6.initCompleted();
        } catch (Throwable var7) {
            throw new FacesExceptionEx(var7);
        }
    }

    protected void destroyContext(ServletContext var1) throws FacesException {
        try {
            ServletContextEvent var2 = new ServletContextEvent(var1);
            this.getListener().contextDestroyed(var2);
            FactoryFinder.releaseFactories();
            this.destroyListener();
        } catch (Throwable var3) {
            throw new FacesExceptionEx(var3);
        }
    }

    private ServletContextListener getListener() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (this.contextListener == null) {
            this.contextListener = (ServletContextListener)Delegation.getImplementation("context-listener");
        }

        return this.contextListener;
    }

    private void destroyListener() {
        this.contextListener = null;
    }

    @Override
    protected List<String> readConfigFiles() {
        return super.readConfigFiles().stream()
                .map(f -> !f.startsWith("/") ? ("/" + f) : f)
                .collect(Collectors.toList());
    }
}
