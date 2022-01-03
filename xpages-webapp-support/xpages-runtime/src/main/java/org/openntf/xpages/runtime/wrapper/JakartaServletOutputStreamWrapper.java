/*
 * Copyright © 2019-2022 Jesse Gallagher
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
package org.openntf.xpages.runtime.wrapper;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class JakartaServletOutputStreamWrapper extends ServletOutputStream {

	private final ServletOutputStream delegate;
	
	public JakartaServletOutputStreamWrapper(ServletOutputStream delegate) {
		this.delegate = delegate;
	}

	public void close() throws IOException {
//		System.out.println("asked to close!");
//		delegate.close();
	}

	public boolean isReady() {
//		return delegate.isReady();
		return true;
	}

	public void write(int b) throws IOException {
		delegate.write(b);
	}

	public int hashCode() {
		return delegate.hashCode();
	}

	public void write(byte[] b) throws IOException {
		delegate.write(b);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		delegate.write(b, off, len);
	}

	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	public void print(String s) throws IOException {
		delegate.print(s);
	}

	public void flush() throws IOException {
		delegate.flush();
	}

	public void print(boolean b) throws IOException {
		delegate.print(b);
	}

	public void print(char c) throws IOException {
		delegate.print(c);
	}

	public void print(int i) throws IOException {
		delegate.print(i);
	}

	public void print(long l) throws IOException {
		delegate.print(l);
	}

	public void print(float f) throws IOException {
		delegate.print(f);
	}

	public void print(double d) throws IOException {
		delegate.print(d);
	}

	public void println() throws IOException {
		delegate.println();
	}

	public void println(String s) throws IOException {
		delegate.println(s);
	}

	public void println(boolean b) throws IOException {
		delegate.println(b);
	}

	public void println(char c) throws IOException {
		delegate.println(c);
	}

	public String toString() {
		return delegate.toString();
	}

	public void println(int i) throws IOException {
		delegate.println(i);
	}

	public void println(long l) throws IOException {
		delegate.println(l);
	}

	public void println(float f) throws IOException {
		delegate.println(f);
	}

	public void println(double d) throws IOException {
		delegate.println(d);
	}

	public void setWriteListener(WriteListener writeListener) {
		delegate.setWriteListener(writeListener);
	}

}
