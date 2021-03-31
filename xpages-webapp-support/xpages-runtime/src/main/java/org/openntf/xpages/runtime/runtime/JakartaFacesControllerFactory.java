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
package org.openntf.xpages.runtime.runtime;

import javax.servlet.ServletContext;

import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.controller.FacesController;
import com.ibm.xsp.controller.FacesControllerFactory;

public class JakartaFacesControllerFactory implements FacesControllerFactory {

	@Override
	public FacesController createFacesController(ServletContext servletContext) throws FacesExceptionEx {
		return new JakartaFacesController();
	}

}