/**
 * Copyright © 2019 Jesse Gallagher
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
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.openntf.xpages.runtime.domino.platform.JakartaDominoPlatform;
import org.openntf.xpages.runtime.wrapper.JakartaServletContextWrapper;

import com.ibm.commons.util.StringUtil;
import com.ibm.domino.napi.c.C;
import com.ibm.domino.xsp.module.nsf.NotesURL;

import lotus.notes.NotesThread;

@WebListener
public class JakartaDominoContextListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		NotesThread.sinitThread();
		C.initLibrary(null);
		
		JakartaDominoPlatform.initContext(new JakartaServletContextWrapper(sce.getServletContext()));
		
		final URLStreamHandlerFactory delegate = AccessController.doPrivileged(new PrivilegedAction<URLStreamHandlerFactory>() {
			@Override
			public URLStreamHandlerFactory run() {
				URLStreamHandlerFactory d;
				try {
					// This is set by the Equinox dependency, which we definitely don't want
					Field facField = URL.class.getDeclaredField("factory");
					facField.setAccessible(true);
					d = (URLStreamHandlerFactory)facField.get(null);
					facField.set(null, null);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
				return d;
			}	
		});
		
		URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
			@Override
			public URLStreamHandler createURLStreamHandler(String protocol) {
				if("xspnsf".equals(StringUtil.toString(protocol))) {
					
					return new URLStreamHandler() {
						@Override
						protected URLConnection openConnection(URL u) throws IOException {
							return NotesURL.getInstance().openConnection(u);
						}
						
					};
				} else {
					return delegate.createURLStreamHandler(protocol);
				}
			}
		});
	}
}
