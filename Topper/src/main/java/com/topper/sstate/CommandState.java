package com.topper.sstate;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.reflections.Reflections;

import com.google.common.collect.ImmutableSet;
import com.topper.exceptions.scripting.CommandException;
import com.topper.exceptions.scripting.InvalidStateTransitionException;
import com.topper.scengine.commands.ScriptCommand;
import com.topper.scengine.commands.ScriptCommandParser;
import com.topper.scengine.commands.TopperCommandParser;

@SuppressWarnings("null")
public abstract class CommandState {
	
	/**
	 * Execution context of this state. It may be changed by commands.
	 * */
	private final ScriptContext context;
	
	private static Map<@NonNull Class<? extends CommandState>, @NonNull Set<@NonNull Class<? extends ScriptCommand>>> stateCommandMap;
	
	static {
		stateCommandMap = new HashMap<>();
		
		// Load state -> command mappings
		final Reflections reflections = new Reflections("com.topper");
		final Set<Class<?>> annos = reflections.getTypesAnnotatedWith(TopperCommandParser.class);
		
		ScriptCommandParser parser;
		
		for (final Class<?> anno : annos) {
			
			for (final Class<? extends CommandState> state : anno.getAnnotation(TopperCommandParser.class).states()) {
				
				if (!stateCommandMap.containsKey(state)) {
					stateCommandMap.put(state, new HashSet<>());
				}
				
				parser = null;
				try {
					parser = (ScriptCommandParser) anno.getDeclaredConstructor().newInstance();
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					throw new IllegalArgumentException("Instantiating parser " + anno.getSimpleName() + " failed.", e);
				}
				
				if (parser == null) {
					continue;
				}
				
				stateCommandMap.get(state).add(parser.commandType());
			}
		}
	}
	
	/**
	 * Initialize this state.
	 * 
	 * @param context Execution context to assign to this state.
	 * */
	public CommandState(@NonNull final ScriptContext context) {
		this.context = context;
	}

	/**
	 * Template method that precedes each command execution attempt
	 * with a white-list check to determine whether executing the
	 * requested command is allowed in this state or not.
	 * 
	 * @param command Command to check and execute if valid.
	 * @throws InvalidStateTransitionException If executing the
	 * 	requested command in this state is not allowed.
	 * @throws CommandException If execution of the command fails.
	 * @throws IOException If IO - related errors occur. If they
	 * 	make it until here they are fatal.
	 * */
	public final void execute(@NonNull final ScriptCommand command) throws InvalidStateTransitionException, CommandException, IOException {
		
		// Command must pass whitelist check
		for (final Class<? extends ScriptCommand> allowed : this.getAvailableCommands()) {
			
			if (allowed.isAssignableFrom(command.getClass())) {
				
				// Run command
				this.executeCommand(command);
				return;
			}
		}
		
		// Otherwise trigger exception
		throw new InvalidStateTransitionException("Cannot execute command.");
	}
	
	/**
	 * Gets execution context assigned to this state.
	 * @see ScriptContext
	 * */
	@NonNull
	public final ScriptContext getContext() {
		return this.context;
	}
	
	/**
	 * Describes what commands are available in this state. Therefore
	 * this is command whitelist. In this state, if <code>execute</code>
	 * is used to run a command, the command is only executed if its
	 * class is part of this whitelist.
	 * 
	 * This somewhat represents the transitions/edges in this finite
	 * state machine without the need of implementing a method for
	 * each possible subclass of <code>ScriptCommand</code>.
	 * 
	 * @return Set of subclasses of <code>ScriptCommand</code> that are
	 * 	allowed to be executed in this state.
	 * */
	@NonNull
	public ImmutableSet<Class<? extends ScriptCommand>> getAvailableCommands() {
		return ImmutableSet.copyOf(stateCommandMap.get(this.getClass()));
	}
	
	/**
	 * Actual execution of <code>command</code>. Its semantics
	 * depend on the state.
	 * 
	 * @param command Command to execute in this state.
	 * */
	public abstract void executeCommand(@NonNull final ScriptCommand command) throws InvalidStateTransitionException, CommandException, IOException;
}