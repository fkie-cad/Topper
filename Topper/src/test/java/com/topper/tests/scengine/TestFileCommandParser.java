package com.topper.tests.scengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.junit.jupiter.api.Test;

import com.topper.exceptions.scripting.IllegalCommandException;
import com.topper.scengine.commands.FileCommand;
import com.topper.scengine.commands.FileCommandParser;
import com.topper.scengine.commands.ScriptCommand;

public class TestFileCommandParser {
	
	private static final String COMMAND_FILE = "FILE";
	private static final String INVALID_FILE_PATH = "./src/test/java/resources/abcdefgh";
	private static final String VALID_FILE_NAME = "base.vdex";
	private static final String VALID_FILE_PATH = "./src/test/java/resources/" + VALID_FILE_NAME;
	private static final String[] COMMAND_FILE_NO_ARG = { COMMAND_FILE };
	private static final String[] COMMAND_FILE_TWO_ARGS = { COMMAND_FILE, VALID_FILE_PATH, INVALID_FILE_PATH };
	private static final String[] COMMAND_FILE_INVALID_ARG = { COMMAND_FILE, INVALID_FILE_PATH };
	private static final String[] COMMAND_FILE_VALID_ARG = { COMMAND_FILE, VALID_FILE_PATH };

	private static final FileCommandParser createFileParser() {
		return new FileCommandParser();
	}
	
	@Test
	public void Given_FileParser_When_TokensIsNull_Then_NullPointerException() {
		
		final FileCommandParser parser = createFileParser();
		
		assertThrowsExactly(
				NullPointerException.class,
				() -> parser.parse(null)
		);
	}
	
	@Test
	public void Given_FileParser_When_TokensIsEmpty_Then_IllegalCommandException() {
		
		final FileCommandParser parser = createFileParser();
		
		assertThrowsExactly(
				IllegalCommandException.class,
				() -> parser.parse(new String[0])
		);
	}
	
	@Test
	public void Given_FileParser_When_TokensWithoutArg_Then_IllegalCommandException() {
		
		final FileCommandParser parser = createFileParser();
		
		assertThrowsExactly(
				IllegalCommandException.class,
				() -> parser.parse(COMMAND_FILE_NO_ARG)
		);
	}
	
	@Test
	public void Given_FileParser_When_TokensWithInvalidArg_Then_IllegalCommandException() {
		
		final FileCommandParser parser = createFileParser();
		
		assertThrowsExactly(
				IllegalCommandException.class,
				() -> parser.parse(COMMAND_FILE_INVALID_ARG)
		);
	}
	
	@Test
	public void Given_FileParser_When_TokenWithValidArg_Then_CorrectParsing() throws IllegalCommandException {
		
		final FileCommandParser parser = createFileParser();
		
		final ScriptCommand command = parser.parse(COMMAND_FILE_VALID_ARG);
		
		assertInstanceOf(FileCommand.class, command);
		assertEquals(VALID_FILE_NAME, ((FileCommand)command).getFile().getName());
	}
	
	@Test
	public void Given_FileParser_When_TokenWithTwoArgs_Then_IllegalCommandException() {
		
		final FileCommandParser parser = createFileParser();
		
		assertThrowsExactly(
				IllegalCommandException.class,
				() -> parser.parse(COMMAND_FILE_TWO_ARGS)
		);
	}
}