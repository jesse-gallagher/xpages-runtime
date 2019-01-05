package org.openntf.openliberty.xpages.xsp;

import org.openntf.openliberty.xpages.wrapper.LibertyClassLoaderWrapper;

import com.ibm.xsp.library.LibraryWrapper;

public class LibertyLibraryWrapperWrapper extends LibraryWrapper {
    private final LibraryWrapper delegate;

    public LibertyLibraryWrapperWrapper(LibraryWrapper delegate) {
        super(delegate.getLibraryId(), delegate.getWrapped());
        this.delegate = delegate;
    }

    @Override
    public ClassLoader getClassLoader() {
        return new LibertyClassLoaderWrapper(super.getClassLoader());
    }

    @Override
    public String[] getFacesConfigFiles() {
        return super.getFacesConfigFiles();
    }
}
