package com.topper.commands.search;

import java.util.List;
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
	
	@Option(names = { "-r", "--regex" }, required = false, defaultValue = "", paramLabel = "REGEX", description = "Regular expression to use while searching through the gadgets.")
	private String regex;
	
	@Option(names = { "-u", "--upper" }, required = false, defaultValue = "-1", paramLabel = "UPPER_BOUND", description = "Upper bound for gadget length matching given regex. Non - positive values are ignored.")
	private int upper;

	@Option(names = { "-l", "--lower" }, required = false, defaultValue = "-1", paramLabel = "LOWER_BOUND", description = "Lower bound for gadget length matching given regex. Negative values are ignored.")
	private int lower;
	
	@ParentCommand
	private PicoTopLevelCommand parent;
	
	@Override
	public final void execute(@NonNull final ScriptContext context) throws IllegalCommandException {
		
		if (this.upper <= 0) {
			this.upper = context.getConfig().getSweeperConfig().getMaxNumberInstructions();
		}
		
		if (this.lower < 0) {
			this.lower = 0;
		}
		
		if (this.upper < this.lower) {
			throw new IllegalCommandException("Upper bound must be at least as big as lower bound.");
		}
		
		if (regex.length() == 0) {
			regex = ".*";
		}
		
		// Interpret expression as regex
		final Pattern pattern;
		try {
			pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		} catch (final PatternSyntaxException e) {
			throw new IllegalCommandException("Given pattern " + regex + " is invalid.");
		}
		
		// Apply expression to each gadget's string representation
		final List<@NonNull BasedGadget> gadgets = context.getSession().getGadgets();
		if (gadgets == null) {
			throw new IllegalCommandException("List of gadgets does not exist.");
		}
		
		Matcher matcher;
		for (@NonNull final BasedGadget gadget : gadgets) {
			if (gadget.getGadget().getInstructions().size() > this.upper ||
					gadget.getGadget().getInstructions().size() < this.lower) {
				continue;
			}
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