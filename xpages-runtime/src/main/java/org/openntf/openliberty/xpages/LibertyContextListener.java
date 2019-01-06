package org.openntf.openliberty.xpages;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.openntf.openliberty.xpages.platform.LibertyPlatform;
import org.openntf.openliberty.xpages.wrapper.LibertyServletContextWrapper;

import com.ibm.commons.util.StringUtil;
import com.ibm.domino.napi.c.C;
import com.ibm.domino.xsp.module.nsf.NotesURL;

import lotus.notes.NotesThread;

@WebListener
public class LibertyContextListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		NotesThread.sinitThread();
		C.initLibrary(null);
		
		LibertyPlatform.initContext(new LibertyServletContextWrapper(sce.getServletContext()));
		
		URLStreamHandlerFactory delegate = AccessController.doPrivileged((PrivilegedAction<URLStreamHandlerFactory>) () -> {
			URLStreamHandlerFactory d;
			try {
				// This is set by the Equinox dependency, which we definitely don't want
				Field facField = URL.class.getDeclaredField("factory");
				facField.setAccessible(true);
				d = (URLStreamHandlerFactory)facField.get(null);
				facField.set(null, null);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
			return d;
		});
		
		URL.setURLStreamHandlerFactory(protocol -> {
			switch(StringUtil.toString(protocol)) {
			case "xspnsf":
				return new URLStreamHandler() {
					@Override
					protected URLConnection openConnection(URL u) throws IOException {
						return NotesURL.getInstance().openConnection(u);
					}
					
				};
			default:
				return delegate.createURLStreamHandler(protocol);
			}
		});
	}
}
