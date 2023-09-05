package com.topper.noninteractive;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNull;
import org.jline.reader.Parser;
import org.jline.reader.SyntaxError;
import org.jline.reader.impl.DefaultParser;

import com.google.common.io.Files;
import com.topper.commands.PicoTopLevelCommand;
import com.topper.helpers.FileUtil;
import com.topper.sstate.ScriptContext;

import picocli.CommandLine;

/**
 * Manager that handles non - interactive command execution in form of script
 * files. This is a reduced version of {@link InteractiveTopper}, as non -
 * interactive implies that features like auto - complete etc. can be omitted.
 * 
 * Based on <a href=
 * "https://github.com/remkop/picocli/blob/main/picocli-shell-jline3/src/test/java/picocli/shell/jline3/example/Example.java#L132">Picocli
 * and Jline3 example</a>.
 * 
 * @author Pascal Kühnemann
 * @since 05.09.2023
 */
public final class NonInteractiveTopper {

	/**
	 * Interprets all lines in the file referenced by <code>scriptPath</code> as
	 * commands. Lines starting with a <code>'#'</code> are interpreted as comments
	 * and thus ignored.
	 * 
	 * @param context    Execution context of this Topper run.
	 * @param scriptPath Path of the script file, from which to fetch commands.
	 */
	public final void run(@NonNull final ScriptContext context, @NonNull final String scriptPath) {

		// Try opening the file. Performs thorough file checks.
		final File file;
		try {
			file = FileUtil.openIfValid(scriptPath);
		} catch (final IllegalArgumentException e) {
			System.err.println("Script path is invalid: " + e.getMessage());
			return;
		}

		final PicoTopLevelCommand commands = new PicoTopLevelCommand(context);
		final CommandLine cmd = new CommandLine(commands);
		cmd.setTrimQuotes(true);
		cmd.registerConverter(Integer.class, Integer::decode);
		cmd.registerConverter(Integer.TYPE, Integer::decode);

		commands.setOut(new PrintWriter(System.out));

		final Parser parser = new DefaultParser();
		try {
			for (final String line : Files.readLines(file, StandardCharsets.UTF_8)) {
				if (line.startsWith("#")) {
					continue;
				}
				cmd.execute(parser.parse(line, 0).words().toArray(new String[0]));
			}
		} catch (SyntaxError | IOException e) {
			System.err.println("Building terminal failed.");
		} finally {
			// Ensure everything is written.
			commands.out().flush();
		}
	}
}