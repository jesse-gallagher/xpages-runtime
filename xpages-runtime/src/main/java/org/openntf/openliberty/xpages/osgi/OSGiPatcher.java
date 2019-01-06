package org.openntf.openliberty.xpages.osgi;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

import org.eclipse.core.runtime.Plugin;

import com.ibm.commons.util.StringUtil;

/**
 * This class looks for known OSGi-reliant plugins and initializes them with a mock
 * context to allow them to serve resources without a true OSGi context.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class OSGiPatcher {
	@SuppressWarnings("unchecked")
	public static void initKnownBundles() {
		try {
			try(InputStream is = OSGiPatcher.class.getResourceAsStream("/META-INF/platformPlugins.txt")) {
				try(BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
					String line;
					while((line = r.readLine()) != null) {
						if(StringUtil.isNotEmpty(line) && !line.startsWith("#")) {
							Class<? extends Plugin> clazz = (Class<? extends Plugin>) Class.forName(line);
							Field instance = clazz.getDeclaredField("instance");
							Plugin inst = clazz.newInstance();
							instance.set(null, inst);
							MockBundle mockBundle = new MockBundle(inst);
							MockBundleContext bundleContext = new MockBundleContext(mockBundle);
							inst.start(bundleContext);
						}
					}
				}
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
