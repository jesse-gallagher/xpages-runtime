package org.openntf.openliberty.xpages.wrapper;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

public class LibertyServletConfigWrapper implements ServletConfig {
    private final ServletConfig delegate;

    public LibertyServletConfigWrapper(ServletConfig delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getServletName() {
        return delegate.getServletName();
    }

    @Override
    public ServletContext getServletContext() {
        return new LibertyServletContextWrapper(delegate.getServletContext());
    }

    @Override
    public String getInitParameter(String name) {
        return delegate.getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return delegate.getInitParameterNames();
    }
}
