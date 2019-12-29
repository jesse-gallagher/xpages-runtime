package org.openntf.xpages.runtime.osgi;

import java.util.List;

import org.osgi.framework.BundleActivator;

public interface ActivatorNameProvider {
	List<Class<? extends BundleActivator>> getClasses();
}
