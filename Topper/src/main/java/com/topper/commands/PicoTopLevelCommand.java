package com.topper.commands;

import java.io.PrintWriter;

import org.eclipse.jdt.annotation.NonNull;
import org.jline.reader.LineReader;

import com.topper.commands.attack.PicoAttackCommand;
import com.topper.commands.exit.PicoExitCommand;
import com.topper.commands.file.PicoFileCommand;
import com.topper.commands.list.PicoListCommand;
import com.topper.commands.search.PicoSearchCommand;
import com.topper.sstate.CommandContext;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "", subcommands = { PicoFileCommand.class, PicoExitCommand.class, PicoSearchCommand.class,
		PicoAttackCommand.class, PicoListCommand.class, CommandLine.HelpCommand.class }, description = "Top level command for grouping other commands.")
public final class PicoTopLevelCommand implements Runnable {

	private PrintWriter out;

	@NonNull
	private final CommandContext context;

	public PicoTopLevelCommand(@NonNull final CommandContext context) {
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