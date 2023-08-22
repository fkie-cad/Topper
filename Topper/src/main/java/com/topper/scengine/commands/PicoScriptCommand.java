package com.topper.scengine.commands;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.sstate.ScriptContext;

import picocli.CommandLine.Model.CommandSpec;

public abstract class PicoScriptCommand implements Runnable {

	public PicoScriptCommand() {
		PicoCommandManager.get().registerCommand(this);
	}
	
	public abstract void execute(@NonNull final ScriptContext context);
	
	@Override
	public void run() {
		
		this.execute(???);
	}
}