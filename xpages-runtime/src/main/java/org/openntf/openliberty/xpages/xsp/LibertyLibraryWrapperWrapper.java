package org.openntf.openliberty.xpages.xsp;

import org.openntf.openliberty.xpages.wrapper.LibertyClassLoaderWrapper;

import com.ibm.xsp.library.LibraryWrapper;

public class LibertyLibraryWrapperWrapper extends LibraryWrapper {
	@SuppressWarnings("deprecation")
	public LibertyLibraryWrapperWrapper(LibraryWrapper delegate) {
		super(delegate.getLibraryId(), delegate.getWrapped());
	}

	@Override
	public ClassLoader getClassLoader() {
		return new LibertyClassLoaderWrapper(super.getClassLoader());
	}
}
