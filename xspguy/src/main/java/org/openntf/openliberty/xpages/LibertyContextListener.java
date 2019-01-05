package org.openntf.openliberty.xpages;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.openntf.openliberty.xpages.platform.LibertyPlatform;
import org.openntf.openliberty.xpages.wrapper.LibertyServletContextWrapper;

import com.ibm.domino.napi.c.C;

import lotus.notes.NotesThread;

@WebListener
public class LibertyContextListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		NotesThread.sinitThread();
		C.initLibrary(null);
		
		LibertyPlatform.initContext(new LibertyServletContextWrapper(sce.getServletContext()));
	}
}
