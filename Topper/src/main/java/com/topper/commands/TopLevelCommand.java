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

@Command(name = "", subcommands = { FileCommand.class, ExitCommand.class, SearchCommand.class,
		AttackCommand.class, ListCommand.class, CommandLine.HelpCommand.class }, description = "Top level command for grouping other commands.")
public final class TopLevelCommand implements Runnable {

	private PrintWriter out;

	@NonNull
	private final CommandContext context;

	public TopLevelCommand(@NonNull final CommandContext context) {
		this.context = context;
	}

	@Override
	public final void run() {
		out.println(new CommandLine(this).getUsageMessage());
	}

	public final void setReader(@NonNull final LineReader reader) {
		this.out = reader.getTerminal().writer();
	}
	
	public final void setOut(@NonNull final PrintWriter out) {
		this.out = out;
	}

	public final PrintWriter out() {
		return this.out;
	}

	@NonNull
	public final CommandContext getContext() {
		return this.context;
	}
}