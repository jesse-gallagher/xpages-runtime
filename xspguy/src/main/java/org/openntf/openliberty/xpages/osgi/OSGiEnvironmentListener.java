package org.openntf.openliberty.xpages.osgi;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class OSGiEnvironmentListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		OSGiPatcher.initKnownBundles();
	}
}
