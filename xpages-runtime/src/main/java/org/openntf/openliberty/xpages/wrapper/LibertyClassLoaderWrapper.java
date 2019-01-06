package org.openntf.openliberty.xpages.wrapper;

import java.io.InputStream;
import java.net.URL;

public class LibertyClassLoaderWrapper extends ClassLoader {
    public LibertyClassLoaderWrapper(ClassLoader parent) {
        super(parent);
    }

    @Override
    public URL getResource(String name) {
        return super.getResource(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return super.getResourceAsStream(name);
    }
}
