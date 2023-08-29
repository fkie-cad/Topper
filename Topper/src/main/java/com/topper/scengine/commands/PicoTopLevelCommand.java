package com.topper.scengine.commands;

import java.io.PrintWriter;

import org.eclipse.jdt.annotation.NonNull;
import org.jline.reader.LineReader;

import com.topper.scengine.commands.attack.PicoAttackCommand;
import com.topper.scengine.commands.exit.PicoExitCommand;
import com.topper.scengine.commands.file.PicoFileCommand;
import com.topper.scengine.commands.search.PicoSearchCommand;
import com.topper.sstate.ScriptContext;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "", subcommands = { PicoFileCommand.class, PicoExitCommand.class, PicoSearchCommand.class,
		PicoAttackCommand.class, CommandLine.HelpCommand.class }, description = "Top level command for grouping other commands.")
public final class PicoTopLevelCommand implements Runnable {

	private PrintWriter out;

	@NonNull
	private final ScriptContext context;

	public PicoTopLevelCommand(@NonNull final ScriptContext context) {
		this.context = context;
	}

	@Override
	public final void run() {
		out.println(new CommandLine(this).getUsageMessage());
	}

	public final void setReader(@NonNull final LineReader reader) {
		this.out = reader.getTerminal().writer();
	}

	public final PrintWriter out() {
		return this.out;
	}

	@NonNull
	public final ScriptContext getContext() {
		return this.context;
	}
}