package org.openntf.xpages.runtime.vfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import com.ibm.commons.vfs.VFS;
import com.ibm.commons.vfs.VFSException;
import com.ibm.commons.vfs.VFSFile;

/**
 * VFSFile implementation for a {@link URL} object.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class UrlVFSFile extends VFSFile {
	private final URL url;
	
	protected UrlVFSFile(VFS vfs, String path, URL url) {
		super(vfs, path);
		this.url = url;
	}

	@Override
	protected InputStream doGetInputStream() throws VFSException {
		try {
			return url.openStream();
		} catch (IOException e) {
			throw new VFSException(e, "Encountered IOException");
		}
	}

	@Override
	protected OutputStream doGetOutputStream(boolean var1) throws VFSException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected long doGetSize() throws VFSException {
		try {
			return url.openConnection().getContentLength();
		} catch (IOException e) {
			throw new VFSException(e, "Encountered IOException");
		}
	}

	@Override
	protected File doGetSystemFile() {
		try {
			return new File(url.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected boolean doExists() throws VFSException {
		return url != null;
	}

	@Override
	protected boolean doIsReadOnly() throws VFSException {
		return true;
	}

	@Override
	protected long doGetLastModificationDate() throws VFSException {
		try {
			return url.openConnection().getLastModified();
		} catch (IOException e) {
			throw new VFSException(e, "Encountered IOException");
		}
	}

	@Override
	protected void doSetLastModificationDate(long mod) throws VFSException {
		
	}

	@Override
	protected List<?> doDelete() throws VFSException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected String doRename(String name) throws VFSException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getPeer() {
		return null;
	}

}
