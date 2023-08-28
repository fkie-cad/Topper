package com.topper.tests.scengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.topper.scengine.commands.CommandManager;
import com.topper.scengine.commands.ExitCommandParser;
import com.topper.scengine.commands.FileCommandParser;
import com.topper.scengine.commands.HelpCommandParser;
import com.topper.scengine.commands.ScriptCommandParser;

public class TestCommandManager {

	private static CommandManager manager;

	@BeforeAll
	public static void init() {
		// Also executes static blocks in these parsers!
		new HelpCommandParser();
		new ExitCommandParser();
		new FileCommandParser();

		manager = CommandManager.get();
	}

	@BeforeEach
	public void initEach() {
		manager.clear();
	}

	private static void setupToplevel(@NonNull final ScriptCommandParser... parsers) {
		for (@NonNull
		final ScriptCommandParser parser : parsers) {
			manager.registerCommandParser(parser);
		}
	}

	@Test
	public void Given_EmptyHierarchy_When_RegisterHelpToplevel_Then_NoException() {
		// Reason: Registering a new, unique command must always work.
		manager.registerCommandParser(new HelpCommandParser());
	}

	@Test
	public void Given_HelpInToplevel_When_RegisterHelpToplevel_Then_IllegalArgumentException() {
		// Reason: Duplicate/ambiguous commands are illegal on same level.
		manager.registerCommandParser(new HelpCommandParser());
		assertThrowsExactly(IllegalArgumentException.class,
				() -> manager.registerCommandParser(new HelpCommandParser()));
	}

	@Test
	public void Given_ToplevelHelpSubcommandExit_When_RegisterExitToplevel_Then_NoException() {
		// Reason: Registering the same command on different levels of the hierarchy
		// must work.
		manager.registerCommandParser(new HelpCommandParser());
		manager.registerCommandParser(HelpCommandParser.class, new ExitCommandParser());
		manager.registerCommandParser(new ExitCommandParser());
	}

	@Test
	public void Given_ToplevelHelpSubcommandExit_When_RegisterExitSubcommand_Then_IllegalArgumentException() {
		// Reason: Duplicate/ambiguous subcommands are illegal.
		manager.registerCommandParser(new HelpCommandParser());
		manager.registerCommandParser(HelpCommandParser.class, new ExitCommandParser());
		assertThrowsExactly(IllegalArgumentException.class,
				() -> manager.registerCommandParser(HelpCommandParser.class, new ExitCommandParser()));
	}

	@Test
	public void Given_EmptyHierarchy_When_RegisterSubcommand_Then_IllegalArgumentException() {
		// Reason: Registering a subcommand without a parent must be illegal.
		assertThrowsExactly(IllegalArgumentException.class,
				() -> manager.registerCommandParser(ExitCommandParser.class, new HelpCommandParser()));
	}
	
	@Test
	public void Given_ToplevelHelp_When_RegisterHelpSubcommand_Expect_IllegalArgumentException() {
		// Reason: Cyclic hierarchy is allowed.
		manager.registerCommandParser(new HelpCommandParser());
		manager.registerCommandParser(HelpCommandParser.class, new HelpCommandParser());
	}

	@Test
	public void Given_EmptyHierarchy_When_SearchingHelp_Then_EmptyResult() {
		// Reason: Searching for a non - existing command must return empty set.
		assertEquals(0, manager.findParserByName(new HelpCommandParser().command()).size());
	}

	@Test
	public void Given_ToplevelHelp_When_SearchingHelp_Then_SingleHelpResult() {
		// Reason: Searching for unique parser in entire hierarchy must return that
		// parser.
		final HelpCommandParser parser = new HelpCommandParser();
		manager.registerCommandParser(parser);
		final Set<? extends ScriptCommandParser> matches = manager.findParserByName(parser.command());
		assertEquals(1, matches.size());
		assertEquals(parser, matches.stream().findFirst().get());
	}

	@Test
	public void Given_ToplevelHelpExitSubcommandHelp_When_SearchingHelp_Then_AllHelps() {
		// Reason: Searching must find all occurrences of a parser, if no layer is
		// specified.
		final HelpCommandParser first = new HelpCommandParser();
		final HelpCommandParser second = new HelpCommandParser();
		manager.registerCommandParser(first);
		manager.registerCommandParser(new ExitCommandParser());
		manager.registerCommandParser(ExitCommandParser.class, second);
		final Set<? extends ScriptCommandParser> matches = manager.findParserByName(first.command());
		assertEquals(2, matches.size());
		assertEquals(1, matches.stream().filter(parser -> parser.equals(first)).count());
		assertEquals(1, matches.stream().filter(parser -> parser.equals(second)).count());
	}

	@Test
	public void Given_ToplevelExitSubcommandHelp_When_SearchingSubcommand_Then_SubcommandHelp() {
		// Reason: Explicitly searching in a non - root sub hierarchy must return
		// results only from that hierarchy.
		final HelpCommandParser parser = new HelpCommandParser();
		manager.registerCommandParser(new ExitCommandParser());
		manager.registerCommandParser(ExitCommandParser.class, parser);
		final ScriptCommandParser result = manager.findParserByName(ExitCommandParser.class, parser.command());
		assertEquals(parser, result);
	}
	
	@Test
	public void Given_ToplevelHelpSubcommandExit_When_SearchingHelpSubcommand_Expect_Null() {
		// Reason: Not finding a parser in on a specified layer must result in null.
		manager.registerCommandParser(new ExitCommandParser());
		manager.registerCommandParser(ExitCommandParser.class, new HelpCommandParser());
		assertNull(manager.findParserByName(ExitCommandParser.class, new ExitCommandParser().command()));
	}
}