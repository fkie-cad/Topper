package com.topper.sstate;

import org.eclipse.jdt.annotation.NonNull;

@SuppressWarnings("null")
public abstract class CommandState {
	
	/**
	 * Execution context of this state. It may be changed by commands.
	 * */
	private final ScriptContext context;
	
	/**
	 * Initialize this state.
	 * 
	 * @param context Execution context to assign to this state.
	 * */
	public CommandState(@NonNull final ScriptContext context) {
		this.context = context;
	}

	/**
	 * Gets execution context assigned to this state.
	 * @see ScriptContext
	 * */
	@NonNull
	public final ScriptContext getContext() {
		return this.context;
	}
}