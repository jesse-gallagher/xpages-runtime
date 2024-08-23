/*
 * Copyright © 2019-2024 Jesse Gallagher
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
		setUseSingletonClassLoaders(true);
	}

	public JakartaXspSourceClassLoader(ClassLoader parentClassLoader, List<String> compilerOptions, String[] classPath) {
		super(parentClassLoader, compilerOptions, classPath);
		setUseSingletonClassLoaders(true);
	}

	@Override
	protected SourceFileManager createSourceFileManager(JavaCompiler javaCompiler, DiagnosticCollector<JavaFileObject> diagnostics, String[] classPath, boolean resolve) {
		StandardJavaFileManager standardJavaFileManager = javaCompiler.getStandardFileManager(diagnostics, null, null);
		return new JakartaSourceFileManager(standardJavaFileManager, this, classPath, resolve);
	}
}
