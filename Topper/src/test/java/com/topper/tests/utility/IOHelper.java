package com.topper.tests.utility;

import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.jdt.annotation.NonNull;

public class IOHelper {

	private static IOHelper instance;
	
	private PrintStream out;
	private PrintStream err;
	
	private IOHelper() {}
	
	public static IOHelper get() {
		if (instance == null) {
			instance = new IOHelper();
		}
		return instance;
	}
	
	public void clearOut() {
		this.out = System.out;
		System.setOut(new PrintStream(new OutputStream() {
			public void write(int b) {}
		}));
	}
	
	public void replaceOut(@NonNull final OutputStream out) {
		this.out = System.out;
		System.setOut(new PrintStream(out));
	}
	
	public void restoreOut() {
		System.setOut(this.out);
	}
	
	public PrintStream getOldOut() {
		return this.out;
	}
	
	public void clearErr() {
		this.err = System.err;
		System.setErr(new PrintStream(new OutputStream() {
			public void write(int b) {}
		}));
	}
	
	public void replaceErr(@NonNull final OutputStream err) {
		this.err = System.err;
		System.setErr(new PrintStream(err));
	}
	
	public void restoreErr() {
		System.setErr(this.err);
	}
	
	public PrintStream getOldErr() {
		return this.err;
	}
}