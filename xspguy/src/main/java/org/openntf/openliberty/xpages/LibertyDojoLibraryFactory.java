package org.openntf.openliberty.xpages;

/*     */
/*     */

import com.ibm.commons.Platform;
/*     */ import com.ibm.commons.log.LogMgr;
/*     */ import com.ibm.commons.util.PathUtil;
/*     */ import com.ibm.commons.util.StringUtil;
/*     */ import com.ibm.commons.util.io.FastBufferedInputStream;
/*     */ import com.ibm.xsp.context.DojoLibrary;
/*     */ import com.ibm.xsp.context.DojoLibraryFactory;
/*     */ import com.ibm.xsp.core.Version;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.FilenameFilter;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.List;
/*     */ import java.util.zip.GZIPInputStream;

/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */ public class LibertyDojoLibraryFactory
        /*     */ extends DojoLibraryFactory
        /*     */ {

    public LibertyDojoLibraryFactory() {
        super();
    }
    /*     */   private static InstalledDojoFactory dominoFactory;
    /*     */   public static final String DOJO_PREFIX = "dojo-";

    /*     */
    /*     */
    public static void installDominoFactory(File paramFile)
    /*     */ {
        /*  52 */
        dominoFactory = new InstalledDojoFactory();
        /*  53 */
        dominoFactory.readDojoLibraries(paramFile);
        /*     */
    }

    /*     */
    /*  56 */
    public static InstalledDojoFactory getDominoDojoFactory() {
        return dominoFactory;
    }

    /*     */
    /*     */   private static class InstalledDojoFactory extends DojoLibraryFactory
            /*     */ {
        /*  60 */     private List<DojoLibrary> libraries = new ArrayList();

        /*     */
        /*     */
        public Collection<DojoLibrary> getLibraries()
        /*     */ {
            /*  64 */
            return libraries;
            /*     */
        }

        /*     */
        /*     */
        protected void readDojoLibraries(File paramFile) {
            /*  68 */
            File[] arrayOfFile1 = paramFile.listFiles(new DojoFilter());
            /*  69 */
            if ((arrayOfFile1 != null) && (arrayOfFile1.length != 0)) {
                File[] arrayOfFile2;
                /*  70 */
                int j = (arrayOfFile2 = arrayOfFile1).length;
                for (int i = 0; i < j; i++) {
                    File localFile1 = arrayOfFile2[i];
                    /*  71 */
                    if (localFile1.isDirectory())
                        /*     */ {
                        /*     */
                        /*  74 */
                        String str1 = localFile1.getName();
                        /*     */
                        /*     */
                        /*     */
                        /*  78 */
                        StringParser localStringParser = new StringParser(str1, "dojo-".length());
                        /*  79 */
                        int k = 0;
                        int m = 0;
                        int n = 0;
                        /*  80 */
                        if (localStringParser.isnum()) {
                            /*  81 */
                            k = localStringParser.getInt();
                            /*  82 */
                            if ((localStringParser.match('.')) &&
                                    /*  83 */                 (localStringParser.isnum())) {
                                /*  84 */
                                m = localStringParser.getInt();
                                /*  85 */
                                if ((localStringParser.match('.')) &&
                                        /*  86 */                   (localStringParser.isnum())) {
                                    /*  87 */
                                    n = localStringParser.getInt();
                                    /*     */
                                }
                                /*     */
                            }
                            /*     */
                        }
                        /*     */
                        /*     */
                        /*     */
                        /*     */
                        /*  95 */
                        boolean bool1 = !localStringParser.hasChar();
                        /*     */
                        /*     */
                        /*  98 */
                        Version localVersion = new Version(k, m, n);
                        /*  99 */
                        String str2 = str1.substring("dojo-".length());
                        /*     */
                        /*     */
                        /* 102 */
                        File localFile2 = new File(paramFile, str1);
                        /* 103 */
                        File localFile3 = new File(localFile2, "dojo" + File.separatorChar + "dojo.js.uncompressed.js");
                        /* 104 */
                        boolean bool2 = localFile3.exists();
                        /* 105 */
                        if (bool2) {
                            /* 106 */
                            DominoFileLibrary localDominoFileLibrary = new DominoFileLibrary(localVersion, str2 + "-u", str1, localFile2, bool1, true);
                            /* 107 */
                            libraries.add(localDominoFileLibrary);
                            /*     */
                        }
                        /*     */
                        /* 110 */
                        DominoFileLibrary localDominoFileLibrary = new DominoFileLibrary(localVersion, str2, str1, localFile2, bool1, false);
                        /* 111 */
                        libraries.add(localDominoFileLibrary);
                        /*     */
                    }
                    /*     */
                }
                /*     */
            }
        }

        /*     */
        /*     */     private static class StringParser {
            String s;
            /*     */ int pos;

            /*     */
            /* 119 */       StringParser(String paramString, int paramInt) {
                s = paramString;
                /* 120 */
                pos = paramInt;
                /*     */
            }

            /*     */
            /* 123 */       char get() {
                return pos < s.length() ? s.charAt(pos) : '\000';
            }

            /*     */
            /*     */
            /* 126 */       boolean hasChar() {
                return pos < s.length();
            }

            /*     */
            /*     */       boolean match(char paramChar) {
                /* 129 */
                if (get() == paramChar) {
                    pos += 1;
                    return true;
                }
                /* 130 */
                return false;
                /*     */
            }

            /*     */
            /* 133 */       boolean isnum() {
                int i = get();
                /* 134 */
                return (i >= 48) && (i <= 57);
                /*     */
            }

            /*     */
            /* 137 */       int getInt() {
                if (isnum()) {
                    /* 138 */
                    int i = 0;
                    /*     */
                    do {
                        /* 140 */
                        i = i * 10 + get() - 48;
                        /* 141 */
                        pos += 1;
                        /* 142 */
                    } while (isnum());
                    /* 143 */
                    return i;
                    /*     */
                }
                /* 145 */
                return -1;
                /*     */
            }
            /*     */
        }

        /*     */
        /*     */     private static class DojoFilter implements FilenameFilter {
            /*     */
            public boolean accept(File paramFile, String paramString) {
                /* 151 */
                if ((paramFile == null) || (paramString == null)) {
                    /* 152 */
                    return false;
                    /*     */
                }
                /* 154 */
                if (!paramString.startsWith("dojo-")) {
                    /* 155 */
                    return false;
                    /*     */
                }
                /* 157 */
                return true;
                /*     */
            }
            /*     */
        }

        /*     */
        /*     */
        private int dojoDirectoryVersion(String paramString)
        /*     */ {
            /* 163 */
            String str = paramString.replaceAll("\\D", "");
            /*     */
            /*     */
            /*     */
            /* 167 */
            if (str.length() < 6) {
                /* 168 */
                str = str + "00000".substring(str.length());
                /*     */
            }
            /*     */
            try {
                /* 171 */
                return Integer.parseInt(str);
                /*     */
            }
            /*     */ catch (NumberFormatException localNumberFormatException) {
            }
            /* 174 */
            return -1;
            /*     */
        }
        /*     */
    }

    /*     */
    /*     */
    /*     */
    /*     */
    /*     */
    /* 182 */   private static final boolean DOMINO = Platform.getInstance().isPlatform("Domino");

    /*     */
    /*     */   public static class DominoFileLibrary extends DojoLibrary {
        /*     */     private Version version;
        /*     */     private String versionTag;
        /*     */     private File directory;
        /*     */     private String directoryName;
        /*     */     private String path;
        /*     */     private boolean defaultLibrary;
        /*     */     private boolean ibmModules;
        /*     */     private boolean ibmLayers;
        /*     */     private boolean uncompressed;

        /*     */
        /*     */
        public DominoFileLibrary(Version paramVersion, String paramString1, String paramString2, File paramFile, boolean paramBoolean1, boolean paramBoolean2) {
            /* 196 */
            version = paramVersion;
            /* 197 */
            versionTag = paramString1;
            /* 198 */
            directory = paramFile;
            /* 199 */
            directoryName = paramString2;
            /* 200 */
            path = ("/domjs/" + paramString2);
            /* 201 */
            defaultLibrary = paramBoolean1;
            /* 202 */
            uncompressed = paramBoolean2;
            /* 203 */
            File localFile1 = new File(paramFile, "ibm");
            /* 204 */
            ibmModules = localFile1.exists();
            /* 205 */
            File localFile2 = new File(paramFile, StringUtil.replace("ibm/xsp/widget/layout/layers", '/', File.separatorChar));
            /* 206 */
            ibmLayers = localFile2.exists();
            /*     */
            /* 208 */
            initDojoJsDepends();
            /*     */
        }

        /*     */
        /* 211 */
        public boolean isDefaultLibrary() {
            return defaultLibrary;
        }

        /*     */
        /*     */
        public boolean isDefaultIbmLibrary() {
            /* 214 */
            return ibmModules;
            /*     */
        }

        /*     */
        /* 217 */
        public boolean hasIbmModules() {
            return ibmModules;
        }

        /*     */
        /*     */
        public boolean useIbmLayers() {
            /* 220 */
            return ibmLayers;
            /*     */
        }

        /*     */
        /* 223 */
        public Version getVersion() {
            return version;
        }

        /*     */
        /*     */
        public String getVersionTag() {
            /* 226 */
            return versionTag;
            /*     */
        }

        /*     */
        /* 229 */
        public File getDirectory() {
            return directory;
        }

        /*     */
        /*     */
        public String getDojoDirectoryName() {
            /* 232 */
            return directoryName;
            /*     */
        }

        /*     */
        /* 235 */
        public boolean isUncompressed() {
            return uncompressed;
        }

        /*     */
        /*     */
        public boolean exists(String paramString) {
            /* 238 */
            String str = StringUtil.replace(paramString, '/', File.separatorChar);
            /* 239 */
            File localFile = new File(directory, str);
            /* 240 */
            if (localFile.exists()) {
                /* 241 */
                return true;
                /*     */
            }
            /*     */
            /* 244 */
            localFile = new File(directory, str + ".gz");
            /* 245 */
            if (localFile.exists()) {
                /* 246 */
                return true;
                /*     */
            }
            /* 248 */
            return false;
            /*     */
        }

        /*     */
        /* 251 */
        public InputStream getFileInputStream(String paramString) throws IOException {
            String str1 = StringUtil.replace(paramString, '/', File.separatorChar);
            /*     */
            Object localObject;
            /* 253 */
            File localFile;
            if (isUncompressed()) {
                /* 254 */
                localObject = null;
                /* 255 */
                if (str1.endsWith(".js")) {
                    /* 256 */
                    localObject = str1 + ".uncompressed.js";
                    /* 257 */
                } else if (paramString.endsWith(".js")) {
                    /* 258 */
                    localObject = str1 + ".uncompressed.css";
                    /*     */
                }
                /* 260 */
                if (localObject != null) {
                    /* 261 */
                    localFile = new File(directory, (String) localObject);
                    /* 262 */
                    if (localFile.exists()) {
                        /* 263 */
                        return new FileInputStream(localFile);
                        /*     */
                    }
                    /*     */
                }
                /*     */
            }
            /*     */
            try
                /*     */ {
                /* 269 */
                localObject = new File(directory, str1);
                /* 270 */
                return new FileInputStream((File) localObject);
                /*     */
            }
            /*     */ catch (FileNotFoundException localFileNotFoundException1) {
                /*     */
                try {
                    /* 274 */
                    localFile = new File(directory, str1 + ".gz");
                    /* 275 */
                    return new GZIPInputStream(new FastBufferedInputStream(new FileInputStream(localFile)));
                    /*     */
                } catch (FileNotFoundException localFileNotFoundException2) {
                    String str2;
                    localFileNotFoundException2.printStackTrace();
                }
                /* 291 */
                return null;
                /*     */
            }
        }

        /*     */
        /*     */
        public String getDebugFileName(String paramString) throws IOException {
            /* 295 */
            String str = StringUtil.replace(paramString, '/', File.separatorChar);
            /*     */
            /* 297 */
            if (isUncompressed()) {
                /* 298 */
                Object localObject = null;
                /* 299 */
                if (str.endsWith(".js")) {
                    /* 300 */
                    localObject = str + ".uncompressed.js";
                    /* 301 */
                } else if (paramString.endsWith(".js")) {
                    /* 302 */
                    localObject = str + ".uncompressed.css";
                    /*     */
                }
                /* 304 */
                if (localObject != null) {
                    /* 305 */
                    File localFile = new File(directory, (String) localObject);
                    /* 306 */
                    if (localFile.exists()) {
                        /* 307 */
                        return localFile.getAbsolutePath();
                        /*     */
                    }
                    /*     */
                }
                /*     */
            }
            /*     */
            /* 312 */
            Object localObject = new File(directory, str);
            /* 313 */
            return ((File) localObject).getAbsolutePath();
            /*     */
        }

        /*     */
        /*     */
        public String getResourceUrl(String paramString, boolean paramBoolean) {
            /* 317 */
            if ((LibertyDojoLibraryFactory.DOMINO) && (paramBoolean) && (!uncompressed)) {
                /* 318 */
                String str = PathUtil.concat(path, paramString.substring("/.ibmxspres/dojoroot".length()), '/');
                /* 319 */
                return str;
                /*     */
            }
            /*     */
            /* 322 */
            if (this != DojoLibraryFactory.getDefaultLibrary(isUncompressed()))
                /*     */ {
                /*     */
                /*     */
                /*     */
                /* 327 */
                paramString = "/.ibmxspres/dojoroot-" + versionTag + paramString.substring("/.ibmxspres/dojoroot".length());
                /* 328 */
                return paramString;
                /*     */
            }
            /* 330 */
            if (isUncompressed()) {
                /* 331 */
                paramString = "/.ibmxspres/dojoroot-u" + paramString.substring("/.ibmxspres/dojoroot".length());
                /*     */
            }
            /*     */
            /*     */
            /*     */
            /* 336 */
            return paramString;
            /*     */
        }

        /*     */
        /* 339 */
        public String[] getModulePaths() {
            return null;
        }
        /*     */
    }

    /*     */
    /*     */
    /*     */
    /*     */
    /*     */
    public Collection<DojoLibrary> getLibraries()
    /*     */ {
        /* 347 */
        if (dominoFactory != null) {
            /* 348 */
            return dominoFactory.getLibraries();
            /*     */
        }
        /* 350 */
        return null;
        /*     */
    }
    /*     */
}