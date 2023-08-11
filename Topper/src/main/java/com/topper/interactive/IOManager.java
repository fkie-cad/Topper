package com.topper.interactive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Manager for all Input/Output - related operations. It abstracts
 * away classical <code>System.out.println</code> statements to
 * streams that are compatible with the above, in order to allow
 * commands to redirect output to files etc.
 * */
public final class IOManager {

	/**
	 * Stream used for taking input. E.g. stdin.
	 * */
	private InputStream inputStream;
	
	/**
	 * Stream used for outputting. E.g. stdout or a file.
	 * */
	private OutputStream outputStream;
	
	/**
	 * Stream used for errors. E.g. stderr or /dev/null.
	 * */
	private OutputStream errorStream;
	
	public IOManager() {
		this(System.in, System.out, System.err);
	}
	
	public IOManager(final InputStream input, final OutputStream output, final OutputStream error) {
		this.inputStream = input;
		this.outputStream = output;
		this.errorStream = error;
	}
	
	/**
	 * Writes <code>queryText</code> to the <code>outputStream</code>
	 * (defaults to stdout) without a line separator and reads in
	 * all characters up input a line separator.
	 * 
	 * This is comparable to python's <code>input</code> function.
	 * 
	 * The default charset is used for receiving input.
	 * 
	 * @param queryText Text to write to <code>outputStream</code>.
	 * @return On success, a string representing (user - ) input
	 * @throws IOException If writing <code>queryText</code> to the
	 * 	<code>outputStream</code> fails.
	 * */
	public final String query(final String queryText) throws IOException {
		
		// Write text to output
		this.output(queryText);
		
		// Read in input until line separator.
		return this.inputLine();
	}
	
	/**
	 * Writes <code>text</code> to the <code>outputStream</code>
	 * without appending a line separator.
	 * 
	 * @param text String to write to <code>outputStream</code>.
	 * @throws IOException If writing <code>test</code> to the
	 * 	<code>outputStream</code> fails.
	 * */
	public final void output(final String text) throws IOException {
		this.outputStream.write(text.getBytes());
	}
	
	/**
	 * Writes <code>text</code> to the <code>errorStream</code>
	 * without appending a line separator.
	 * 
	 * @param text String to write to <code>errorStream</code>.
	 * @throws IOException If writing <code>text</code> to the
	 * 	<code>errorStream</code> fails.
	 * */
	public final void error(final String text) throws IOException {
		this.errorStream.write(text.getBytes());
	}
	
	/**
	 * Reads <code>count</code> bytes from <code>inputStream</code>.
	 * 
	 * This method can be useful for commands that need binary
	 * input to construct e.g. shellcode.
	 * 
	 * @param count Amount of bytes to read from <code>inputStream</code>.
	 * @throws IOException 
	 * @return Array of bytes representing input.
	 * */
	public final byte[] input(int count) throws IOException {
		
		// For count < 0, this should throw
		final byte[] buffer = new byte[count];
		
		// Read from input stream
		final int result = this.inputStream.read(buffer, 0, count);
		if (result == -1) {
			throw new IOException("Hit EOF");
		}
		
		return buffer;
	}
	
	/**
	 * Reads until next line separator and returns everything read
	 * excluding the line separator. Until a line separator is read,
	 * this method blocks the calling thread.
	 * 
	 * The default charset is used.
	 * 
	 * @return Text read from <code>inputStream</code> without line separator.
	 * @throws IOException If reading a line fails.
	 * */
	public final String inputLine() throws IOException {
		final String line =  new BufferedReader(new InputStreamReader(this.inputStream)).readLine();
		if (line == null) {
			throw new IOException("Hit EOF before reading any characters.");
		}
		return line;
	}
	
	/**
	 * Flushes <code>outputStream</code> and <code>errorStream</code>.
	 * */
	public final void flushAll() {
		
		try {
			this.outputStream.flush();
			this.errorStream.flush();
		} catch (IOException ignored) {}
	}
	
	/**
	 * Closes any stream that is neither stdin, stdout nor stderr.
	 * */
	public final void close() {
		
		if (!this.inputStream.equals(System.in)) {
			try {
				this.inputStream.close();
			} catch (IOException ignored) {}
		}
		
		if (!this.outputStream.equals(System.out)) {
			try {
				this.outputStream.close();
			} catch (IOException ignored) {}
		}
		
		if (!this.errorStream.equals(System.err)) {
			try {
				this.errorStream.close();
			} catch (IOException ignored) {}
		}
	}
	
	/**
	 * Set the <code>inputStream</code> of this manager.
	 * 
	 * This can be useful, if a command wants to obtain
	 * input from another source than e.g. stdin.
	 * 
	 * <b>Caution</b>: Overwriting this may break assumptions
	 * made by consecutive commands.
	 * 
	 * @param inputStream New <code>InputStream</code> to
	 * 	use from now on.
	 * */
	public final void setInputStream(final InputStream inputStream) {
		this.inputStream = inputStream;
	}

	/**
	 * Set the <code>outputStream</code> of this manager.
	 * 
	 * This can be useful, if a command wants to dump 
	 * output to another source than e.g. stdout, like a file.
	 * 
	 * <b>Caution</b>: Overwriting this may break assumptions
	 * made by consecutive commands.
	 * 
	 * @param outputStream New <code>OutputStream</code> to
	 * 	use from now on.
	 * */
	public final void setOutputStream(final OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	/**
	 * Set the <code>errorStream</code> of this manager.
	 * 
	 * This can be useful, if a command wants to dump
	 * errors to another source than e.g. stderr, like /dev/null.
	 * 
	 * <b>Caution</b>: Overwriting this may break assumptions
	 * made by consecutive commands.
	 * 
	 * @param outputStream New <code>OutputStream</code> to
	 * 	use from now on.
	 * */
	public final void setErrorStream(final OutputStream errorStream) {
		this.errorStream = errorStream;
	}
	
	/**
	 * Gets the <code>inputStream</code> of this manager.
	 * 
	 * This can be useful for commands that want to use wrapper
	 * implementations for <code>InputStream</code>.
	 * @see InputStream
	 * */
	public final InputStream getInputStream() {
		return this.inputStream;
	}

	/**
	 * Gets the <code>outputStream</code> of this manager.
	 * 
	 * This can be useful for commands that want to use wrapper
	 * implementations for <code>OutputStream</code>.
	 * @see OutputStream
	 * */
	public final OutputStream getOutputStream() {
		return this.outputStream;
	}
	
	/**
	 * Gets the <code>errorStream</code> of this manager.
	 *
	 * This can be useful for commands that want to use wrapper
	 * implementations for <code>OutputStream</code>. The
	 * <code>errorStream</code> is an instance of <code>OutputStream</code>.
	 * @see OutputStream
	 */
	public final OutputStream getErrorStream() {
		return this.errorStream;
	}
}