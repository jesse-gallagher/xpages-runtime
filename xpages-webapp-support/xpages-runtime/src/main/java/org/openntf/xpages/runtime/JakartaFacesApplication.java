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
package org.openntf.xpages.runtime;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.faces.application.Application;
import javax.faces.application.ViewHandler;

import com.ibm.xsp.application.DesignerApplicationEx;
import com.ibm.xsp.factory.FactoryLookup;

@SuppressWarnings("unused")
public class JakartaFacesApplication extends DesignerApplicationEx {
	private static final Logger log = Logger.getLogger(JakartaFacesApplication.class.getName());
	
	private FactoryLookup factoryLookup;
	private ViewHandler viewHandler;

	protected JakartaFacesApplication(Application application) {
		super(application);

//		try {
//			Field factoryLookupField = ApplicationExImpl.class.getDeclaredField("_factoryLookup");
//			factoryLookupField.setAccessible(true);
//			factoryLookupField.set(this, getFactoryLookup());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	
	

//	@Override
//	public synchronized ViewHandler getViewHandler() {
//		if (this.viewHandler == null) {
//			ViewHandler sup = super.getViewHandler();
//			if (!(sup instanceof ViewHandlerEx)) {
//				FacesClassLoader cl = new JakartaFacesClassLoader(getDesignerApplication());
//				FacesPageDriver driver = new JakartaFacesPageDriver(cl);
//				ViewHandlerExImpl result = new ViewHandlerExImpl(sup);
//				result.setPageDriver(driver);
//				this.viewHandler = result;
//			} else {
//				this.viewHandler = sup;
//			}
//		}
//		return this.viewHandler;
//	}

//	@Override
//	public FactoryLookup getFactoryLookup() {
//		if (this.factoryLookup == null) {
//			this.factoryLookup = new JakartaFactoryLookup(super.getFactoryLookup());
//		}
//		return this.factoryLookup;
//	}

//	@Override
//	public String getApplicationProperty(String s, String s1) {
//		String prop = super.getApplicationProperty(s, s1);
//		return prop;
//	}
//
	@Override
	protected ResourceBundle loadResourceBundle(String s, Locale locale) throws IOException {
		ResourceBundle result = super.loadResourceBundle(s, locale);
		if(result == null) {
			result = ResourceBundle.getBundle(s, locale, Thread.currentThread().getContextClassLoader());
		}
		return result;
	}

//	@Override
//	public List<?> findServices(String s) {
//		List<?> svc = super.findServices(s);
//		return svc;
//	}
}
