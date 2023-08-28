package com.topper.scengine;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.exceptions.scripting.IllegalCommandException;
import com.topper.scengine.commands.CommandManager;
import com.topper.scengine.commands.ScriptCommand;
import com.topper.scengine.commands.ScriptCommandParser;


/**
 * Parser for scripts consisting of the commands as registered
 * with {@link CommandManager}.
 * 
 * A script consists of multiple lines. Each line represents
 * a single command and arguments. Lines are separated by
 * {@link System#lineSeparator()}. Tokens in a line are
 * separated by spaces (<code>0x20</code>). The first token in a line
 * represents the command to run. It is linked to an instance
 * of {@link ScriptCommandParser} by <code>CommandManager</code>.
 * 
 * If the first token of a line does not match any of the
 * registered commands, then parsing fails for the entire
 * script. This holds for any other parsing error as well.
 * 
 * The string representations of commands are stored in
 * uppercase. Therefore, it is <b>not</b> possible to register
 * <code>"test1"</code> and <code>TEST1</code>, because they
 * are the same after applying <code>String.toUpperCase()</code>.
 * 
 * @author Pascal Kühnemann
 * @see CommandManager
 * @see ScriptCommand
 * @see ScriptCommandParser
 * */
public final class ScriptParser {

	private final CommandManager manager;
	
	/**
	 * Creates a <code>ScriptParser</code>.
	 * */
	public ScriptParser(@NonNull final CommandManager manager) {
		this.manager = manager;
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
	@SuppressWarnings("null")	// ImmutableList.copyOf is not expected to return null
	@NonNull
	public final ImmutableList<@NonNull ScriptCommand> parse(final String script) throws IllegalCommandException {
		
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
			parser = this.manager.findParserByName(this.manager.getRoot(), tokens[0]);
			if (parser == null) {
				throw new IllegalCommandException(String.format("Line %d contains illegal command: %s", i + 1, tokens[0]));
			}
			
			// Perform thorough parsing based on identified command.
			scriptCommands.add(parser.parse(tokens));
		}
		
		return ImmutableList.copyOf(scriptCommands);
	}
}