package com.topper.scengine.commands;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.scripting.IllegalCommandException;

/**
 * This "workaround" is only used, because in Java it is not allowed
 * to create interfaces with abstract static methods... Otherwise
 * <code>ScriptCommand</code> would be given a
 * <code>static parse(String command);</code> method to be overwritten
 * by all implementing subclasses. This would require adding only
 * a single class to <code>scengine</code>, if a new command was needed.
 * 
 * All implementations of {@link ScriptCommandParser} must be located
 * in the same (or sub-) package as <code>ScriptCommandParser</code>,
 * because {@link CommandManager} loads all classes from this package.
 * This enables adding commands and their parsers without having to
 * modify multiple points in code. Hence, adding a new parser can
 * be done by:
 * <pre>
 * static {
 * 	CodeManager.get().registerCommandParser(new subclass of ScriptCommandParser);
 * }
 * </pre>
 * 
 * @author Pascal Kühnemann
 * @since 23.08.2023
 * */
public interface ScriptCommandParser {
	
	/**
	 * Constructs a <code>ScriptCommand</code> object from an
	 * array of <code>String</code>s.
	 * 
	 * @param tokens Tokens of a command to be
	 * 				  interpreted as this command.
	 * @return Object representation of <code>tokens</code>.
	 * @throws IllegalCommandException If parsing <code>tokens</code>
	 * 	fails, because <code>tokens</code> violates some use-case
	 * 	rules.
	 */
	@NonNull
	ScriptCommand parse(@NonNull final String[] tokens) throws IllegalCommandException;
	
	/**
	 * Fixes a user - readable format of the command that this
	 * parser parses.
	 * 
	 * @return Human - readable command format.
	 * */
	@NonNull
	String usage();
	
	/**
	 * Fixed a user - readable (and writable) string representation
	 * of the <code>ScriptCommand</code> parsed by this parser. 
	 * 
	 * @return String representation of <code>ScriptCommand</code> linked
	 * 	to this parser.
	 * */
	@NonNull
	String command();
}