package org.openntf.openliberty.xpages.listener;

import com.ibm.xsp.config.BootStrap;
import com.ibm.xsp.config.ConfigureCoreListener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

import org.openntf.openliberty.xpages.wrapper.LibertyServletContextWrapper;
import org.openntf.openliberty.xpages.xsp.LibertyBootStrap;

@WebListener
public class LibertyConfigureCoreListener extends ConfigureCoreListener {
    BootStrap bootstrap;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext context = new LibertyServletContextWrapper(servletContextEvent.getServletContext());
        bootstrap = new LibertyBootStrap(context);
        bootstrap.init(context);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        bootstrap.destroy(new LibertyServletContextWrapper(servletContextEvent.getServletContext()));
    }
}
