package org.openntf.xpages.runtime.servlet;

import javax.servlet.annotation.WebServlet;

import com.ibm.xsp.webapp.DesignerModuleResourceServlet;

@WebServlet(urlPatterns="/xsp/.ibmmodres/*")
public class ModuleResourceServlet extends DesignerModuleResourceServlet {
	private static final long serialVersionUID = 1L;
}
