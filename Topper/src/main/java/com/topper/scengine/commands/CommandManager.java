package com.topper.scengine.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.reflections.Reflections;

import com.topper.sstate.CommandState;
import com.topper.sstate.PicoState;

/**
 * Manager that manages all {@link ScriptCommandParser}s and their related
 * {@link ScriptCommand}s. It stores the command hierarchy denoting what
 * commands are sub - commands of other commands. There is a root command that
 * forms the overall basis of that hierarchy.
 * 
 * @author Pascal Kühnemann
 * @since 28.08.2023
 */
public final class CommandManager {
	
	private static Map<@NonNull Class<? extends CommandState>, @NonNull Set<@NonNull Class<? extends PicoCommand>>> stateCommandMap;
	
	static {
		stateCommandMap = new HashMap<>();
		
		// Load state -> command mappings
		final Reflections reflections = new Reflections("com.topper");
		final Set<Class<?>> annos = reflections.getTypesAnnotatedWith(PicoState.class);
		
		for (final Class<?> anno : annos) {
			
			for (final Class<? extends CommandState> state : anno.getAnnotation(PicoState.class).states()) {
				
				if (!stateCommandMap.containsKey(state)) {
					stateCommandMap.put(state, new HashSet<>());
				}
				
				try {
					stateCommandMap.get(state).add((Class<? extends PicoCommand>) anno);
				} catch (final ClassCastException e) {
					throw new IllegalArgumentException("Annotated class " + anno.getSimpleName() + " is not a " + PicoCommand.class.getSimpleName());
				}
			}
		}
	}
	
	public static final Map<@NonNull Class<? extends CommandState>, @NonNull Set<@NonNull Class<? extends PicoCommand>>> getStateCommandMap() {
		return CommandManager.stateCommandMap;
	}
}