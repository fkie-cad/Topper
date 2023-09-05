package com.topper.sstate;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Application state representing a terminal state. Once this state is reached,
 * the application can never reach any other {@link CommandState} again. This
 * state signals application termination.
 * 
 * @author Pascal Kühnemann
 * @since 05.09.2023
 */
public final class TerminationState extends CommandState {
	public TerminationState(@NonNull final CommandContext context) {
		super(context);
	}
}