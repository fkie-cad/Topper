package com.topper.tests.scengine;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.TopperConfig;
import com.topper.exceptions.CommandException;
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

public class TestScriptExecutor {
	
	private static final ImmutableList<ScriptCommand> VALID_COMMANDS = ImmutableList.of(
			new HelpCommand("help"),
			new ExitCommand()
	);
	
	private static ScriptContext VALID_CONTEXT;
	
	private static final ScriptExecutor createExecutor() {
		return new ScriptExecutor();
	}
	
	@BeforeEach
	public void init() throws FileNotFoundException {
		
		final ScriptParser parser = new ScriptParser();
		parser.registerParser(new HelpCommandParser());
		parser.registerParser(new ExitCommandParser());
		
		VALID_CONTEXT = new ScriptContext(
			new TopperConfig(),
			new IOManager(),
			parser
		);
	}
	
	@Test
	public void Given_Executor_When_AllNull_Then_NullPointerException() {
		
		final ScriptExecutor executor = createExecutor();
		assertThrowsExactly(NullPointerException.class, () -> executor.execute(null, null));
	}
	
	@Test
	public void Given_Executor_When_NullContextValidCommand_Then_NullPointerException() {
		
		final ScriptExecutor executor = createExecutor();
		assertThrowsExactly(NullPointerException.class, () -> executor.execute(null, VALID_COMMANDS));
	}
	
	@Test
	public void Given_Executor_When_ValidContextNullCommands_Then_NullPointerException() {
		
		final ScriptExecutor executor = createExecutor();
		assertThrowsExactly(NullPointerException.class, () -> executor.execute(VALID_CONTEXT, null));
	}
	
	@Test
	public void Given_Executor_When_AllValid_Then_TerminationState() throws InvalidStateTransitionException, CommandException, IOException {
		
		final ScriptExecutor executor = createExecutor();
		executor.execute(VALID_CONTEXT, VALID_COMMANDS);
		
		assertTrue(VALID_CONTEXT.isTerminationState());
	}
}