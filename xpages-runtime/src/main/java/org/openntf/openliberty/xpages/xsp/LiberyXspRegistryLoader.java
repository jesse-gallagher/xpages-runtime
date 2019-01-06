package org.openntf.openliberty.xpages.xsp;

import com.ibm.xsp.library.LibraryWrapper;
import com.ibm.xsp.registry.config.ServiceRegistryLoader;
import com.ibm.xsp.registry.config.XspRegistryManager;
import com.ibm.xsp.registry.config.XspRegistryProvider;

public class LiberyXspRegistryLoader extends ServiceRegistryLoader {
    @Override
    public void initRegistryManager(XspRegistryManager xspRegistryManager) {
        super.initRegistryManager(xspRegistryManager);
    }

    @Override
    protected XspRegistryProvider createProvider(LibraryWrapper libraryWrapper) {
        return super.createProvider(new LibertyLibraryWrapperWrapper(libraryWrapper));
    }
}
