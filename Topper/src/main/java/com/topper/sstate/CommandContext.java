package com.topper.sstate;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.commands.PicoCommand;
import com.topper.configuration.TopperConfig;

/**
 * Execution context of {@link PicoCommand}s. It provides access to resources
 * like {@link TopperConfig} and the current execution {@link CommandState}.
 * 
 * @author Pascal Kühnemann
 * @since 05.09.2023
 */
public final class CommandContext {

	/**
	 * Configuration assigned to this context. It can be used by specific states,
	 * whose registered commands can be configured.
	 */
	@NonNull
	private final TopperConfig config;

	/**
	 * Current state of this Deterministic Finite Automaton (DFA).
	 */
	@NonNull
	private CommandState state;

	/**
	 * Session - specific information including e.g. currently loaded file.
	 */
	@NonNull
	private final Session session;

	/**
	 * Initialize this context. The initial {@link CommandState} is
	 * {@link SelectionState}, requiring a user to select a file before executing
	 * any analysis - related commands.
	 * 
	 * @param config Initial configuration used for this application run.
	 */
	public CommandContext(@NonNull final TopperConfig config) {
		this.config = config;
		this.state = new SelectionState(this);
		this.session = new Session();
	}

	/**
	 * Swaps the current state with <code>newState</code>. This is equivalent to a
	 * state transition in a DFA.
	 * 
	 * @param newState New state to transition to.
	 */
	public final void changeState(@NonNull final CommandState newState) {
		this.state = newState;
	}

	/**
	 * Gets the current {@link CommandState} of the DFA.
	 */
	@NonNull
	public final CommandState getCurrentState() {
		return this.state;
	}

	/**
	 * Gets the {@link TopperConfig} assigned to this context.
	 */
	@NonNull
	public final TopperConfig getConfig() {
		return this.config;
	}

	/**
	 * Gets the current {@link Session}.
	 */
	@NonNull
	public final Session getSession() {
		return this.session;
	}
}