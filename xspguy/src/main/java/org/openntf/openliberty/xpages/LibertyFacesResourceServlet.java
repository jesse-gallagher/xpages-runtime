package org.openntf.openliberty.xpages;

import javax.servlet.annotation.WebServlet;

import com.ibm.xsp.webapp.FacesResourceServlet;

@WebServlet(urlPatterns="/xsp/*")
public class LibertyFacesResourceServlet extends FacesResourceServlet {
	private static final long serialVersionUID = 1L;

}
