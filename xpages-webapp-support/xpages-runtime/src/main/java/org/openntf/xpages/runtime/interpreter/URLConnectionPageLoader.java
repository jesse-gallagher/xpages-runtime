package org.openntf.xpages.runtime.interpreter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;

import javax.faces.FacesException;

import com.ibm.commons.util.io.StreamUtil;
import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.extlib.interpreter.interpreter.parser.AbstractXPagesLoader;
/**
 * Implementation of {@link com.ibm.xsp.extlib.interpreter.interpreter.parser.XPagesLoader XPagesLoader}
 * that reads XPage content using {@link URLConnection}.
 * 
 * @author Jesse Gallagher
 */
public class URLConnectionPageLoader extends AbstractXPagesLoader {
	public URLConnectionPageLoader() {
	}
	
	@Override
	public XPage load(String uri) throws FacesException {
		URLConnection conn = connect(uri);
		try(InputStream is = conn.getInputStream()) {
			Long mod = conn.getLastModified();
			String xspSource = StreamUtil.readString(is);
			return new XPageContent(xspSource, mod);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public boolean isCacheExpired(String uri, Object cacheInfo) throws FacesException {
		URLConnection conn = connect(uri);
		long mod = conn.getLastModified();
		return mod > (Long)cacheInfo;
	}

	private URLConnection connect(String uriString) {
		URI uri = URI.create(uriString);
		try {
			return uri.toURL().openConnection();
		} catch (IOException e) {
			throw new FacesExceptionEx(e);
		}
	}
}
