package com.topper.tests.scengine;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.topper.exceptions.InvalidConfigException;
import com.topper.exceptions.scripting.CommandException;
import com.topper.exceptions.scripting.InvalidStateTransitionException;
import com.topper.interactive.IOManager;
import com.topper.scengine.ScriptExecutor;
import com.topper.scengine.ScriptParser;
import com.topper.scengine.commands.CommandManager;
import com.topper.scengine.commands.ExitCommand;
import com.topper.scengine.commands.ExitCommandParser;
import com.topper.scengine.commands.HelpCommand;
import com.topper.scengine.commands.HelpCommandParser;
import com.topper.scengine.commands.ScriptCommand;
import com.topper.sstate.ScriptContext;
import com.topper.tests.utility.IOHelper;
import com.topper.tests.utility.TestConfig;

public class TestScriptExecutor {
	
	private static final ImmutableList<ScriptCommand> VALID_COMMANDS = ImmutableList.of(
			new HelpCommand("help"),
			new ExitCommand()
	);
	
	private static ScriptContext VALID_CONTEXT;
	
	private static final ScriptExecutor createExecutor() {
		return new ScriptExecutor();
	}
	
	@BeforeAll
	public static void clearStreams() {
		IOHelper.get().clearOut();
		IOHelper.get().clearErr();
		
		// Ensure classes are loaded and static blocks executed.
		new HelpCommandParser();
		new ExitCommandParser();
	}
	
	@AfterAll
	public static void restoreStreams() {
		IOHelper.get().restoreOut();
		IOHelper.get().restoreErr();
	}
	
	@BeforeEach
	public void init() throws FileNotFoundException, InvalidConfigException {
		
		// Clear command manager
		final CommandManager manager = CommandManager.get();
		manager.clear();
		manager.registerCommandParser(new HelpCommandParser());
		manager.registerCommandParser(new ExitCommandParser());
		
		final ScriptParser parser = new ScriptParser(manager);
//		parser.registerParser(new HelpCommandParser());
//		parser.registerParser(new ExitCommandParser());
		
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
