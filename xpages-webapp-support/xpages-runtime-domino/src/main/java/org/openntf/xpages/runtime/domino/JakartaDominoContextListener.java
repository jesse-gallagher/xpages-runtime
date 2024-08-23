/*
 * Copyright © 2019-2024 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.xpages.runtime.domino;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.security.AccessController;
import java.security.PrivilegedAction;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import org.openntf.xpages.runtime.domino.platform.JakartaDominoPlatform;
import org.openntf.xpages.runtime.wrapper.JakartaServletContextWrapper;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;

import com.ibm.commons.util.StringUtil;
import com.ibm.domino.napi.c.C;
import com.ibm.domino.xsp.module.nsf.NotesURL;

import lotus.notes.NotesThread;

@WebListener
public class JakartaDominoContextListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		NotesThread t = new NotesThread(() -> {
			C.initLibrary(null);
			
			JakartaDominoPlatform.initContext(new JakartaServletContextWrapper(ServletUtil.newToOld(sce.getServletContext())));
			
			final URLStreamHandlerFactory delegate = AccessController.doPrivileged((PrivilegedAction<URLStreamHandlerFactory>) () -> {
				URLStreamHandlerFactory d;
				try {
					// This is set by the Equinox dependency, which we definitely don't want
					Field facField = URL.class.getDeclaredField("factory"); //$NON-NLS-1$
					facField.setAccessible(true);
					d = (URLStreamHandlerFactory)facField.get(null);
					facField.set(null, null);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
				return d;
			});
			
			URL.setURLStreamHandlerFactory(protocol -> {
				if("xspnsf".equals(StringUtil.toString(protocol))) { //$NON-NLS-1$
					return new URLStreamHandler() {
						@Override
						protected URLConnection openConnection(URL u) throws IOException {
							return NotesURL.getInstance().openConnection(u);
						}
					};
				} else {
					return delegate.createURLStreamHandler(protocol);
				}
			});
		});
		t.run();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
