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
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleActivator;

import com.ibm.commons.util.StringUtil;

public class DefaultActivatorNameProvider implements ActivatorNameProvider {

	@SuppressWarnings("unchecked")
	@Override
	public List<Class<? extends BundleActivator>> getClasses() {
		List<Class<? extends BundleActivator>> result = new ArrayList<Class<? extends BundleActivator>>();
		try {
			InputStream is = OSGiPatcher.class.getResourceAsStream("/META-INF/platformPlugins.txt");
			try {
				BufferedReader r = new BufferedReader(new InputStreamReader(is));
				try {
					String line;
					while((line = r.readLine()) != null) {
						if(StringUtil.isNotEmpty(line) && !line.startsWith("#")) {
							Class<? extends BundleActivator> clazz = (Class<? extends BundleActivator>) Class.forName(line);
							result.add(clazz);
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
		return result;
	}

}
