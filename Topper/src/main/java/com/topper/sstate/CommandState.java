package com.topper.sstate;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Abstract representation of a state in the Deterministic Finite Automaton that
 * dictates the current state of the entire application.
 * 
 * @author Pascal Kühnemann
 * @since 05.09.2023
 */
public abstract class CommandState {

	/**
	 * Execution context of this state. It may be changed by commands.
	 */
	@NonNull
	private final CommandContext context;

	/**
	 * Initializes this state.
	 * 
	 * @param context Execution context to assign to this state.
	 */
	public CommandState(@NonNull final CommandContext context) {
		this.context = context;
	}

	/**
	 * Gets execution context assigned to this state.
	 * 
	 * @see CommandContext
	 */
	@NonNull
	public final CommandContext getContext() {
		return this.context;
	}
}