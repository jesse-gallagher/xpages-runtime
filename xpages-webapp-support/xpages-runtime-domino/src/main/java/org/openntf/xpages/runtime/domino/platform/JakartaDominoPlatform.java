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
package org.openntf.xpages.runtime.domino.platform;

import java.io.File;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.openntf.xpages.runtime.JakartaAppExecutionContext;
import org.openntf.xpages.runtime.JakartaApplication;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.Application.IApplicationFinder;
import com.ibm.designer.runtime.ApplicationException;
import com.ibm.domino.napi.c.C;
import com.ibm.domino.napi.c.Os;
import com.ibm.domino.xsp.module.nsf.platform.AbstractNotesDominoPlatform;
import com.ibm.domino.xsp.module.nsf.platform.JSDebuggerRuntime;
import com.ibm.jscript.JSContext;
import com.ibm.xsp.domino.context.DominoDojo;
import com.ibm.xsp.model.domino.DominoUtils;

import lotus.notes.NotesThread;

public class JakartaDominoPlatform extends AbstractNotesDominoPlatform {
	private static ServletContext servletContext;

	public static void initContext(ServletContext servletContext) {
		JakartaDominoPlatform.servletContext = servletContext;
	}

	public static final String DOMINO_ROOT_PREFIX = "domino"; //$NON-NLS-1$
	public static final String DOMINO_RESOURCE_ROOT = "/.ibmxspres/domino"; //$NON-NLS-1$
	private File installationDirectory;
	private File dataDirectory;
	private File sharedDataDirectory;
	private File userDataDirectory;
	private File propertiesDirectory;
	private File xspDirectory;
	private File nsfDirectory;
	private File styleKitsDirectory;
	private File serverDirectory;
	private File dominoDirectory;
	private File notesIconsDirectory;
	private File jsDirectory;
	private Properties xspProperties;

