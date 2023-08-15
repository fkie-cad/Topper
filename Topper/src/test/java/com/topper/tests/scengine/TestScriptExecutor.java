package com.topper.tests.scengine;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.topper.exceptions.CommandException;
import com.topper.exceptions.InvalidConfigException;
import com.topper.exceptions.InvalidStateTransitionException;
import com.topper.interactive.IOManager;
import com.topper.scengine.ScriptExecutor;
import com.topper.scengine.ScriptParser;
import com.topper.scengine.commands.ExitCommand;
import com.topper.scengine.commands.ExitCommandParser;
import com.topper.scengine.commands.HelpCommand;
import com.topper.scengine.commands.HelpCommandParser;
import com.topper.scengine.commands.ScriptCommand;
import com.topper.sstate.ScriptContext;
import com.topper.tests.utility.TestConfig;

public class TestScriptExecutor {
	
	private static final ImmutableList<ScriptCommand> VALID_COMMANDS = ImmutableList.of(
			new HelpCommand("help"),
			new ExitCommand()
	);
	
	private static ScriptContext VALID_CONTEXT;
	
	private static PrintStream out;
	private static PrintStream err;
	
	private static final ScriptExecutor createExecutor() {
		return new ScriptExecutor();
	}
	
	@BeforeAll
	public static void clearStreams() {
		out = System.out;
		err = System.err;
		System.setOut(new PrintStream(new OutputStream() {
			public void write(int b) {}
		}));
		System.setErr(new PrintStream(new OutputStream() {
			public void write(int b) {}
		}));
	}
	
	@AfterAll
	public static void restoreStreams() {
		System.setOut(out);
		System.setErr(err);
	}
	
	@BeforeEach
	public void init() throws FileNotFoundException, InvalidConfigException {
		
		final ScriptParser parser = new ScriptParser();
		parser.registerParser(new HelpCommandParser());
		parser.registerParser(new ExitCommandParser());
		
		VALID_CONTEXT = new ScriptContext(
			TestConfig.getDefault(), //new TopperConfig(10, Opcode.THROW, 1, 0, 38, false),
			new IOManager(),
			parser
		);
	}
	
//	@Test
//	public void Given_Executor_When_AllNull_Then_NullPointerException() {
//		
//		final ScriptExecutor executor = createExecutor();
//		assertThrowsExactly(NullPointerException.class, () -> executor.execute(null, null));
//	}
//	
//	@Test
//	public void Given_Executor_When_NullContextValidCommand_Then_NullPointerException() {
//		
//		final ScriptExecutor executor = createExecutor();
//		assertThrowsExactly(NullPointerException.class, () -> executor.execute(null, VALID_COMMANDS));
//	}
//	
//	@Test
//	public void Given_Executor_When_ValidContextNullCommands_Then_NullPointerException() {
//		
//		final ScriptExecutor executor = createExecutor();
//		assertThrowsExactly(NullPointerException.class, () -> executor.execute(VALID_CONTEXT, null));
//	}
	
	@Test
	public void Given_Executor_When_AllValid_Then_TerminationState() throws InvalidStateTransitionException, CommandException, IOException {
		
		final ScriptExecutor executor = createExecutor();
		executor.execute(VALID_CONTEXT, VALID_COMMANDS);
		
		assertTrue(VALID_CONTEXT.isTerminationState());
	}
}
