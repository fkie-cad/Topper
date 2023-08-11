package com.topper.tests.interactive;

import static com.topper.tests.utility.BlockingCall.Assert.assertBlocks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.topper.interactive.IOManager;

public class TestIOManager {

	private static final String NL = System.lineSeparator();
	private static final String INPUT_TEXT = "input";
	private static final String INPUT_TEXT_MULTILINE = "1" + NL + NL + "2" + NL + "3" + NL;
	private static final String OUTPUT_TEXT = "output";
	private static final String ERROR_TEXT = "error";
	private static final String QUERY_TEXT = "query";
	private static final String QUERY_TEXT_MULTILINE = "1" + NL + NL + "2" + NL + "3" + NL;
	
	private static final Duration TIMEOUT = Duration.ofSeconds(3);
	
	private static IOManager manager;
	
	private static class TestOutputStream extends OutputStream {

		private StringBuilder content = new StringBuilder();
		@Override
		public final void write(int b) throws IOException {
			this.content.append((char)b);
		}
		
		public final String get() {
			return this.content.toString();
		}
		
		public final void clear() {
			this.content = new StringBuilder();
		}
	}
	
	private static class TestInputStream extends InputStream {
		
		private StringBuilder content = new StringBuilder();
		private boolean simulateBlock = false;
		private boolean interrupted = false;

		@Override
		public int read() throws IOException {
			if (content.length() == 0) {
				if (this.simulateBlock) {
						// Block a bit longer than timeout to avoid races
						try {
							Thread.sleep(TIMEOUT.plusSeconds(1).toMillis());
						} catch (InterruptedException e) {
							this.interrupted = true;
						}
				}
				return -1;
			}
			int c = content.charAt(0);
			content.deleteCharAt(0);
			return c;
		}
		
		public final void fill(final String s) {
			content.append(s);
		}
		
		public final void clear() {
			this.content = new StringBuilder();
		}
		
		public final void setSimulateBlock(final boolean simulateBlock) {
			this.simulateBlock = simulateBlock;
		}
		
		public final boolean wasInterrupted() {
			return this.interrupted;
		}
		
		public final void clearInterrupted() {
			this.interrupted = false;
		}
	}
	
	@BeforeAll
	public static final void createManager() throws FileNotFoundException {
		final InputStream input = new TestInputStream();
		final OutputStream output = new TestOutputStream();
		final OutputStream error = new TestOutputStream();
		
		
		manager = new IOManager(input, output, error);
	}
	
	@AfterAll
	public static final void closeManager() throws IOException {
		manager.getInputStream().close();
		manager.getOutputStream().close();
		manager.getErrorStream().close();
	}
	
	@BeforeEach
	public void clearStreams() {
		((TestInputStream)manager.getInputStream()).clear();
		((TestInputStream)manager.getInputStream()).setSimulateBlock(false);
		((TestInputStream)manager.getInputStream()).clearInterrupted();
		((TestOutputStream)manager.getOutputStream()).clear();
		((TestOutputStream)manager.getErrorStream()).clear();
	}
	
	private static final void writeToInput(final String input) {
		((TestInputStream)manager.getInputStream()).fill(input);
	}
	
	private static final String readFromOutput() {
		return ((TestOutputStream)manager.getOutputStream()).get();
	}
	
	private static final String readFromError() {
		return ((TestOutputStream)manager.getErrorStream()).get();
	}
	
	private static final void enableBlock() {
		((TestInputStream)manager.getInputStream()).setSimulateBlock(true);
	}
	
	private static final boolean wasInterrupted() {
		return ((TestInputStream)manager.getInputStream()).wasInterrupted();
	}
	
	// Method: query
	@Test
	public void Given_Manager_When_QueryTextNullWithInput_Then_NullPointerException() {
		
		// Setup streams
		writeToInput(INPUT_TEXT);
		
		assertThrowsExactly(
				NullPointerException.class,
				() -> manager.query(null)
		);
	}
	
	@Test
	public void Given_Manager_When_QueryTextEmptyWithInput_Then_CorrectQuery() throws IOException {
		
		// Setup streams
		writeToInput(INPUT_TEXT);
		
		final String result = manager.query("");
		assertEquals(INPUT_TEXT, result);
		assertEquals("", readFromOutput());
	}
	
	@Test
	public void Given_Manager_When_QuerySingleLineTextWithInput_Then_CorrectQuery() throws IOException {
		
		// Setup streams
		writeToInput(INPUT_TEXT);
		
		final String result = manager.query(QUERY_TEXT);
		assertEquals(INPUT_TEXT, result);
		assertEquals(QUERY_TEXT, readFromOutput());
	}
	
	@Test
	public void Given_Manager_When_QueryMultiLineTextWithInput_Then_CorrectQuery() throws IOException {
		
		// Setup streams
		writeToInput(INPUT_TEXT);
		
		final String result = manager.query(QUERY_TEXT_MULTILINE);
		assertEquals(INPUT_TEXT, result);
		assertEquals(QUERY_TEXT_MULTILINE, readFromOutput());
	}
	
	// These blocking tests only make sense of in practice the InputStream implementations
	// actually block on read
	@Test
	public void Given_Manager_When_QueryTextEmptyNoInput_Then_Blocking() throws IOException {
		
		enableBlock();
		assertBlocks(TIMEOUT.toMillis(), () -> {
			try {
				manager.query("");
			} catch (final IOException e) {}
		});
		assertTrue(wasInterrupted());
	}
	
