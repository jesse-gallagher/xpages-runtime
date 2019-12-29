package org.openntf.xpages.runtime.wrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class JakartaPrintWriterWrapper extends PrintWriter {
	private final PrintWriter delegate;

	public JakartaPrintWriterWrapper(PrintWriter delegate) {
		super(delegate);
		this.delegate = delegate;
	}
	
	@Override
	public void close() {
		System.out.println("asked to close!");
	}

}
