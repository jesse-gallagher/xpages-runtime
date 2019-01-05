package org.openntf.openliberty.xpages.platform;

import com.ibm.commons.ResourceHandler;
import com.ibm.commons.platform.WebAppServerPlatform;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.Application;
import com.ibm.designer.runtime.ApplicationException;
import com.ibm.domino.napi.c.C;
import com.ibm.domino.napi.c.Os;
import com.ibm.domino.xsp.module.nsf.platform.AbstractNotesDominoPlatform;
import com.ibm.domino.xsp.module.nsf.platform.JSDebuggerRuntime;
import com.ibm.jscript.JSContext;
import com.ibm.xsp.context.DojoLibraryFactory;
import com.ibm.xsp.domino.DominoLogger;
import com.ibm.xsp.domino.context.DominoDojo;
import com.ibm.xsp.model.domino.DominoUtils;

import lotus.notes.NotesThread;

import javax.servlet.ServletContext;

import org.openntf.openliberty.xpages.LibertyAppExecutionContext;
import org.openntf.openliberty.xpages.LibertyApplication;

import java.io.File;
import java.util.Properties;

public class LibertyPlatform extends WebAppServerPlatform {
	private static ServletContext servletContext;

	public static void initContext(ServletContext servletContext) {
		LibertyPlatform.servletContext = servletContext;
	}

	public static final String DOMINO_ROOT_PREFIX = "domino";
	public static final String DOMINO_RESOURCE_ROOT = "/.ibmxspres/domino";
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

	public LibertyPlatform() {
		super();
		
		NotesThread.sinitThread();
		C.initLibrary(null);

		installationDirectory = new File(Os.OSGetExecutableDirectory());
		dataDirectory = new File(Os.OSGetDataDirectory());
		sharedDataDirectory = new File(Os.OSGetSharedDataDirectory());
		userDataDirectory = dataDirectory;
		propertiesDirectory = new File(dataDirectory, "properties");
		xspDirectory = new File(installationDirectory, "xsp");
		nsfDirectory = new File(xspDirectory, "nsf");
		styleKitsDirectory = new File(nsfDirectory, "themes");
		serverDirectory = new File(dataDirectory, StringUtil.replace(AbstractNotesDominoPlatform.DOMINO_ROOT_PREFIX + "/java/xsp", '/', File.separatorChar));
		dominoDirectory = new File(dataDirectory, StringUtil.replace(AbstractNotesDominoPlatform.DOMINO_ROOT_PREFIX + "/html", '/', File.separatorChar));
		notesIconsDirectory = new File(dataDirectory, StringUtil.replace(AbstractNotesDominoPlatform.DOMINO_ROOT_PREFIX + "/icons", '/', File.separatorChar));
		jsDirectory = new File(dataDirectory, AbstractNotesDominoPlatform.DOMINO_ROOT_PREFIX + "/js/");
		this.xspProperties = this.loadStaticProperties();
		DominoDojo.installDominoFactory(this.jsDirectory);
		DojoLibraryFactory.initializeLibraries();
		this.initJSEngine();
	}
	
	protected void initJSEngine() {
		JSContext.ENABLE_JSDEBUGGER = this.isJavaDebugEnabled() && this.isJavaScriptDebugEnabled();
		if (JSContext.ENABLE_JSDEBUGGER) {
			if (DominoLogger.CORE.isInfoEnabled()) {
				DominoLogger.CORE.infop(this, "initJSEngine", ResourceHandler.getLoggingString(
						"info.AbstractNotesDominoPlatform.JavaScriptDebuggingisenabledresul"), new Object[0]);
			}

			JSContext.setDebuggerRuntime(new JSDebuggerRuntime());
		}

	}

	protected boolean isJavaDebugEnabled() {
		String var1 = DominoUtils.getEnvironmentString(AbstractNotesDominoPlatform.PROP_JAVADEBUG);
		return var1 != null ? StringUtil.equals(var1.trim(), "1") : false;
	}

	protected boolean isJavaScriptDebugEnabled() {
		String var1 = DominoUtils.getEnvironmentString(AbstractNotesDominoPlatform.PROP_JAVASCRIPTDEBUG);
		return var1 != null ? StringUtil.equals(var1.trim(), "1") : false;
	}

	private Properties loadStaticProperties() {
		return new Properties();
	}

	@Override
	public Object getObject(String s) {
		switch (StringUtil.toString(s)) {
		case "com.ibm.xsp.designer.ApplicationFinder":
			return (Application.IApplicationFinder) () -> {
				try {
					LibertyAppExecutionContext ctx = new LibertyAppExecutionContext(servletContext);
					return new LibertyApplication(ctx);
				} catch (ApplicationException e) {
					throw new RuntimeException(e);
				}
			};
		default:
			return super.getObject(s);
		}
	}

	@Override
	public File getInstallationDirectory() {
		return installationDirectory;
	}

	@Override
	public File getResourcesDirectory() {
		return nsfDirectory;
	}

	public File getGlobalResourceFile(String paramString) {
		File localFile = doGetGlobalResourceFile(paramString);
		return localFile;
	}

	private File doGetGlobalResourceFile(String paramString) {
		File localFile;
		if (paramString.startsWith("/stylekits/")) {
			localFile = new File(styleKitsDirectory, paramString.substring(11));
			return localFile;
		}
		if (paramString.startsWith("/server/")) {
			localFile = new File(serverDirectory, paramString.substring(8));
			return localFile;
		}
		if (paramString.startsWith("/domino/")) {
			localFile = new File(dominoDirectory, paramString.substring(8));
			return localFile;
		}
		if (paramString.startsWith("/global/")) {
			localFile = new File(serverDirectory, paramString.substring(8));
			return localFile;
		}
		if (paramString.startsWith("/properties/")) {

			if (userDataDirectory != null) {
				localFile = new File(userDataDirectory, "properties/" + paramString.substring(12));
				if (localFile.exists()) {
					return localFile;
				}
			}

			if (sharedDataDirectory != null) {
				localFile = new File(sharedDataDirectory, "properties/" + paramString.substring(12));
				if (localFile.exists()) {
					return localFile;
				}
			}

			localFile = new File(propertiesDirectory, paramString.substring(12));
			return localFile;
		}
		if (paramString.startsWith("/icons/")) {
			localFile = new File(notesIconsDirectory, paramString.substring(7));
			return localFile;
		}
		return super.getGlobalResourceFile(paramString);
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
		switch(StringUtil.toString(name)) {
		case "Domino":
			// Sure I am
			return true;
		default:
			return super.isPlatform(name);
		}
	}
	
}
