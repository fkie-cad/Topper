package com.topper.commands;

import java.io.PrintWriter;

import org.eclipse.jdt.annotation.NonNull;
import org.jline.reader.LineReader;

import com.topper.commands.attack.AttackCommand;
import com.topper.commands.exit.ExitCommand;
import com.topper.commands.file.FileCommand;
import com.topper.commands.list.ListCommand;
import com.topper.commands.search.SearchCommand;
import com.topper.sstate.CommandContext;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Top level command used for grouping all commands and providing global
 * information.
 * 
 * This is based on <a href=
 * "https://github.com/remkop/picocli/blob/main/picocli-shell-jline3/src/test/java/picocli/shell/jline3/example/Example.java#L49">Picocli
 * and Jline3 example</a>.
 * 
 * @author Pascal Kühnemann
 * @since 18.09.2023
 */
@Command(name = "", subcommands = { FileCommand.class, ExitCommand.class, SearchCommand.class, AttackCommand.class,
		ListCommand.class,
		CommandLine.HelpCommand.class }, description = "Top level command for grouping other commands.")
public final class TopLevelCommand implements Runnable {

	/**
	 * Current output for all commands.
	 * */
	private PrintWriter out;

	/**
	 * Current {@link CommandContext} of this application.
	 * */
	@NonNull
	private final CommandContext context;

	public TopLevelCommand(@NonNull final CommandContext context) {
		this.context = context;
	}

	/**
	 * Executes this {@link TopLevelCommand}, i.e. lists help information.
	 * */
	@Override
	public final void run() {
		out.println(new CommandLine(this).getUsageMessage());
	}

	/**
	 * Overwrites current output with output from interactive {@link LineReader}.
	 */
	public final void setReader(@NonNull final LineReader reader) {
		this.out = reader.getTerminal().writer();
	}

	/**
	 * Overwrites current output with new <code>out</code>. This can be useful for
	 * redirecting command outputs to a file or special stream.
	 */
	public final void setOut(@NonNull final PrintWriter out) {
		this.out = out;
	}

	/**
	 * Gets current output of this application.
	 */
	public final PrintWriter out() {
		return this.out;
	}

	/**
	 * Gets current {@link CommandContext} of this application.
	 */
	@NonNull
	public final CommandContext getContext() {
		return this.context;
	}
}