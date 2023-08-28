package com.topper.scengine.commands;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.scripting.IllegalCommandException;
import com.topper.sstate.ExecutionState;

@TopperCommandParser(states = { ExecutionState.class })
public final class AttackCommandParser implements ScriptCommandParser {

	private final CommandManager manager;

	public AttackCommandParser() {
		this.manager = CommandManager.get();
	}

	@Override
	@NonNull
	public final ScriptCommand parse(@NonNull final String[] tokens) throws IllegalCommandException {

		// TODO: CONTINUE HERE: IMPLEMENT SUB COMMANDS AND WRITE TESTS FOR COMMANDS

		// Check sub - command
		if (tokens.length <= 1) {
			throw new IllegalCommandException("Invalid usage: " + this.usage());
		}

		// In command hierarchy, the sub - command must be a direct child of this parent
		// parser.
		final ScriptCommandParser parser = this.manager.findParserByName(this.getClass(), tokens[1]);
		if (parser == null) {
			throw new IllegalCommandException("Subcommand " + tokens[1] + " is unknown.");
		}

		// Parse command from sub - command tokens
		return parser.parse(Arrays.copyOfRange(tokens, 1, tokens.length));
	}

	@Override
	@NonNull
	public final String usage() {
		return this.command() + " <attack-type> <attack-specific args>";
	}

	@Override
	@NonNull
	public final String command() {
		return "attack";
	}

	@Override
	@NonNull 
	public Class<? extends ScriptCommand> commandType() {
		return AttackCommand.class;
	}
}