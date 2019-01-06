package org.openntf.openliberty.xpages;

import com.ibm.designer.runtime.ApplicationException;
import com.ibm.designer.runtime.server.ServletExecutionContext;

import javax.servlet.ServletContext;

public class LibertyAppExecutionContext extends ServletExecutionContext {
    public LibertyAppExecutionContext(ServletContext servletContext) throws ApplicationException {
        super("Liberty App", "libertyApp", servletContext);
    }
}
