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
package org.openntf.xpages.runtime.domino.adapter;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionContext;

import javax.servlet.http.HttpSession;

import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpSessionAdapter;

@SuppressWarnings("deprecation")
public class ServletHttpSessionAdapter implements HttpSessionAdapter {
	private final HttpSession delegate;
	
	public ServletHttpSessionAdapter(HttpSession delegate) {
		this.delegate = delegate;
	}

	@Override
	public Object getAttribute(String var1) {
		return delegate.getAttribute(var1);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return delegate.getAttributeNames();
	}

	@Override
	public long getCreationTime() {
		return delegate.getCreationTime();
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public long getLastAccessedTime() {
		return delegate.getLastAccessedTime();
	}

	@Override
	public int getMaxInactiveInterval() {
		return delegate.getMaxInactiveInterval();
	}

	@Override
	public ServletContext getServletContext() {
//		return new LibertyServletContextWrapper(delegate.getServletContext());
		return delegate.getServletContext();
	}

	@Override
	public HttpSessionContext getSessionContext() {
		return delegate.getSessionContext();
	}

	@Override
	public Object getValue(String var1) {
		return delegate.getValue(var1);
	}

	@Override
	public String[] getValueNames() {
		return delegate.getValueNames();
	}

	@Override
	public void invalidate() {
		delegate.invalidate();
	}

	@Override
	public boolean isNew() {
		return delegate.isNew();
	}

	@Override
	public void putValue(String var1, Object var2) {
		delegate.putValue(var1, var2);
	}

	@Override
	public void removeAttribute(String var1) {
		delegate.removeAttribute(var1);
	}

	@Override
	public void removeValue(String var1) {
		delegate.removeValue(var1);
	}

	@Override
	public void setAttribute(String var1, Object var2) {
		delegate.setAttribute(var1, var2);
	}

	@Override
	public void setMaxInactiveInterval(int var1) {
		delegate.setMaxInactiveInterval(var1);
	}

}
