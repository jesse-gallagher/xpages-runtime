package org.openntf.openliberty.xpages.platform;

import com.ibm.commons.Platform;
import com.ibm.commons.platform.IPlatformFactory;

public class LibertyPlatformFactory implements IPlatformFactory {
    @Override
    public Platform createPlatform() {
        return new LibertyPlatform();
    }
}
