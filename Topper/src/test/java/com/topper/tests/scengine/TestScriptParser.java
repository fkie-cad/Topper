package com.topper.tests.scengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.topper.exceptions.scripting.IllegalCommandException;
import com.topper.scengine.ScriptParser;
import com.topper.scengine.commands.CommandManager;
import com.topper.scengine.commands.ScriptCommand;
import com.topper.scengine.commands.ScriptCommandParser;
import com.topper.sstate.ScriptContext;

public class TestScriptParser {
	
	private static class TestCommand implements ScriptCommand {
		@Override
		public void execute(ScriptContext context) {
			
		}
	}
	
	private static class TestCommandParser implements ScriptCommandParser {
		@Override
		public ScriptCommand parse(final String[] tokens) {
			//throw new UnsupportedOperationException("TODO: Add tests for new parser/command interface");
			return new TestCommand();
		}

		@Override
		public String usage() {
			return this.command();
		}

		@Override
		public String command() {
			return "test";
		}
	}
	
	private static class OtherCommand implements ScriptCommand {
		@Override
		public void execute(ScriptContext context) {
			
		}
	}
	
	private static class OtherCommandParser implements ScriptCommandParser {
		@Override
		public ScriptCommand parse(final String[] tokens) {
			return new OtherCommand();
		}

		@Override
		public String usage() {
			return this.command();
		}

		@Override
		public String command() {
			return "other";
		}
	}
	
	private static final String NL = System.lineSeparator();
	private static final String COMMAND_TEST_ONLY_NO_SPACE = "testtest";
	private static final String COMMAND_TEST_ONLY = "test test";
	private static final String COMMAND_TEST_ONLY_MULTILINE = "test 1" + NL + "test 2" + NL + "test 3";
	private static final String COMMAND_TEST_ONLY_NOISY_MULTILINE = 
			NL + NL + "test 1     " + NL + NL + "   test    2" + NL + NL + NL + "  test   3  ";
	private static final String COMMAND_MULTI_OPCODE = "test 1" + NL + "other 2" + NL + "test 3" + NL + "other 4";
	private static final String COMMAND_MULTI_OPCODE_NOISY = 
			NL + NL + "test 1     " + NL + NL + "   other 2" + NL + NL + NL + "  test   3  " + NL + " other 4" + NL + NL;
	private static final String COMMAND_ILLEGAL = "ABCDEFG GFEDCBA";
	private static final String COMMAND_EMPTY = "";

	private static final ScriptParser createEmptyParser() {
		final CommandManager manager = CommandManager.get();
		manager.clear();
		return new ScriptParser(manager);
	}
	
	private static final ScriptParser createSingleOnlyParser() {
		final CommandManager manager = CommandManager.get();
		manager.clear();
		manager.registerCommandParser(new TestCommandParser());
		final ScriptParser parser = new ScriptParser(manager);
		return parser;
	}

	private static final ScriptParser createMultiParser() {
		final CommandManager manager = CommandManager.get();
		manager.clear();
		manager.registerCommandParser(new TestCommandParser());
		manager.registerCommandParser(new OtherCommandParser());
		final ScriptParser parser = new ScriptParser(manager);
		return parser;
	}
	
	// Method: parse(script : String) : ImmutableList<ScriptCommand>
	@Test
	public void Given_TestParserRegistered_When_CommandIsTestOnly_Then_CorrectParsing() throws IllegalCommandException {
		
		// Preconditions
		final ScriptParser parser = createSingleOnlyParser();
		
		// State to be tested
		final ImmutableList<ScriptCommand> commands = parser.parse(COMMAND_TEST_ONLY);
		
		// Checks
		assertEquals(1, commands.size());
		assertInstanceOf(TestCommand.class, commands.get(0));
	}
	
	@Test
	public void Given_TestParserRegistered_When_CommandIsNotTest_Then_IllegalCommandException() {
		
		// Preconditions
		final ScriptParser parser = createSingleOnlyParser();
		
		// State to be tested
		assertThrowsExactly(
				IllegalCommandException.class, 
				() -> parser.parse(COMMAND_ILLEGAL)
		);
	}
	
