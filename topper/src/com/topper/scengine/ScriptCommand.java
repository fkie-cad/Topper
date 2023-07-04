package com.topper.scengine;

import com.topper.sstate.ScriptContext;

public interface ScriptCommand {

	/**
	 * Execute this command. The semantics of this method depend on
	 * the implementing subclass and the <code>context</code>.
	 * 
	 * @param context Execution context of this command.
	 * */
	void execute(final ScriptContext context);
}