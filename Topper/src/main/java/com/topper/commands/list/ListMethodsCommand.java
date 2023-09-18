package com.topper.commands.list;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;

import com.topper.commands.PicoCommand;
import com.topper.commands.TopLevelCommand;
import com.topper.exceptions.commands.CommandException;
import com.topper.exceptions.commands.IllegalCommandException;
import com.topper.file.ComposedFile;
import com.topper.file.DexFile;
import com.topper.file.RawFile;
import com.topper.helpers.DexFileHelper;
import com.topper.sstate.CommandContext;
import com.topper.sstate.CommandLink;
import com.topper.sstate.CommandState;
import com.topper.sstate.ExecutionState;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "methods", mixinStandardHelpOptions = true, version = "1.0", description = "Lists information on methods provided by loaded file(s).")
@CommandLink(states = { ExecutionState.class })
public final class ListMethodsCommand extends PicoCommand {
	
	@Option(names = { "-c", "--class" }, paramLabel = "CLASS", defaultValue = "", description = "Lists all available methods of the requested class.")
	private String className;
	
	@Option(names = { "-r", "--regex" }, paramLabel = "REGEX", defaultValue = "", description = "Lists all methods matching the given regular expression.")
	private String regex;
	
	private Pattern pattern;
	
	@ParentCommand
	private ListCommand parent;

	@Override
	public final void execute(final @NonNull CommandContext context) throws CommandException {
		
		// Check arguments.
		this.checkArgs();
		
		// Get all file candidates.
		final ComposedFile loaded = this.getContext().getSession().getLoadedFile();
		final List<@NonNull DexFile> dexFiles = loaded.getDexFiles();
		
		// Make class name valid, if necessary.
		if (!this.className.isEmpty()) {
			if (!this.className.startsWith("L")) {
				this.className = "L" + this.className;
			}
			if (!this.className.endsWith(";")) {
				this.className += ";";
			}
		}
		
		// Iterate over all files and print methods based on pattern.
		DexBackedDexFile dex;
		for (@NonNull final DexFile dexFile : dexFiles) {
			
			final StringBuilder b = new StringBuilder();
			
			dex = dexFile.getDexFile();
			DexFileHelper.iterateMethods(dex, method -> {
				
				// Skip non-matching classes, if a class name is defined.
				if (this.className.length() > 0 && !this.className.equals(method.getDefiningClass())) {
					return;
				}
				
				final String name = DexFileHelper.prettyMethod(method);
				final Matcher matcher = this.pattern.matcher(name);
				if (matcher.find()) {
					b.append("  " + name + System.lineSeparator());
				}
			});
			
			if (b.length() > 0) {
				b.insert(0, String.format("[Offset = %#x]: ", dexFile.getOffset()) + dexFile.getId() + System.lineSeparator());
			}
			print("" + b.toString());
		}
	}

	@Override
	@NonNull 
	public final CommandState next() {
		return new ExecutionState(this.getContext());
	}

	@Override
	@NonNull 
	public final TopLevelCommand getTopLevel() {
		if (this.parent == null) {
			throw new UnsupportedOperationException("Cannot access parent before its initialized.");
		}
		return this.parent.getTopLevel();
	}
	
	private final void checkArgs() throws CommandException {
		
		// Check regex, if available. Otherwise use all - matching pattern. 
		if (this.regex.isEmpty()) {
			this.regex = ".*";
		}
		try {
			this.pattern = Pattern.compile(this.regex, Pattern.DOTALL);
		} catch (final PatternSyntaxException e) {
			throw new IllegalCommandException("Given pattern " + this.regex + " is invalid.");
		}
		
		// Check if there is at least a .dex file to use.
		if (this.getContext().getSession().getLoadedFile() instanceof RawFile) {
			throw new IllegalCommandException("Currently no valid .dex file is loaded.");
		}
	}
	
	private final void print(@NonNull final String message) {
		this.getTopLevel().out().print(message);
	}
}