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

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.openntf.xpages.runtime.platform.JakartaPlatform;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.context.ExternalContextEx;
import com.sun.faces.RIConstants;

public class JakartaExternalContext extends ExternalContextEx {
	public JakartaExternalContext(ExternalContext delegate) {
		super(delegate);
	}

	@Override
	public String encodeResourceURL(String var1) {
		String result = super.encodeResourceURL(var1);
		// Patch around the parent's "/xsp" prefix
		// TODO do this more intelligently
		if(result.startsWith("/xsp/")) {
			result = JakartaPlatform.getServletContext().getContextPath() + result;
		}
		
		pushIfPossibleAndNecessary(result);
		
		return result;
	}
	
	@Override
	public InputStream getResourceAsStream(String res) {
		InputStream is = super.getResourceAsStream(res);
		// Mobile app compatibility
		if(is == null && res != null && res.startsWith("/WEB-INF/")) {
			is = super.getResourceAsStream("/DARWINO-INF/" + res.substring("/WEB-INF/".length()));
			
			if(is == null) {
				is = super.getResourceAsStream(res.substring("/WEB-INF/".length()));
			}
		}
		return is;
	}
	
	// Based on https://github.com/eclipse-ee4j/mojarra/blob/475fb3e88edd2f939d0378c23012277c82d1f7fe/impl/src/main/java/com/sun/faces/context/ExternalContextImpl.java
	// Licensed under the EPL 2.0
	
	private static final String PUSH_SUPPORTED_ATTRIBUTE_NAME = RIConstants.FACES_PREFIX + "ExternalContextImpl.PUSH_SUPPORTED";
	public static final String PUSH_RESOURCE_URLS_KEY_NAME = RIConstants.FACES_PREFIX + "resourceUrls";
	public static final String XSP_ENABLE_PUSH = "xsp.http2.push";
	
	private void pushIfPossibleAndNecessary(String result) {
		FacesContext context = FacesContext.getCurrentInstance();

		if ("false".equals(ApplicationEx.getInstance(context).getProperty(XSP_ENABLE_PUSH, "true"))) {
			return;
		}

		ExternalContext extContext = context.getExternalContext();
		HttpServletRequest req = (HttpServletRequest) extContext.getRequest();
		Object val;

		// 1. check the request cache
		if (null != (val = req.getAttribute(PUSH_SUPPORTED_ATTRIBUTE_NAME))) {
			if (!(Boolean) val) {
				return;
			}
		}

		// 2. Not in the request cache, see if PushBuilder is available in the container
		// Not available in XPages's JSF fork
//        ApplicationAssociate associate = ApplicationAssociate.getInstance(extContext);
//        if (!associate.isPushBuilderSupported()) {
//            // At least we won't have to hit the ApplicationAssociate every time on this request.
//            attrs.putIfAbsent(PUSH_SUPPORTED_ATTRIBUTE_NAME, FALSE);
//            return;
//        }

		// 3. Don't bother trying to push if we've already pushed this URL for this
		// request
		@SuppressWarnings("unchecked")
		Set<String> resourceUrls = (Set<String>) req.getAttribute(PUSH_RESOURCE_URLS_KEY_NAME);
		if (resourceUrls == null) {
			resourceUrls = new HashSet<>();
			req.setAttribute(PUSH_RESOURCE_URLS_KEY_NAME, resourceUrls);
		}
		if (resourceUrls.contains(result)) {
			return;
		}
		resourceUrls.add(result);

		// 4. At this point we know
		// a) the container has PushBuilder
		// b) we haven't pushed this URL for this request before
		Object pbObj = getPushBuilder(context, extContext);
		if (pbObj != null) {
			// and now we also know c) there was no If-Modified-Since header
			((javax.servlet.http.PushBuilder) pbObj).path(result).push();
		}

	}

	private Object getPushBuilder(FacesContext context, ExternalContext extContext) {
		javax.servlet.http.PushBuilder result = null;

		Object requestObj = extContext.getRequest();
		if (requestObj instanceof HttpServletRequest) {
			HttpServletRequest hreq = (HttpServletRequest) requestObj;
			Object val;
			boolean isPushSupported = false;

			// Try to pull value from the request cache
			if ((val = hreq.getAttribute(PUSH_SUPPORTED_ATTRIBUTE_NAME)) != null) {
				isPushSupported = (Boolean) val;
			} else {
				// If the request has an If-Modified-Since header, do not push, since it's
				// possible the resources are already in the cache.
				isPushSupported = StringUtil
						.isEmpty(StringUtil.toString(extContext.getRequestHeaderMap().get("If-Modified-Since")));
			}

			if (isPushSupported) {
				isPushSupported = (result = hreq.newPushBuilder()) != null;
			}
			if (!Collections.list(hreq.getAttributeNames()).contains(PUSH_SUPPORTED_ATTRIBUTE_NAME)) {
				hreq.setAttribute(PUSH_SUPPORTED_ATTRIBUTE_NAME, isPushSupported);
			}
		}

		return result;
	}
}
