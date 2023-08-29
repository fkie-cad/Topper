package com.topper.sstate;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.configuration.Config;
import com.topper.configuration.TopperConfig;

public final class ScriptContext {

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
	private final SessionInfo session;

	/**
	 * Initialize this context.
	 * 
	 * @param config Initial configuration used for this application run.
	 */
	public ScriptContext(@NonNull final TopperConfig config) {
		this.config = config;
		this.state = new SelectionState(this);
		this.session = new SessionInfo();
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
	 * Gets the current state of the DFA.
	 * 
	 * @see CommandState
	 */
	@NonNull
	public final CommandState getCurrentState() {
		return this.state;
	}

	/**
	 * Determines whether current state is the <code>TerminationState</code> or not.
	 * 
	 * @return Whether DFA is in <code>TerminationState</code>.
	 */
	public final boolean isTerminationState() {
		return this.state instanceof TerminationState;
	}

	/**
	 * Gets the config assigned to this context.
	 * 
	 * @see Config
	 */
	@NonNull
	public final TopperConfig getConfig() {
		return this.config;
	}
	
	@NonNull
	public final SessionInfo getSession() {
		return this.session;
	}
}