	@Test
	public void Given_Manager_When_QueryMultiLineTextNoInput_Then_Blocking() throws IOException {
		
		enableBlock();
		assertBlocks(TIMEOUT.toMillis(), () -> {
			try {
				manager.query(INPUT_TEXT_MULTILINE);
			} catch (final IOException e) {}
		});
		assertTrue(wasInterrupted());
	}
	
	// Method: output
	@Test
	public void Given_Manager_When_OutputTextNull_Then_NullPointerException() {
		
		assertThrowsExactly(NullPointerException.class, () -> manager.output(null));
	}
	
	@Test
	public void Given_Manager_When_OutputEmptyText_Then_CorrectOutput() throws IOException {
		
		manager.output("");
		assertEquals("", readFromOutput());
	}
	
	@Test
	public void Given_Manager_When_OutputSingleLineText_Then_CorrectOutput() throws IOException {
		
		manager.output(OUTPUT_TEXT);
		assertEquals(OUTPUT_TEXT, readFromOutput());
	}
	
	@Test
	public void Given_Manager_When_OutputMultiLineText_Then_CorrectOutput() throws IOException {
		
		manager.output(INPUT_TEXT);
		assertEquals(INPUT_TEXT, readFromOutput());
	}
	
	// Method: error
	@Test
	public void Given_Manager_When_ErrorTextNull_Then_NullPointerException() {
		
		assertThrowsExactly(NullPointerException.class, () -> manager.error(null));
	}
	
	@Test
	public void Given_Manager_When_ErrorEmptyText_Then_CorrectOutput() throws IOException {
		
		manager.error("");
		assertEquals("", readFromError());
	}
	
	@Test
	public void Given_Manager_When_ErrorSingleLineText_Then_CorrectOutput() throws IOException {
		
		manager.error(ERROR_TEXT);
		assertEquals(ERROR_TEXT, readFromError());
	}
	
	@Test
	public void Given_Manager_When_ErrorMultiLineText_Then_CorrectOutput() throws IOException {
		
		manager.error(INPUT_TEXT);
		assertEquals(INPUT_TEXT, readFromError());
	}
	
	// Method: input(count : int) : byte[]
	@Test
	public void Given_Manager_When_InputCountNegative_Then_NegativeArraySizeException() {
		assertThrowsExactly(NegativeArraySizeException.class, () -> manager.input(-1));
	}
	
	@Test
	public void Given_Manager_When_InputCountZeroNoInput_Then_ZeroLengthInput() throws IOException {
		
		final byte[] input = manager.input(0);
		assertEquals(0, input.length);
	}
	
	@Test
	public void Given_Manager_When_InputCountZeroPendingInput_Then_ZeroLengthInput() throws IOException {
		
		writeToInput(INPUT_TEXT);
		final byte[] input = manager.input(0);
		assertEquals(0, input.length);
	}
	
	@Test
	public void Given_Manager_When_InputCountSmallerThanPendingInputSize_Then_PartialInput() throws IOException {
		
		writeToInput(INPUT_TEXT);
		final byte[] input = manager.input(INPUT_TEXT.length() - 1);
		assertEquals(INPUT_TEXT.length() - 1, input.length);
	}
	
	@Test
	public void Given_Manager_When_InputCountBiggerThanPendingInputSize_Then_Blocking() {
		
		enableBlock();
		writeToInput(INPUT_TEXT);
		
		assertBlocks(TIMEOUT.toMillis(), () -> {
			
			try {
				manager.input(INPUT_TEXT.length() + 1);
			} catch (IOException e) {}
		});
		assertTrue(wasInterrupted());
	}
	
	@Test
	public void Given_Manager_When_InputCountEqualsPendingInputSize_Then_AllInput() throws IOException {
		
		writeToInput(INPUT_TEXT);
		final byte[] input = manager.input(INPUT_TEXT.length());
		assertEquals(INPUT_TEXT.length(), input.length);
	}
	
	// Method: inputLine() : String
	@Test
	public void Given_Manager_When_InputLineNoInput_Then_Blocking() {
		
		enableBlock();
		
		assertBlocks(TIMEOUT.toMillis(), () -> {
			
			try {
				manager.inputLine();
			} catch (IOException e) {}
		});
		assertTrue(wasInterrupted());
	}
	
	@Test
	public void Given_Manager_When_InputLineInputWithoutNewline_Then_Blocking() {
		
		enableBlock();
		writeToInput(INPUT_TEXT);
		
		assertBlocks(TIMEOUT.toMillis(), () -> {
			
			try {
				manager.inputLine();
			} catch (IOException e) {}
		});
		assertTrue(wasInterrupted());
	}
	
	@Test
	public void Given_Manager_When_InputLineInputSingleLine_Then_AllInput() throws IOException {
		
		writeToInput(INPUT_TEXT + NL);
		final String line = manager.inputLine();
		assertEquals(INPUT_TEXT, line);
	}
	
	@Test
	public void Given_Manager_When_InputLineInputMultiLine_Then_FirstLine() throws IOException {
		
		final String firstLine = INPUT_TEXT_MULTILINE.split(NL)[0];
		writeToInput(INPUT_TEXT_MULTILINE);
		
		final String line = manager.inputLine();
		assertEquals(firstLine, line);
	}
	
	@Test
	public void Given_Manager_When_InputLineEOF_Then_IOException() {
		
		assertThrowsExactly(IOException.class, () -> manager.inputLine());
	}
}