	@Test
	public void Given_TestParserRegistered_When_CommandIsNull_Then_NullPointerException() {
		
		// Preconditions
		final ScriptParser parser = createSingleOnlyParser();
		
		// State to be tested
		assertThrowsExactly(
				NullPointerException.class,
				() -> parser.parse(null)
		);
	}
	
	@Test
	public void Given_TestParserRegistered_When_CommandEmpty_Then_IllegalCommandException() {
		
		// Preconditions
		final ScriptParser parser = createSingleOnlyParser();
		
		// State to be tested
		assertThrowsExactly(
				IllegalCommandException.class, 
				() -> parser.parse(COMMAND_EMPTY)
		);
	}
	
	@Test
	public void Given_TestParser_When_CommandIsTypo_Then_IllegalCommandException() {
		
		// Preconditions
		final ScriptParser parser = createSingleOnlyParser();
		
		// State to be tested
		assertThrowsExactly(
				IllegalCommandException.class, 
				() -> parser.parse(COMMAND_TEST_ONLY_NO_SPACE)
		);
	}
	
	@Test
	public void Given_EmptyParser_When_CommandIsTestOnly_Then_Illegal_Command_Exception() {
		
		// Preconditions
		final ScriptParser parser = createEmptyParser();
		
		// State to be tested
		assertThrowsExactly(
				IllegalCommandException.class,
				() -> parser.parse(COMMAND_TEST_ONLY)
		);
	}
	
	@Test
	public void Given_TestParser_When_CommandIsMultilineTest_Then_CorrectParsing() throws IllegalCommandException {
		
		// Preconditions
		final ScriptParser parser = createSingleOnlyParser();
		
		// State to be tested
		final ImmutableList<ScriptCommand> commands = parser.parse(COMMAND_TEST_ONLY_MULTILINE);
		
		// Checks
		assertEquals(3, commands.size());
		for (final ScriptCommand command : commands) {
			assertInstanceOf(TestCommand.class, command);
		}
	}
	
	@Test
	public void Given_TestParser_When_CommandIsNoisyMultiline_Then_CorrectParsing() throws IllegalCommandException {
		
		// Preconditions
		final ScriptParser parser = createSingleOnlyParser();
		
		// State to be tested
		final ImmutableList<ScriptCommand> commands = parser.parse(COMMAND_TEST_ONLY_NOISY_MULTILINE);
		
		// Checks
		assertEquals(3, commands.size());
		for (final ScriptCommand command : commands) {
			assertInstanceOf(TestCommand.class, command);
		}
	}
	
	@Test
	public void Given_MultiParser_When_CommandIsMultiOpcode_Then_CorrectParsing() throws IllegalCommandException {
		
		// Preconditions
		final ScriptParser parser = createMultiParser();
		
		// State to be tested
		final ImmutableList<ScriptCommand> commands = parser.parse(COMMAND_MULTI_OPCODE);
		
		// Checks
		assertEquals(4, commands.size());
		assertInstanceOf(TestCommand.class, commands.get(0));
		assertInstanceOf(TestCommand.class, commands.get(2));
		assertInstanceOf(OtherCommand.class, commands.get(1));
		assertInstanceOf(OtherCommand.class, commands.get(3));
	}
	
	@Test
	public void Given_MultiParser_When_CommandIsMultiOpcodeNoisy_Then_CorrectParsing() throws IllegalCommandException {
		
		// Preconditions
		final ScriptParser parser = createMultiParser();
		
		// State to be tested
		final ImmutableList<ScriptCommand> commands = parser.parse(COMMAND_MULTI_OPCODE_NOISY);
		
		// Checks
		assertEquals(4, commands.size());
		assertInstanceOf(TestCommand.class, commands.get(0));
		assertInstanceOf(TestCommand.class, commands.get(2));
		assertInstanceOf(OtherCommand.class, commands.get(1));
		assertInstanceOf(OtherCommand.class, commands.get(3));
	}
}