	public JakartaDominoPlatform() {
		NotesThread t = new NotesThread(() -> {
			C.initLibrary(null);
	
			installationDirectory = new File(Os.OSGetExecutableDirectory());
			dataDirectory = new File(Os.OSGetDataDirectory());
			sharedDataDirectory = new File(Os.OSGetSharedDataDirectory());
			userDataDirectory = dataDirectory;
			propertiesDirectory = new File(dataDirectory, "properties"); //$NON-NLS-1$
			xspDirectory = new File(installationDirectory, "xsp"); //$NON-NLS-1$
			nsfDirectory = new File(xspDirectory, "nsf"); //$NON-NLS-1$
			styleKitsDirectory = new File(nsfDirectory, "themes"); //$NON-NLS-1$
			serverDirectory = new File(dataDirectory, StringUtil.replace(AbstractNotesDominoPlatform.DOMINO_ROOT_PREFIX + "/java/xsp", '/', File.separatorChar)); //$NON-NLS-1$
			dominoDirectory = new File(dataDirectory, StringUtil.replace(AbstractNotesDominoPlatform.DOMINO_ROOT_PREFIX + "/html", '/', File.separatorChar)); //$NON-NLS-1$
			notesIconsDirectory = new File(dataDirectory, StringUtil.replace(AbstractNotesDominoPlatform.DOMINO_ROOT_PREFIX + "/icons", '/', File.separatorChar)); //$NON-NLS-1$
			jsDirectory = new File(dataDirectory, AbstractNotesDominoPlatform.DOMINO_ROOT_PREFIX + "/js/"); //$NON-NLS-1$
			this.xspProperties = this.loadStaticProperties();
			DominoDojo.installDominoFactory(this.jsDirectory);
			this.initJSEngine();
		});
		t.run();
		try {
			t.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void initJSEngine() {
		JSContext.ENABLE_JSDEBUGGER = this.isJavaDebugEnabled() && this.isJavaScriptDebugEnabled();
		if (JSContext.ENABLE_JSDEBUGGER) {
			JSContext.setDebuggerRuntime(new JSDebuggerRuntime());
		}

	}

	protected boolean isJavaDebugEnabled() {
		String var1 = DominoUtils.getEnvironmentString(AbstractNotesDominoPlatform.PROP_JAVADEBUG);
		return var1 != null ? StringUtil.equals(var1.trim(), "1") : false; //$NON-NLS-1$
	}

	protected boolean isJavaScriptDebugEnabled() {
		String var1 = DominoUtils.getEnvironmentString(AbstractNotesDominoPlatform.PROP_JAVASCRIPTDEBUG);
		return var1 != null ? StringUtil.equals(var1.trim(), "1") : false; //$NON-NLS-1$
	}

//	@Override
	protected Properties loadStaticProperties() {
		return new Properties();
	}

	@Override
	public Object getObject(String s) {
		if("com.ibm.xsp.designer.ApplicationFinder".equals(s)) { //$NON-NLS-1$
			return (IApplicationFinder) () -> {
				if(app == null) {
					JakartaAppExecutionContext ctx = getAppExecutionContext();
					app = new JakartaApplication(ctx);
				}
				return app;
			};
		} else {
			return super.getObject(s);
		}
	}
	
	private JakartaAppExecutionContext execContext;
	private JakartaApplication app;
	
	private JakartaAppExecutionContext getAppExecutionContext() {
		if(execContext == null) {
			try {
				execContext = new JakartaAppExecutionContext(servletContext);
			} catch (ApplicationException e) {
				throw new RuntimeException(e);
			}
		}
		return execContext;
	}

	@Override
	public File getInstallationDirectory() {
		return installationDirectory;
	}

	@Override
	public File getResourcesDirectory() {
		return nsfDirectory;
	}

	public File getGlobalResourceFile(String path) {
		if(path.startsWith("/stylekits/")) { //$NON-NLS-1$
			return new File(styleKitsDirectory, path.substring("/stylekits/".length())); //$NON-NLS-1$
		}
		if(path.startsWith("/server/")) { //$NON-NLS-1$
			return new File(serverDirectory, path.substring("/server/".length())); //$NON-NLS-1$
		}
		if(path.startsWith("/domino/")) { //$NON-NLS-1$
			return new File(dominoDirectory, path.substring("/domino/".length())); //$NON-NLS-1$
		}
		if(path.startsWith("/global/")) { //$NON-NLS-1$
			return new File(serverDirectory, path.substring("/global/".length())); //$NON-NLS-1$
		}
		if(path.startsWith("/properties/")) { //$NON-NLS-1$

			if (userDataDirectory != null) {
				File localFile = new File(userDataDirectory, "properties/" + path.substring("/properties/".length())); //$NON-NLS-1$ //$NON-NLS-2$
				if (localFile.exists()) {
					return localFile;
				}
			}

			if (sharedDataDirectory != null) {
				File localFile = new File(sharedDataDirectory, "properties/" + path.substring("/properties/".length())); //$NON-NLS-1$ //$NON-NLS-2$
				if (localFile.exists()) {
					return localFile;
				}
			}

			return new File(propertiesDirectory, path.substring("/properties/".length())); //$NON-NLS-1$
		}
		if(path.startsWith("/icons/")) { //$NON-NLS-1$
			return new File(notesIconsDirectory, path.substring("/icons/".length())); //$NON-NLS-1$
		}
		return super.getGlobalResourceFile(path);
	}
	
	@Override
	public String getProperty(String prop) {
		String var2;
		if (this.xspProperties != null) {
			var2 = this.xspProperties.getProperty(prop);
			if (StringUtil.isNotEmpty(var2)) {
				return var2;
			}
		}
		return super.getProperty(prop);
	}
	
	@Override
	public boolean isPlatform(String name) {
		if("Domino".equals(name)) { //$NON-NLS-1$
			// Sure I am
			return true;
		} else {
			return false;
		}
	}

	@Override
	public File getNotesIconsDirectory() {
		return this.notesIconsDirectory;
	}
}
