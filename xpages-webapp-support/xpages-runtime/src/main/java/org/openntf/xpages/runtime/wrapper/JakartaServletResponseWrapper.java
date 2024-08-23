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
package org.openntf.xpages.runtime.wrapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class JakartaServletResponseWrapper implements HttpServletResponse {
	private final HttpServletResponse delegate;
	private ServletOutputStream os;
	private PrintWriter writer;
	
	public JakartaServletResponseWrapper(HttpServletResponse delegate) {
		this.delegate = delegate;
	}

	public ServletOutputStream getOutputStream() throws IOException {
		if(this.os == null) {
			this.os = new JakartaServletOutputStreamWrapper(delegate.getOutputStream());
		}
		return this.os;
	}

	public PrintWriter getWriter() throws IOException {
		if(this.writer == null) {
			this.writer = new JakartaPrintWriterWrapper(delegate.getWriter());
		}
		return this.writer;
	}

	public void addCookie(Cookie cookie) {
		delegate.addCookie(cookie);
	}

	public boolean containsHeader(String name) {
		return delegate.containsHeader(name);
	}

	public String encodeURL(String url) {
		return delegate.encodeURL(url);
	}

	public String getCharacterEncoding() {
		return delegate.getCharacterEncoding();
	}

	public String encodeRedirectURL(String url) {
		return delegate.encodeRedirectURL(url);
	}

	public String getContentType() {
		return delegate.getContentType();
	}

	@SuppressWarnings("deprecation")
	public String encodeUrl(String url) {
		return delegate.encodeUrl(url);
	}

	@SuppressWarnings("deprecation")
	public String encodeRedirectUrl(String url) {
		return delegate.encodeRedirectUrl(url);
	}

	public void sendError(int sc, String msg) throws IOException {
		delegate.sendError(sc, msg);
	}

	public void sendError(int sc) throws IOException {
		delegate.sendError(sc);
	}

	public void setCharacterEncoding(String charset) {
		delegate.setCharacterEncoding(charset);
	}

	public void sendRedirect(String location) throws IOException {
		delegate.sendRedirect(location);
	}

	public void setDateHeader(String name, long date) {
		delegate.setDateHeader(name, date);
	}

	public void setContentLength(int len) {
		delegate.setContentLength(len);
	}

	public void setContentLengthLong(long len) {
		delegate.setContentLengthLong(len);
	}

	public void addDateHeader(String name, long date) {
		delegate.addDateHeader(name, date);
	}

	public void setContentType(String type) {
		delegate.setContentType(type);
	}

	public void setHeader(String name, String value) {
		delegate.setHeader(name, value);
	}

	public void addHeader(String name, String value) {
		delegate.addHeader(name, value);
	}

	public void setBufferSize(int size) {
		delegate.setBufferSize(size);
	}

	public void setIntHeader(String name, int value) {
		delegate.setIntHeader(name, value);
	}

	public void addIntHeader(String name, int value) {
		delegate.addIntHeader(name, value);
	}

	public void setStatus(int sc) {
		delegate.setStatus(sc);
	}

	public int getBufferSize() {
		return delegate.getBufferSize();
	}

	public void flushBuffer() throws IOException {
		delegate.flushBuffer();
	}

	@SuppressWarnings("deprecation")
	public void setStatus(int sc, String sm) {
		delegate.setStatus(sc, sm);
	}

	public void resetBuffer() {
		delegate.resetBuffer();
	}

	public int getStatus() {
		return delegate.getStatus();
	}

	public boolean isCommitted() {
		return delegate.isCommitted();
	}

	public String getHeader(String name) {
		return delegate.getHeader(name);
	}

	public void reset() {
		delegate.reset();
	}

	public Collection<String> getHeaders(String name) {
		return delegate.getHeaders(name);
	}

	public void setLocale(Locale loc) {
		delegate.setLocale(loc);
	}

	public Collection<String> getHeaderNames() {
		return delegate.getHeaderNames();
	}

	public void setTrailerFields(Supplier<Map<String, String>> supplier) {
		delegate.setTrailerFields(supplier);
	}

	public Locale getLocale() {
		return delegate.getLocale();
	}

	public Supplier<Map<String, String>> getTrailerFields() {
		return delegate.getTrailerFields();
	}

}
