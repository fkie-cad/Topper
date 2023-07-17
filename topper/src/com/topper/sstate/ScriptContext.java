package com.topper.sstate;

import com.topper.configuration.TopperConfig;
import com.topper.interactive.IOManager;
import com.topper.scengine.ScriptParser;

public final class ScriptContext {

	/**
	 * Configuration assigned to this context. It can be used by
	 * specific states, whose registered commands can be configured.
	 * */
	private final TopperConfig config;
	
	/**
	 * Current state of this Deterministic Finite Automaton (DFA).
	 * */
	private CommandState state;
	
	/**
	 * Input / Output manager providing commands the ability to
	 * e.g. write to and read from console.
	 * */
	private final IOManager io;
	
	/**
	 * Parser for multi - line scripts. The <code>HelpCommand</code>
	 * uses it to extract command information.
	 * */
	private final ScriptParser parser;
	
	/**
	 * Initialize this context.
	 * 
	 * @param config Initial configuration used for this application run.
	 * @param io Input/Output manager used by commands to interact with user.
	 * @param parser Parser used to extract information on commands.
	 * */
	public ScriptContext(final TopperConfig config, final IOManager io, final ScriptParser parser) {
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
	public final void changeState(final CommandState newState) {
		this.state = newState;
	}
	
	/**
	 * Gets the current state of the DFA.
	 * @see CommandState
	 * */
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
	 * @see TopperConfig
	 * */
	public final TopperConfig getConfig() {
		return this.config;
	}
	
	/**
	 * Gets the Input/Output manager used for interacting
	 * with the user.
	 * @see IOManager
	 * */
	public final IOManager getIO() {
		return this.io;
	}
	
	/**
	 * Gets the parser used for extracting information on commands.
	 * @see ScriptParser
	 * */
	public final ScriptParser getParser() {
		return this.parser;
	}
}