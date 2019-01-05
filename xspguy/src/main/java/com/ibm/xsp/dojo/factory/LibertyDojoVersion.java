package com.ibm.xsp.dojo.factory;

import com.ibm.commons.Platform;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;
import com.ibm.xsp.context.DojoLibrary;
import com.ibm.xsp.context.DojoLibraryFactory;
import com.ibm.xsp.core.Version;
import com.ibm.xsp.dojo.factory.DevDojo;
import com.ibm.xsp.dojo.factory.DojoLibraryActivator;
import com.ibm.xsp.dojo.factory.IbmBundleDojo;
import com.ibm.xsp.model.domino.DominoUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

import java.io.*;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.*;

public class LibertyDojoVersion extends DojoLibraryFactory {
    private static final String DOJOSTREAM = "xsp.dev.dojostream";
    private static boolean isdev;
    private static File devDojoStream;

    public LibertyDojoVersion() {
    }

    private static String findVersionTag() {
        BufferedReader var0 = null;

        String var6;
        try {
            URL var2 = LibertyDojoVersion.class.getResource("/resources/dojo-src-version.txt");
            System.out.println("Got var2 " + var2);
            if (var2 == null) {
                devDojoStream = findDevSourceDir();
                if (devDojoStream != null) {
                    File var3 = new File(devDojoStream, "dojo.01\\dojo-src\\lwp\\version.txt");
                    if (var3.exists()) {
                        isdev = true;
                        var2 = var3.toURL();
                    }
                }
            }

            if (var2 == null) {
                return "0.0.0";
            }

            var0 = new BufferedReader(new InputStreamReader(var2.openStream()));
            String var13 = var0.readLine();
            if (var13 == null || var13.length() <= 0) {
                return "0.0.0";
            }

            int var4 = var13.indexOf(45);
            if (-1 != var4) {
                var13 = var13.substring(0, var4);
            }

            try {
                Version.parseVersion(var13);
                var6 = var13;
            } catch (IllegalArgumentException var10) {
                return "0.0.0";
            }
        } catch (IOException var11) {
            var11.printStackTrace();
            return "0.0.0";
        } finally {
            StreamUtil.close(var0);
        }

        return var6;
    }

    private static File findDevSourceDir() {
        String var0 = Platform.getInstance().getProperty("xsp.dev.dojostream");
        if (StringUtil.isEmpty(var0)) {
            var0 = DominoUtils.getEnvironmentString("xsp.dev.dojostream");
        }

        if (StringUtil.isNotEmpty(var0)) {
            File var1 = new File(var0);
            if (var1.exists()) {
                return var1;
            }
        }

        return null;
    }

    private Bundle mockBundle() {
        return new Bundle() {
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
            public Dictionary<String, String> getHeaders() {
                return null;
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
            public URL getResource(String s) {
                return LibertyDojoVersion.class.getResource(s);
            }

            @Override
            public Dictionary<String, String> getHeaders(String s) {
                return null;
            }

            @Override
            public String getSymbolicName() {
                return null;
            }

            @Override
            public Class<?> loadClass(String s) throws ClassNotFoundException {
                return null;
            }

            @Override
            public Enumeration<URL> getResources(String s) throws IOException {
                return null;
            }

            @Override
            public Enumeration<String> getEntryPaths(String s) {
                return null;
            }

            @Override
            public URL getEntry(String s) {
                return null;
            }

            @Override
            public long getLastModified() {
                return 0;
            }

            @Override
            public Enumeration<URL> findEntries(String s, String s1, boolean b) {
                return null;
            }

            @Override
            public BundleContext getBundleContext() {
                return null;
            }

            @Override
            public Map<X509Certificate, List<X509Certificate>> getSignerCertificates(int i) {
                return null;
            }

            @Override
            public org.osgi.framework.Version getVersion() {
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
        };
    }

    public Collection<DojoLibrary> getLibraries() {
        String var2 = findVersionTag();
        String var3 = "/resources/dojo-version";
        ArrayList var4 = new ArrayList();
        if (isdev) {
            var4.add(new DevDojo(devDojoStream, mockBundle(), var2, var3));
            var4.add(new DevDojo(devDojoStream, mockBundle(), var2 + "-u", var3));
        } else {
            var4.add(new IbmBundleDojo(mockBundle(), var2, var3));
            var4.add(new IbmBundleDojo(mockBundle(), var2 + "-u", var3));
        }

        return var4;
    }
}
