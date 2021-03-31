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
package org.openntf.xpages.runtime.listener;

import com.ibm.xsp.config.BootStrap;
import com.ibm.xsp.config.ConfigureCoreListener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

import org.openntf.xpages.runtime.wrapper.JakartaServletContextWrapper;
import org.openntf.xpages.runtime.xsp.JakartaBootStrap;

@WebListener
public class JakartaConfigureCoreListener extends ConfigureCoreListener {
    BootStrap bootstrap;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext context = new JakartaServletContextWrapper(servletContextEvent.getServletContext());
        bootstrap = new JakartaBootStrap(context);
        bootstrap.init(context);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        bootstrap.destroy(new JakartaServletContextWrapper(servletContextEvent.getServletContext()));
    }
}
