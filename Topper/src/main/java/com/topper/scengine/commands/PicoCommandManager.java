package com.topper.scengine.commands;

import org.eclipse.jdt.annotation.NonNull;

import picocli.CommandLine.Model.CommandSpec;

public class PicoCommandManager {

	private static PicoCommandManager instance;
	
	private final CommandSpec root;
	
	private PicoCommandManager() {
		
		root = CommandSpec.create();
	}
	
	@NonNull
	public static final PicoCommandManager get() {
		if (instance == null) {
			instance = new PicoCommandManager();
		}
		return instance;
	}
	
	
	public final void registerCommand(@NonNull final PicoScriptCommand command) {
		final CommandSpec subcommand = CommandSpec.forAnnotatedObject(command);
		root.addSubcommand(null, subcommand);
	}
}