package com.topper.sstate;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.configuration.TopperConfig;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;

public final class PicoScriptEnvironment implements Runnable {

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
	
	public PicoScriptEnvironment(@NonNull final TopperConfig config) {
		this.config = config;
		this.state = new SelectionState(this);
		this.session = new SessionInfo();
		
	}
	
	public TopperConfig getConfig() {
		return config;
	}

	public CommandState getState() {
		return state;
	}

	public SessionInfo getSession() {
		return session;
	}

	@Override
	public void run() {
		
	}
}