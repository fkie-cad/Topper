package com.topper.interactive;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.exceptions.CommandException;
import com.topper.exceptions.StateException;
import com.topper.scengine.ScriptExecutor;
import com.topper.scengine.ScriptParser;
import com.topper.scengine.commands.ScriptCommand;
import com.topper.sstate.ScriptContext;

/**
 * Manager that will run the main loop, if Topper is started in interactive
 * mode.
 * 
 * @author Pascal Kühnemann
 */
public final class InteractiveTopper {

	/**
	 * Format string to write before requesting user input.
	 */
	@NonNull
	private static final String LINE_PREFIX = "%s> ";

	/**
	 * Main loop for an interactive session. It manages IO as well as command
	 * parsing and execution.
	 * 
	 * @throws IOException If IO in interactive mode fails. This is a fatal error,
	 *                     from which recovery is not possible (probably).
	 */
	public final void mainLoop(@NonNull final IOManager io, @NonNull final ScriptParser parser,
			@NonNull final ScriptExecutor executor, @NonNull final ScriptContext context) throws IOException {

		try {

			// Loop
			String command;
			ImmutableList<ScriptCommand> commands;
			while (!context.isTerminationState()) {

				// TODO: If context has loaded file, then print file name. Otherwise empty
				// string
				io.output(String.format(LINE_PREFIX, ""));

				// Get input line
				command = io.inputLine();

				// Ignore a user spamming enter
				if (command.length() > 0) {

					try {
						// Parse command. Treat each line as a one - line script.
						commands = parser.parse(command);

						// Execute commands. Results are made visible through io
						// and changes in the context.
						executor.execute(context, commands);

					} catch (final CommandException | StateException e) {
						// Distinguishing between output and error allows for
						// discarding error messages in scripts etc.
						io.error(e.getMessage() + System.lineSeparator());
					}

				}

				// Eventually flush all output streams (output and error)
				io.flushAll();
			}

		} finally {
			// Clean up
			io.close();
		}
	}
}