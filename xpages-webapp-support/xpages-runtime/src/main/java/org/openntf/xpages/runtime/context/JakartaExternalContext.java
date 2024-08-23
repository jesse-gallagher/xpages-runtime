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
package org.openntf.xpages.runtime.context;

import java.io.InputStream;

import javax.faces.context.ExternalContext;

import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.context.ExternalContextEx;

public class JakartaExternalContext extends ExternalContextEx {
	public JakartaExternalContext(ExternalContext delegate) {
		super(delegate);
	}

	@Override
	public String encodeResourceURL(String resourcePath) {
		String result;
		if(StringUtil.toString(resourcePath).startsWith("/.") || StringUtil.toString(resourcePath).startsWith("/xsp/.")) {
			// Let the parent handle specialized resources
			result = super.encodeResourceURL(resourcePath);
			if(result.startsWith("/xsp/.ibmxspres/")) {
				// The default assumes that global resources will be available from the server root
				result = PathUtil.concat(getRequestContextPath(), result, '/');
			}
		} else {
			// In-app resources don't need an "/xsp" prefix
			result = resourcePath;
		}
		
		return result;
	}
	
	@Override
	public InputStream getResourceAsStream(String res) {
		InputStream is = super.getResourceAsStream(res);
		// Mobile app compatibility
		if(is == null && res != null && res.startsWith("/WEB-INF/")) { //$NON-NLS-1$
			is = super.getResourceAsStream("/DARWINO-INF/" + res.substring("/WEB-INF/".length())); //$NON-NLS-1$ //$NON-NLS-2$
			
			if(is == null) {
				is = super.getResourceAsStream(res.substring("/WEB-INF/".length())); //$NON-NLS-1$
			}
		}
		return is;
	}
	
	
}
