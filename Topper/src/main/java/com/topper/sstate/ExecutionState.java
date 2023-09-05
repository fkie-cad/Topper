package com.topper.sstate;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Application state that allows executing the core features of Topper like e.g.
 * analysis - related commands.
 * 
 * @author Pascal Kühnemann
 * @since 05.09.2023
 */
public final class ExecutionState extends CommandState {
	public ExecutionState(@NonNull final CommandContext context) {
		super(context);
	}
}