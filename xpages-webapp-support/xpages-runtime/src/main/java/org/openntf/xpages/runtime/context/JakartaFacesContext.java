/*
 * Copyright © 2019-2021 Jesse Gallagher
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

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import com.ibm.xsp.context.FacesContextExImpl;

public class JakartaFacesContext extends FacesContextExImpl {
	
	private ExternalContext externalContext;

	public JakartaFacesContext(FacesContext delegate) {
		super(delegate);
	}
	
	@Override
	public synchronized ExternalContext getExternalContext() {
		if(this.externalContext == null) {
			this.externalContext = new JakartaExternalContext(super.getExternalContext());
		}
		return this.externalContext;
	}
}
