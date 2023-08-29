package com.topper.commands.search;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.commands.PicoCommand;
import com.topper.commands.PicoTopLevelCommand;
import com.topper.commands.file.BasedGadget;
import com.topper.exceptions.commands.IllegalCommandException;
import com.topper.sstate.CommandState;
import com.topper.sstate.ExecutionState;
import com.topper.sstate.PicoState;
import com.topper.sstate.ScriptContext;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "search", mixinStandardHelpOptions = true, version = "1.0", description = "Searches for a regular expression in the string representation of all extracted gadgets.")
@PicoState(states = { ExecutionState.class })
public final class PicoSearchCommand extends PicoCommand {
	
	@Option(names = { "-r", "--regex" }, required = true, paramLabel = "REGEX", description = "Regular expression to use while searching through the gadgets.")
	private String regex;

	@ParentCommand
	private PicoTopLevelCommand parent;
	
	@Override
	public final void execute(@NonNull final ScriptContext context) throws IllegalCommandException {
		
		if (regex == null) {
			throw new IllegalCommandException("Invalid regex expression.");
		}
		
		if (regex.length() == 0) {
			// Print all gadgets
			for (@NonNull final BasedGadget gadget : context.getSession().getGadgets()) {
				parent.out().println(gadget.toString() + System.lineSeparator());
			}
			return;
		}
		
		// Interpret expression as regex
		final Pattern pattern;
		try {
			pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		} catch (final PatternSyntaxException e) {
			throw new IllegalCommandException("Given pattern " + regex + " is invalid.");
		}
		
		// Apply expression to each gadget's string representation
		Matcher matcher;
		for (@NonNull final BasedGadget gadget : context.getSession().getGadgets()) {
			matcher = pattern.matcher(gadget.toString());
			if (matcher.find()) {
				parent.out().println(gadget.toString() + System.lineSeparator());
			}
		}
	}

	@Override
	@NonNull 
	public final CommandState next() {
		return new ExecutionState(this.getContext());
	}
	
	@SuppressWarnings("null")	// cannot be null
	@Override
	@NonNull 
	public final PicoTopLevelCommand getTopLevel() {
		if (this.parent == null) {
			throw new UnsupportedOperationException("Cannot access parent before its initialized.");
		}
		return this.parent;
	}
}