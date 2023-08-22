package com.topper.scengine.commands;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.sstate.ScriptContext;

import picocli.CommandLine.Command;

@Command(name = "help", description = "Displays the help message.")
public final class PicoHelpCommand extends PicoScriptCommand {

	@Override
	public final void execute(@NonNull final ScriptContext context) {
		
	}

}