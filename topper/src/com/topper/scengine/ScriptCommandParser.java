package com.topper.scengine;

/**
 * 
 * 
 * This "workaround" is only used, because in Java it is not allowed
 * to create interfaces with abstract static methods... Otherwise
 * <code>ScriptCommand</code> would be given a
 * <code>static parse(String command);</code> method to be overwritten
 * by all implementing subclasses. This would require adding only
 * a single class to <code>scengine</code>, if a new command was needed.
 * */
public interface ScriptCommandParser {

	
	/**
	 * Constructs a <code>ScriptCommand</code> object from an
	 * array of <code>String</code>s.
	 * 
	 * 
	 * 
	 * @param tokens Tokens of a command to be
	 * 				  interpreted as this command.
	 * @return Object representation of <code>tokens</code>.
	 * @throws IllegalCommandException If parsing <code>tokens</code>
	 * 	fails, because <code>tokens</code> violates some use-case
	 * 	rules.
	 */
	ScriptCommand parse(final String[] tokens);
}