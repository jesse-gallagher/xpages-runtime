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
package org.openntf.xpages.runtime.servlet;

import org.openntf.xpages.runtime.runtime.JakartaFacesControllerFactory;

import com.ibm.xsp.controller.FacesControllerFactory;
import com.ibm.xsp.webapp.DesignerFacesServlet;

public class JakartaFacesServlet extends DesignerFacesServlet {
	private final FacesControllerFactory factory = new JakartaFacesControllerFactory();
	
	@Override
	protected FacesControllerFactory getFacesControllerFactory() {
		return factory;
	}
}
