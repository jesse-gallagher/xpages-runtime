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
package org.openntf.xpages.runtime.xsp;

import java.util.ArrayList;
import java.util.List;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.openntf.xpages.runtime.wrapper.JakartaServletContextWrapperWrapper;

import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.application.ApplicationFactoryEx;
import com.ibm.xsp.config.BootStrap;
import com.ibm.xsp.config.ServletContextWrapper;
import com.ibm.xsp.extlib.library.ExtlibLibrary;
import com.ibm.xsp.util.Delegation;

public class JakartaBootStrap extends BootStrap {
    private ServletContextListener contextListener;
    private final ServletContext servletContext;

    public JakartaBootStrap(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void preloadFiles() {
        try {
            List<String> configFiles = this.getConfigFiles();
            List<String> extraFiles = this.getExtraFiles();
            ServletContextWrapper contextWrapper = new JakartaServletContextWrapperWrapper(servletContext, configFiles, extraFiles);
            ServletContextEvent sce = new ServletContextEvent(contextWrapper);
            this.getListener().contextInitialized(sce);
        } catch (Throwable t) {
            throw new FacesExceptionEx(t);
        }
    }

    @Override
    protected void initContext(ServletContext servletContext) throws FacesException {
        try {
            List<String> configFiles = this.getConfigFiles();
            List<String> extraFiles = this.getExtraFiles();
            ServletContextWrapper contextWrapper = new JakartaServletContextWrapperWrapper(servletContext, configFiles, extraFiles);
            ServletContextEvent sce = new ServletContextEvent(contextWrapper);
            this.getListener().contextInitialized(sce);
            Object fac = FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
            if(fac instanceof ApplicationFactoryEx) {
	            ApplicationFactoryEx factory = (ApplicationFactoryEx)fac;
	            factory.initCompleted();
            }
        } catch (Throwable t) {
            throw new FacesExceptionEx(t);
        }
    }

    @Override
    protected void destroyContext(ServletContext servletContext) throws FacesException {
        try {
            ServletContextEvent sce = new ServletContextEvent(servletContext);
            getListener().contextDestroyed(sce);
            FactoryFinder.releaseFactories();
            destroyListener();
        } catch (Throwable t) {
            throw new FacesExceptionEx(t);
        }
    }

    private ServletContextListener getListener() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (this.contextListener == null) {
            this.contextListener = (ServletContextListener)Delegation.getImplementation("context-listener"); //$NON-NLS-1$
        }

        return this.contextListener;
    }

    private void destroyListener() {
        this.contextListener = null;
    }

    @Override
    protected List<String> readConfigFiles() {
    	List<String> result = new ArrayList<String>();
    	for(String f : super.readConfigFiles()) {
//    		if(!f.startsWith("/")) {
//    			result.add("/" + f);
//    		} else {
    			result.add(f);
//    		}
    	}
    	result.add("/META-INF/jakarta-faces-config.xml"); //$NON-NLS-1$
    	
    	return result;
    }
    
    @Override
    protected List<String> readExtraFiles(ServletContext servletContext) {
    	List<String> result = new ArrayList<String>();
    	for(String f : super.readExtraFiles(servletContext)) {
//    		if(!f.startsWith("/")) {
//    			result.add("/" + f);
//    		} else {
    			result.add(f);
//    		}
    	}
    	
    	// Preload files from the ExtLib library as a workaround to https://github.com/darwino/xpages-runtime/issues/19
    	ExtlibLibrary lib = new ExtlibLibrary();
		String[] files = lib.getFacesConfigFiles();
		if(files != null) {
			for(String f : files) {
//				if(!f.startsWith("/")) {
//	    			result.add("/" + f);
//	    		} else {
	    			result.add(f);
//	    		}
			}
		}
    	return result;
    }
}
