package com.topper.tests.scengine;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.junit.jupiter.api.Test;

import com.topper.exceptions.scripting.IllegalCommandException;
import com.topper.scengine.commands.HelpCommandParser;

public class TestHelpCommandParser {

	private static final HelpCommandParser createHelpParser() {
		return new HelpCommandParser();
	}
	
	@Test
	public void Given_FileParser_When_TokensIsNull_Then_NullPointerException() {
		
		final HelpCommandParser parser = createHelpParser();
		
		assertThrowsExactly(
				NullPointerException.class,
				() -> parser.parse(null)
		);
	}
	
	@Test
	public void Given_FileParser_When_TokensIsEmpty_Then_IllegalCommandException() {
		
		final HelpCommandParser parser = createHelpParser();
		
		assertThrowsExactly(
				IllegalCommandException.class,
				() -> parser.parse(new String[0])
		);
	}
}