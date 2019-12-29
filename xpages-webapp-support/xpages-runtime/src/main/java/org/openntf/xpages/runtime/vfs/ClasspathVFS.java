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
package org.openntf.xpages.runtime.vfs;

import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

import org.openntf.xpages.runtime.util.XSPUtil;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.vfs.VFS;
import com.ibm.commons.vfs.VFSException;
import com.ibm.commons.vfs.VFSFile;
import com.ibm.commons.vfs.VFSFilter.IFilter;
import com.ibm.commons.vfs.VFSFolder;
import com.ibm.designer.runtime.server.ServletExecutionContext;
import com.ibm.designer.runtime.server.util.WarVFS;

/**
 * Implementation of IBM Commons VFS using the current classloader's resource mechanism.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class ClasspathVFS extends VFS {
	private static final boolean DEBUG = false;
	
	private final WarVFS delegate;
	private final ServletExecutionContext context;
	
	/**
	 * Pattern matcher for local XSP classes, assumed to be in the "xsp" package.
	 */
	private static final Pattern XSP_CLASS = Pattern.compile("^xsp/[^/\\.]+\\.class$");
	
	public ClasspathVFS(ServletExecutionContext context) throws VFSException {
		this.delegate = new WarVFS(context);
		this.context = context;
	}

	@Override
	protected FileEntry doCreateFileEntry(VFSFile file) {
		return null;
	}

	@Override
	protected FolderEntry doCreateFolderEntry(VFSFolder folder) {
		return null;
	}

	@Override
	protected VFSFile doCreateVFSFile(String fileName) {
		String localPath = localResolve(fileName);
		if(isBadClassResource(localPath)) {
			return new NopVFSFile(this, fileName);
		}
		URL uri = XSPUtil.getResource(localPath, context.getContextClassLoader());
		return new UrlVFSFile(this, fileName, uri);
	}

	@Override
	protected VFSFolder doCreateVFSFolder(String folderName) {
		return delegate.getFolder(folderName);
	}

	@Override
	protected boolean doIsReadonly() throws VFSException {
		return true;
	}

	@Override
	protected void doReadEntries(VFS vfs, String path, @SuppressWarnings("rawtypes") List result) {
		// Can't assume
	}

	@Override
	protected void doReadResources(String path, @SuppressWarnings("rawtypes") List result, IFilter filter) {
		// Can't assume
		// TODO investigate if we're in a .war file or directory context
		//   Running from Eclipse is tough, as context.getRealPath("/") is just the src/main/webapp dir, while
		//   resource bundles shouldn't go there
	}
	
	@Override
	public boolean hasEntryCache() {
		return true;
	}
	@Override
	protected Entry findEntry(String path) {
		if(DEBUG) {
			System.out.println("findEntry " + path);
		}
		String localPath = localResolve(path);
		if(isBadClassResource(path)) {
			return null;
		}
		return super.findEntry(localPath);
	}
	@Override
	protected FileEntry findFileEntry(String path) {
		if(DEBUG) {
			System.out.println("findFileEntry " + path);
		}
		String localPath = localResolve(path);
		URL uri = null;
		if(!isBadClassResource(path)) {
			uri = XSPUtil.getResource(localPath, context.getContextClassLoader());
		}
		if(uri != null) {
			return super.findFileEntry(path);
		} else {
			try {
				return new DelegateFileEntry(this, delegate.getFile(path));
			} catch (VFSException e) {
				throw new RuntimeException(e);
			}
		}
	}
	@Override
	protected FolderEntry findFolderEntry(String path) {
		if(DEBUG) {
			System.out.println("findFolderEntry " + path);
		}
		VFSFolder folder = delegate.getFolder(path);
		try {
			return new DelegateFolderEntry(this, folder);
		} catch (VFSException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public synchronized void refresh() {
		// NOP
	}
	
	@Override
	protected void doClose() {
		// NOP
	}
	
	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************
	
	// TODO determine if this is needed. It's here to try to get around Badly formed URI "res://WebContent/WEB-INF/classes/xsp/Layout.class", but that is
	//   not successful
	private String localResolve(String path) {
		String p = StringUtil.toString(path);
		if("WebContent/WEB-INF/classes".equals(p)) {
			return ".";
		} else if(p.startsWith("WebContent/WEB-INF/classes/")) {
			return p.substring("WebContent/WEB-INF/classes/".length());
		} else {
			return p;
		}
	}
	
	private boolean isBadClassResource(String path) {
		return path.endsWith(".class") && !XSP_CLASS.matcher(path).matches();
	}

	// *******************************************************************************
	// * Implementation support classes
	// *******************************************************************************
	
	private static class DelegateFileEntry extends FileEntry {
		public DelegateFileEntry(VFS vfs, VFSFile file) throws VFSException {
			super(vfs, file, file.getLastModificationDate());
		}
	}
	
	private static class DelegateFolderEntry extends FolderEntry {
		public DelegateFolderEntry(VFS vfs, VFSFolder folder) throws VFSException {
			super(vfs, folder, folder.getLastModificationDate());
		}
		
	}
}
