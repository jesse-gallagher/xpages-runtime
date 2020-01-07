/**
 * Copyright © 2019-2020 Jesse Gallagher
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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.ibm.commons.vfs.VFS;
import com.ibm.commons.vfs.VFSException;
import com.ibm.commons.vfs.VFSFile;

/**
 * No-op VFSFile implementation for invalid requests.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class NopVFSFile extends VFSFile {

	protected NopVFSFile(VFS vfs, String path) {
		super(vfs, path);
	}

	@Override
	protected InputStream doGetInputStream() throws VFSException {
		return null;
	}

	@Override
	protected OutputStream doGetOutputStream(boolean var1) throws VFSException {
		return null;
	}

	@Override
	protected long doGetSize() throws VFSException {
		return 0;
	}

	@Override
	protected File doGetSystemFile() {
		return null;
	}

	@Override
	protected boolean doExists() throws VFSException {
		return false;
	}

	@Override
	protected boolean doIsReadOnly() throws VFSException {
		return true;
	}

	@Override
	protected long doGetLastModificationDate() throws VFSException {
		return 0;
	}

	@Override
	protected void doSetLastModificationDate(long var1) throws VFSException {
		
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected List doDelete() throws VFSException {
		return null;
	}

	@Override
	protected String doRename(String var1) throws VFSException {
		return null;
	}

	@Override
	public Object getPeer() {
		return null;
	}

}
