package com.topper.scengine.commands;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
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

	/**
	 * Sole instance of this class.
	 */
	private static CommandManager instance;

	/**
	 * Flat representation of the command hierarchy. It is required to use unique
	 * {@link ScriptCommandParser} classes for each {@link ScriptCommand}.
	 * Otherwise, a flat representation for a hierarchy (tree structure) is not
	 * possible.
	 */
	@NonNull
	private final Map<@NonNull Class<? extends ScriptCommandParser>, @NonNull Set<@NonNull ScriptCommandParser>> commandHierarchy;

	/**
	 * Unique top - level parser to use as the root of the command hierarchy.
	 */
	@NonNull
	private final Class<TopLevelCommandParser> root;
	
	private static Map<@NonNull Class<? extends CommandState>, @NonNull Set<@NonNull Class<? extends PicoCommand>>> stateCommandMap;
	
	static {
		stateCommandMap = new HashMap<>();
		
		// Load state -> command mappings
		final Reflections reflections = new Reflections("com.topper");
		final Set<Class<?>> annos = reflections.getTypesAnnotatedWith(PicoState.class);
		
		for (final Class<?> anno : annos) {
			
			System.out.println(anno.getAnnotation(PicoState.class));
			
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
	
	/**
	 * Creates a {@link CommandManager} by setting up the
	 * {@link ScriptCommandParser} hierarchy.
	 */
	private CommandManager() {
		this.root = TopLevelCommandParser.class;
		this.commandHierarchy = new HashMap<>();
		this.clear();
	}

	/**
	 * Gets single instance of {@link CommandManager}. On first call, this method
	 * creates the single instance and loads all commands annotated with
	 * {@link TopperCommandParser} into the command hierarchy.
	 * 
	 * For loading <code>ScriptCommandParser</code>s, it tries to derive the entire
	 * command hierarchy using the <code>TopperCommandParser</code> annotation and
	 * its {@link TopperCommandParser#parent()} parameter. If a sub - command is
	 * considered before its parent is, the sub - command will be re - scheduled to
	 * postpone processing it.
	 * 
	 * @return Sole instance of <code>CommandManager</code>.
	 * @throws IllegalArgumentException If registering a command annotated with
	 *                                  <code>TopperCommandParser</code> fails.
	 */
	@SuppressWarnings("null") // There is no null - path...
	@NonNull
	public static final CommandManager get() {
		if (CommandManager.instance == null) {
			CommandManager.instance = new CommandManager();
			CommandManager.instance.loadCommands();
		}
		return CommandManager.instance;
	}

	private final void loadCommands() {

		// Grab all annotated classes.
		final Reflections reflections = new Reflections("com.topper");
		final Set<Class<?>> parsers = reflections.getTypesAnnotatedWith(TopperCommandParser.class);

		// Convert annotated classes to parser classes.
		// Add parsers to processing queue.
		final Queue<Class<? extends ScriptCommandParser>> initQueue = new LinkedBlockingQueue<>();
		Class<? extends ScriptCommandParser> parser;
		for (final Class<?> raw : parsers) {
			try {
				parser = (Class<? extends ScriptCommandParser>) raw;
				initQueue.add(parser);
			} catch (final ClassCastException ignored) {
			}
		}

		// Try to register all parsers. As there is no guarantee on the
		// order of annotated classes, this approach retries until registering
		// classes fails for all parsers.
		ScriptCommandParser current;
		int cycle = 0;

		while (!initQueue.isEmpty() && cycle < initQueue.size()) {

			// Grab first parser class.
			parser = initQueue.poll();

			// Instantiate parser class, if possible. Otherwise ignore.
			current = null;
			try {
				current = (ScriptCommandParser) parser.getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				continue;
			}
			if (current == null) {
				continue;
			}

			// Try registering the parser. If it fails, then it will be re-queued and the
			// failure counter increased.
			try {
				this.registerCommandParser(current.getClass().getAnnotation(TopperCommandParser.class).parent(),
						current);
				cycle = 0;
			} catch (final IllegalArgumentException e) {
				cycle += 1;
				initQueue.add(parser);
			}
		}

		if (!initQueue.isEmpty()) {
			throw new IllegalArgumentException("Missing parent parsers to connect remaining sub - command parsers.");
		}
	}

	public final void registerCommandParser(@NonNull final Class<? extends ScriptCommandParser> parent,
			@NonNull final ScriptCommandParser toRegister) {

		if (!this.commandHierarchy.containsKey(parent)) {
			throw new IllegalArgumentException(
					"Given parent parser " + parent.getName() + " is not part of command hierarchy.");
		}

		// Check that command string is unique under parent.
		if (this.commandHierarchy.get(parent).stream()
				.anyMatch(parser -> parser.command().equals(toRegister.command()))) {
			throw new IllegalArgumentException("Command string " + toRegister.command()
					+ " is already taken by another parser under " + parent.getSimpleName() + ".");
		}

		// Add parser to subcommand set.
		this.commandHierarchy.get(parent).add(toRegister);
		this.commandHierarchy.put(toRegister.getClass(), new HashSet<@NonNull ScriptCommandParser>());
	}

	/**
	 * Register under root.
	 */
	public final void registerCommandParser(@NonNull final ScriptCommandParser parser) {
		this.registerCommandParser(root, parser);
	}

	/**
	 * Obtains all registered {@link ScriptCommandParser}s that match
	 * <code>name</code> as their command string. The comparison is case -
	 * insensitive, meaning that e.g. <code>"test"</code> and <code>"TeSt"</code>
	 * are considered equal.
	 * 
	 * 
	 * @param name Name of the command(s) to find.
	 * @return Set of unique <code>ScriptCommandParser</code>s that match
	 *         <code>name</code>.
	 */
	@SuppressWarnings("null")
	@NonNull
	public final Set<@NonNull ScriptCommandParser> findParserByName(@NonNull final String name) {
		// Breadth - First - Search
		final Queue<@NonNull ScriptCommandParser> todo = new LinkedBlockingQueue<>();
		todo.addAll(this.commandHierarchy.get(root));

		final String command = name.toUpperCase();
		ScriptCommandParser current;
		Set<@NonNull ScriptCommandParser> nextParsers;
		final Set<@NonNull ScriptCommandParser> matches = new HashSet<>();
		while (!todo.isEmpty()) {

			current = todo.poll();
			if (current.command().toUpperCase().equals(command)) {
				matches.add(current);
			}

			nextParsers = this.commandHierarchy.get(current.getClass());
			if (nextParsers != null) {
				todo.addAll(nextParsers);
			}
		}

		return matches;
	}

	@Nullable
	public final ScriptCommandParser findParserByName(@NonNull final Class<? extends ScriptCommandParser> parent,
			@NonNull final String name) {

		if (!this.commandHierarchy.containsKey(parent)) {
			return null;
		}

		final String command = name.toUpperCase();
		final Optional<? extends ScriptCommandParser> result = this.commandHierarchy.get(parent).stream()
				.filter(parser -> parser.command().toUpperCase().equals(command)).findFirst();
		return (result.isPresent()) ? result.get() : null;
	}

	public final void clear() {
		this.commandHierarchy.keySet().removeAll(this.commandHierarchy.keySet());
		this.commandHierarchy.put(root, new HashSet<@NonNull ScriptCommandParser>());
	}

	@NonNull
	public final Class<TopLevelCommandParser> getRoot() {
		return this.root;
	}
	
	@NonNull
	public static final Map<@NonNull Class<? extends CommandState>, @NonNull Set<@NonNull Class<? extends PicoCommand>>> getStateCommandMap() {
		return CommandManager.stateCommandMap;
	}
}