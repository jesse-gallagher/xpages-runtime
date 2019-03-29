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
package org.openntf.xpages.runtime.osgi;

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
			InputStream is = OSGiPatcher.class.getResourceAsStream("/META-INF/platformPlugins.txt");
			try {
				BufferedReader r = new BufferedReader(new InputStreamReader(is));
				try {
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
				} finally {
					r.close();
				}
			} finally {
				is.close();
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
