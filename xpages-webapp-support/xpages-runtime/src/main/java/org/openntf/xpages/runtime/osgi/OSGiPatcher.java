/*
 * Copyright © 2019-2024 Jesse Gallagher
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
package org.openntf.xpages.runtime.osgi;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.osgi.framework.BundleActivator;

import com.ibm.commons.extension.ExtensionManager;

/**
 * This class looks for known OSGi-reliant plugins and initializes them with a mock
 * context to allow them to serve resources without a true OSGi context.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class OSGiPatcher {
	public static void initKnownBundles() {
		try {
			List<ActivatorNameProvider> providers = ExtensionManager.findServices(null, Thread.currentThread().getContextClassLoader(), ActivatorNameProvider.class.getName(), ActivatorNameProvider.class);
			for(ActivatorNameProvider provider : providers) {
				for(Class<? extends BundleActivator> clazz : provider.getClasses()) {
					BundleActivator inst = clazz.newInstance();
					MockBundle mockBundle = new MockBundle(inst);
					MockBundleContext bundleContext = new MockBundleContext(mockBundle);
					mockBundle.setBundleContext(bundleContext);
					inst.start(bundleContext);
				}
			}
			
			// Set the "context" property in InternalPlatform to an empty MockBundle to avoid trouble in file-upload controls
			InternalPlatform platform = InternalPlatform.getDefault();
			Field contextField = InternalPlatform.class.getDeclaredField("context");
			contextField.setAccessible(true);
			contextField.set(platform, new MockBundleContext(null));
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
