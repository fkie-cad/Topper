package com.topper.scengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.topper.exceptions.IllegalCommandException;
import com.topper.scengine.commands.ScriptCommand;
import com.topper.scengine.commands.ScriptCommandParser;

public final class ScriptParser {

	
	private final Map<String, ScriptCommandParser> parserMap;
	
	/**
	 * 
	 * */
	public ScriptParser() {
		
		this.parserMap = new HashMap<String, ScriptCommandParser>();
	}
	
	public final void registerParser(final String command, final ScriptCommandParser parser) {
		
		if (this.parserMap.containsKey(command.toUpperCase())) {
			throw new IllegalArgumentException(String.format("%s is already registered.", command));
		}
		
		this.parserMap.put(command.toUpperCase(), parser);
	}

	/**
	 * Tries to map the given <code>command</code> string to a respective
	 * <code>ScriptCommandParser</code> class. To that end, the <code>command</code>
	 * is converted to upper - case before trying to map to a class.
	 * 
	 * @param command <code>command</code> string to map to a <code>ScriptCommand</code>.
	 * @return On success the matching <code>ScriptCommandParser</code> is returned. Otherwise
	 * 	<code>null</code>.
	 * @see ScriptCommand
	 * */
	public final ScriptCommandParser findParserByString(final String command) {
		
		if (!this.parserMap.containsKey(command.toUpperCase())) {
			return null;
		}
		
		return this.parserMap.get(command.toUpperCase());
	}
	
	
	/**
	 * Parses <code>script</code> into a list of <code>ScriptCommand</code> objects.
	 * To that end, <code>script</code> is first split on line separators to determine
	 * the individual commands. Then each command is split on a whitespace after
	 * trimming to identify the tokens of a command. The first token of each command
	 * dictates the subclass of <code>ScriptCommand</code> used to hold the data.
	 * 
	 * All commands in <code>script</code> must be valid in order to execute any
	 * commands. This is <b>not</b> an interpreter, which executes all commands
	 * up to an invalid command.
	 * 
	 * @param script <code>String</code> to be parsed into a list of <code>ScriptCommand</code>s.
	 * @return On success, a list of <code>ScriptCommand</code> objects linked
	 * 	to the semantics of <code>script</code>.
	 * @throws IllegalCommandException If <code>script</code> contains at least one
	 * 	invalid command.
	 * */
	public final ImmutableList<ScriptCommand> parse(final String script) throws IllegalCommandException {
		
		// Also get rid of duplicate line separators
		String stripped = script.trim().replaceAll(System.lineSeparator() + "+", System.lineSeparator());
		
		// Split on line separators.
		String[] commands = stripped.split(System.lineSeparator());
		
		// Ensure that all commands are initially correct.
		final List<ScriptCommand> scriptCommands = new ArrayList<ScriptCommand>(commands.length);
		ScriptCommandParser parser;
		String command;
		String[] tokens;
		for (int i = 0; i < commands.length; i++) {
			
			// Commands might contain noisy whitespaces
			command = commands[i].trim().replace(" +", " ");
			
			// Grab actual command tokens
			tokens = command.split(" ");
			
			// Check command length and whether there is a registered parser.
			parser = this.findParserByString(tokens[0].toUpperCase());
			if (command.length() == 0 || parser == null) {
				
				throw new IllegalCommandException(String.format("Line %d contains illegal command: %s", i + 1, tokens[0]));
			}
			
			// Perform thorough parsing based on identified command.
			scriptCommands.add(parser.parse(tokens));
		}
		
		return ImmutableList.copyOf(scriptCommands);
	}
}