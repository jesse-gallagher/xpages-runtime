package org.openntf.xpages.runtime.xsp;

import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import com.ibm.xsp.extlib.javacompiler.JavaSourceClassLoader;
import com.ibm.xsp.extlib.javacompiler.impl.SourceFileManager;

public class JakartaXspSourceClassLoader extends JavaSourceClassLoader {

	public JakartaXspSourceClassLoader(ClassLoader parentClassLoader, List<String> compilerOptions, String[] classPath, boolean resolve) {
		super(parentClassLoader, compilerOptions, classPath, resolve);
	}

	public JakartaXspSourceClassLoader(ClassLoader parentClassLoader, List<String> compilerOptions, String[] classPath) {
		super(parentClassLoader, compilerOptions, classPath);
	}

	@Override
	protected SourceFileManager createSourceFileManager(JavaCompiler javaCompiler, DiagnosticCollector<JavaFileObject> diagnostics, String[] classPath, boolean resolve) {
		StandardJavaFileManager standardJavaFileManager = javaCompiler.getStandardFileManager(diagnostics, null, null);
		return new JakartaSourceFileManager(standardJavaFileManager, this, classPath, resolve);
	}
}
