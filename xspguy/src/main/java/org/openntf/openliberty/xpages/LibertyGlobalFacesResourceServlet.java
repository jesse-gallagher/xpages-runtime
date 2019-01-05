package org.openntf.openliberty.xpages;

import javax.servlet.annotation.WebServlet;

import com.ibm.xsp.webapp.DesignerGlobalResourceServlet;

@WebServlet(urlPatterns="/xsp/.ibmxspres/*")
public class LibertyGlobalFacesResourceServlet extends DesignerGlobalResourceServlet {
	private static final long serialVersionUID = 1L;

}
