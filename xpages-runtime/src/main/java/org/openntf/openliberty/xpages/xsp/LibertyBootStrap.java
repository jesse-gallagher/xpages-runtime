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
            List<String> configFiles = this.getConfigFiles();
            List<String> extraFiles = this.getExtraFiles();
            ServletContextWrapper contextWrapper = new LibertyServletContextWrapperWrapper(servletContext, configFiles, extraFiles);
            ServletContextEvent sce = new ServletContextEvent(contextWrapper);
            this.getListener().contextInitialized(sce);
        } catch (Throwable t) {
            throw new FacesExceptionEx(t);
        }
    }

    protected void initContext(ServletContext servletContext) throws FacesException {
        try {
            List<String> configFiles = this.getConfigFiles();
            List<String> extraFiles = this.getExtraFiles();
            ServletContextWrapper contextWrapper = new LibertyServletContextWrapperWrapper(servletContext, configFiles, extraFiles);
            ServletContextEvent sce = new ServletContextEvent(contextWrapper);
            this.getListener().contextInitialized(sce);
            ApplicationFactoryEx var6 = (ApplicationFactoryEx)FactoryFinder.getFactory("javax.faces.application.ApplicationFactory");
            var6.initCompleted();
        } catch (Throwable t) {
            throw new FacesExceptionEx(t);
        }
    }

    protected void destroyContext(ServletContext servletContext) throws FacesException {
        try {
            ServletContextEvent sce = new ServletContextEvent(servletContext);
            this.getListener().contextDestroyed(sce);
            FactoryFinder.releaseFactories();
            this.destroyListener();
        } catch (Throwable t) {
            throw new FacesExceptionEx(t);
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
