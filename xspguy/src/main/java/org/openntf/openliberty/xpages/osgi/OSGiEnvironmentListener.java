package org.openntf.openliberty.xpages.osgi;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class OSGiEnvironmentListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
			OSGiPatcher.initKnownBundles();
			return null;
		});
	}
}
