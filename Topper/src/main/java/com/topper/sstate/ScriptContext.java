package com.topper.sstate;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.configuration.Config;
import com.topper.configuration.TopperConfig;
import com.topper.interactive.IOManager;
import com.topper.scengine.ScriptParser;

public final class ScriptContext {

	/**
	 * Configuration assigned to this context. It can be used by
	 * specific states, whose registered commands can be configured.
	 * */
	@NonNull
	private final TopperConfig config;
	
	/**
	 * Current state of this Deterministic Finite Automaton (DFA).
	 * */
	@NonNull
	private CommandState state;
	
	/**
	 * Input / Output manager providing commands the ability to
	 * e.g. write to and read from console.
	 * */
	@NonNull
	private final IOManager io;
	
	/**
	 * Parser for multi - line scripts. The <code>HelpCommand</code>
	 * uses it to extract command information.
	 * */
	@NonNull
	private final ScriptParser parser;
	
	/**
	 * Initialize this context.
	 * 
	 * @param config Initial configuration used for this application run.
	 * @param io Input/Output manager used by commands to interact with user.
	 * @param parser Parser used to extract information on commands.
	 * */
	public ScriptContext(@NonNull final TopperConfig config, @NonNull final IOManager io, @NonNull final ScriptParser parser) {
		this.config = config;
		this.io = io;
		this.parser = parser;
		this.state = new SelectionState(this);
	}
	
	/**
	 * Swaps the current state with <code>newState</code>. This
	 * is equivalent to a state transition in a DFA.
	 * 
	 * @param newState New state to transition to.
	 * */
	public final void changeState(@NonNull final CommandState newState) {
		this.state = newState;
	}
	
	/**
	 * Gets the current state of the DFA.
	 * @see CommandState
	 * */
	@NonNull
	public final CommandState getCurrentState() {
		return this.state;
	}
	
	/**
	 * Determines whether current state is the <code>TerminationState</code>
	 * or not.
	 * 
	 * @return Whether DFA is in <code>TerminationState</code>.
	 * */
	public final boolean isTerminationState() {
		return this.state instanceof TerminationState;
	}
	
	/**
	 * Gets the config assigned to this context.
	 * @see Config
	 * */
	@NonNull
	public final TopperConfig getConfig() {
		return this.config;
	}
	
	/**
	 * Gets the Input/Output manager used for interacting
	 * with the user.
	 * @see IOManager
	 * */
	@NonNull
	public final IOManager getIO() {
		return this.io;
	}
	
	/**
	 * Gets the parser used for extracting information on commands.
	 * @see ScriptParser
	 * */
	@NonNull
	public final ScriptParser getParser() {
		return this.parser;
	}
}