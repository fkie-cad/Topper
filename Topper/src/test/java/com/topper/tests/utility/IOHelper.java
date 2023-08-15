package com.topper.tests.utility;

import java.io.OutputStream;
import java.io.PrintStream;

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
	
	public void restoreOut() {
		System.setOut(this.out);
	}
	
	public void clearErr() {
		this.err = System.err;
		System.setErr(new PrintStream(new OutputStream() {
			public void write(int b) {}
		}));
	}
	
	public void restoreErr() {
		System.setErr(this.err);
	}
}