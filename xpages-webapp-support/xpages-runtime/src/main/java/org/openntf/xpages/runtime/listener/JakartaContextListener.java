package org.openntf.xpages.runtime.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.openntf.xpages.runtime.platform.JakartaPlatform;
import org.openntf.xpages.runtime.wrapper.JakartaServletContextWrapper;

@WebListener
public class JakartaContextListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		JakartaPlatform.initContext(new JakartaServletContextWrapper(sce.getServletContext()));
	}
}
