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
