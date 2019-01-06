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
package org.openntf.openliberty.xpages.xsp;

import com.ibm.xsp.binding.id.ClientIdBindingFactory;
import com.ibm.xsp.binding.javascript.JavaScriptBindingFactoryImpl;
import com.ibm.xsp.binding.xpath.XPathBindingFactoryImpl;
import com.ibm.xsp.designer.context.ServletXSPContextFactory;
import com.ibm.xsp.factory.FactoryLookup;
import com.ibm.xsp.javascript.JavaScriptFactoryImpl;
import com.ibm.xsp.model.ExtsnDataModelFactory;

import java.util.Iterator;

public class LibertyFactoryLookup extends FactoryLookup {
	private final FactoryLookup delegate;

	public LibertyFactoryLookup(FactoryLookup delegate) {
		this.delegate = delegate;

		delegate.setFactory("com.ibm.xsp.XSP_CONTEXT_FACTORY", new ServletXSPContextFactory());
		delegate.setFactory("com.ibm.xsp.EXTSN_DATAMODEL_FACTORY", new ExtsnDataModelFactory());
		delegate.setFactory("com.ibm.xsp.JAVASCRIPT_FACTORY", new JavaScriptFactoryImpl());
		delegate.setFactory("javascript", new JavaScriptBindingFactoryImpl());
		delegate.setFactory("xpath", new XPathBindingFactoryImpl());
		delegate.setFactory("id", new ClientIdBindingFactory());
	}

	@Override
	public Iterator<?> getFactories() {
		return delegate.getFactories();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Iterator<?> getFactories(Class aClass) {
		return delegate.getFactories(aClass);
	}

	@Override
	public Object getFactory(String s) {
		return delegate.getFactory(s);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void setFactory(String s, Class aClass) throws InstantiationException, IllegalAccessException {
		delegate.setFactory(s, aClass);
	}
	
	@Override
	public void setFactory(String s, Object factory) {
		delegate.setFactory(s, factory);
	}
	
	@Override
	public void releaseFactories() {
		delegate.releaseFactories();
	}
}
