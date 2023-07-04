package com.topper.interactive;

import java.io.IOException;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.TopperConfig;
import com.topper.exceptions.CommandException;
import com.topper.scengine.ExitCommandParser;
import com.topper.scengine.FileCommandParser;
import com.topper.scengine.ScriptCommand;
import com.topper.scengine.ScriptExecutor;
import com.topper.scengine.ScriptParser;
import com.topper.sstate.ScriptContext;

public final class InteractiveTopper {

	private static final String LINE_PREFIX = "%s> ";
	
	private final TopperConfig config;
	
	public InteractiveTopper(final TopperConfig config) {
		this.config = config;
	}
	
	public final TopperConfig getConfig() {
		return this.config;
	}
	
	/**
	 * Main loop for an interactive session. It manages
	 * IO as well as command parsing and execution.
	 * @throws IOException If IO in interactive mode fails.
	 * 	This is a fatal error, from which recovery is not possible (probably).
	 * */
	public final void mainLoop() throws IOException {
		
		// Set up IO
		final IOManager io = new IOManager();
		
		// Create command parser and executor
		final ScriptParser parser = new ScriptParser();
		final ScriptExecutor executor = new ScriptExecutor();
		
		// Register commands with parser
		parser.registerParser("file", new FileCommandParser());
		parser.registerParser("exit", new ExitCommandParser());
		
		// Prepare execution context
		final ScriptContext context = new ScriptContext(this.config, io);
		
		// Loop
		String command;
		ImmutableList<ScriptCommand> commands;
		while (!context.isTerminationState()) {
			
			
			// TODO: If context has loaded file, then print file name. Otherwise empty string
			io.output(String.format(LINE_PREFIX, ""));
			
			// Get input line
			command = io.inputLine();
			
			try {
				
				// Parse command. Treat each line as a one - line script.
				commands = parser.parse(command);
				
				// Execute commands. Results are made visible through io
				// and changes in the context.
				executor.execute(context, commands);
				
			} catch (final CommandException e) {
				// Distinguishing between output and error allows for
				// discarding error messages in scripts etc.
				io.error(e.getMessage() + System.lineSeparator());
			}
		}
		
		// Clean up
		io.close();
	}
}