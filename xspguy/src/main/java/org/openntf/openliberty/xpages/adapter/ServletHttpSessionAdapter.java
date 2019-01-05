package org.openntf.openliberty.xpages.adapter;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.http.HttpSession;

import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpSessionAdapter;

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
