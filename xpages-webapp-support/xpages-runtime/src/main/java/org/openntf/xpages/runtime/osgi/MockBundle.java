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
package org.openntf.xpages.runtime.osgi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.openntf.xpages.runtime.util.XSPUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;

public class MockBundle implements Bundle {

    private final Object context;
    private BundleContext bundleContext;

    public MockBundle(Object context) {
        this.context = context;
    }

    @Override
    public URL getResource(String s) {
		return XSPUtil.getResource(s, context.getClass().getClassLoader());
    }

    @Override
    public URL getEntry(String s) {
        return getResource(s);
    }

    @Override
    public Class<?> loadClass(String s) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(s);
    }

    @Override
    public Enumeration<URL> getResources(String s) throws IOException {
		return XSPUtil.getResources(context.getClass().getClassLoader(), s);
    }

    @Override
    public Enumeration<String> getEntryPaths(String s) {
        return null;
    }

    @Override
    public Enumeration<URL> findEntries(String s, String s1, boolean b) {
    	try {
	    	// Not a perfect version, but it'll do
	    	String path = PathUtil.concat(s, s1, '/');
			return getResources(path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }

    @Override
    public BundleContext getBundleContext() {
        return bundleContext;
    }
    
    public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

    @Override
    public Dictionary<String, String> getHeaders() {
    	Manifest mf = getManifest();
        Dictionary<String, String> result = new Hashtable<String, String>();
        for(Map.Entry<Object, Object> entry : mf.getMainAttributes().entrySet()) {
        	result.put(StringUtil.toString(entry.getKey()), StringUtil.toString(entry.getValue()));
        }
        return result;
    }

    @Override
    public Dictionary<String, String> getHeaders(String s) {
    	Manifest mf = getManifest();
        Dictionary<String, String> result = new Hashtable<String, String>();
        for(Map.Entry<Object, Object> entry : mf.getAttributes(s).entrySet()) {
        	result.put(StringUtil.toString(entry.getKey()), StringUtil.toString(entry.getValue()));
        }
        return result;
    }
    
    private Manifest manifest;
    private synchronized Manifest getManifest() {
    	if(this.manifest == null) {
        	// TODO have this look for the specific context class's manifest, which may require parsing URLs
        	String path = context.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        	if(StringUtil.isNotEmpty(path) && path.endsWith(".jar")) {
        		try {
					JarFile f = new JarFile(new File(path));
					try {
						this.manifest = f.getManifest();
					} finally {
						f.close();
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
        	} else {
        		this.manifest = new Manifest();
        		
        		// Check if it's available as a directory
        		Path dir = Paths.get(path);
        		if(Files.isDirectory(dir)) {
        			Path manifest = dir.resolve("META-INF").resolve("MANIFEST.MF");
        			if(Files.isRegularFile(manifest) && Files.isReadable(manifest)) {
        				try(InputStream is = Files.newInputStream(manifest)) {
        					this.manifest.read(is);
        				} catch(IOException e) {
        					throw new RuntimeException(e);
        				}
        			} else {
	        			// It's also possible it's up one level, e.g. in an Eclipse workspace project
	        			manifest = dir.getParent().resolve("MANIFEST.MF");
	        			if(Files.isRegularFile(manifest) && Files.isReadable(manifest)) {
	        				try(InputStream is = Files.newInputStream(manifest)) {
	        					this.manifest.read(is);
	        				} catch(IOException e) {
	        					throw new RuntimeException(e);
	        				}
	        			}
        			}
        		}
        	}
    	}
    	return this.manifest;
    }

    @Override
    public String getSymbolicName() {
    	String name = getHeaders().get("Bundle-SymbolicName");
    	if(StringUtil.isNotEmpty(name)) {
	    	int semiIndex = name.indexOf(";");
	    	if(semiIndex > -1) {
	    		name = name.substring(0, semiIndex);
	    	}
    	}
        return name;
    }

    @Override
    public org.osgi.framework.Version getVersion() {
    	String v = getHeaders().get("Bundle-Version");
    	if(StringUtil.isEmpty(v)) {
    		return null;
    	} else {
    		return new org.osgi.framework.Version(v);
    	}
    }
    
    // *******************************************************************************
	// * Stubbed methods
	// *******************************************************************************

    @Override
    public long getLastModified() {
        return 0;
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public void start(int i) throws BundleException {

    }

    @Override
    public void start() throws BundleException {

    }

    @Override
    public void stop(int i) throws BundleException {

    }

    @Override
    public void stop() throws BundleException {

    }

    @Override
    public void update(InputStream inputStream) throws BundleException {

    }

    @Override
    public void update() throws BundleException {

    }

    @Override
    public void uninstall() throws BundleException {

    }

    @Override
    public long getBundleId() {
        return 0;
    }

    @Override
    public String getLocation() {
        return null;
    }

    @Override
    public ServiceReference<?>[] getRegisteredServices() {
        return new ServiceReference[0];
    }

    @Override
    public ServiceReference<?>[] getServicesInUse() {
        return new ServiceReference[0];
    }

    @Override
    public boolean hasPermission(Object o) {
        return false;
    }

    @Override
    public Map<X509Certificate, List<X509Certificate>> getSignerCertificates(int i) {
        return null;
    }

    @Override
    public <A> A adapt(Class<A> aClass) {
        return null;
    }

    @Override
    public File getDataFile(String s) {
        return null;
    }

    @Override
    public int compareTo(Bundle o) {
        return 0;
    }

}
