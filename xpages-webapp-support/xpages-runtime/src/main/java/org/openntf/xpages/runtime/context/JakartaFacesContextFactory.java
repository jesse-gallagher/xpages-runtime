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
package org.openntf.xpages.runtime.context;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;

import com.ibm.xsp.context.FacesContextFactoryImpl;

public class JakartaFacesContextFactory extends FacesContextFactoryImpl {
	public JakartaFacesContextFactory() {
		super();
	}
	
	public JakartaFacesContextFactory(FacesContextFactory delegate) {
		super(delegate);
	}
	
	@Override
	public FacesContext getFacesContext(Object var1, Object var2, Object var3, Lifecycle var4) throws FacesException {
		FacesContext context = super.getFacesContext(var1, var2, var3, var4);
		return new JakartaFacesContext(context);
	}
}
