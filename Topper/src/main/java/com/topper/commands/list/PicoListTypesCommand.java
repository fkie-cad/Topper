package com.topper.commands.list;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;

import com.topper.commands.PicoCommand;
import com.topper.commands.PicoTopLevelCommand;
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

@Command(name = "types", mixinStandardHelpOptions = true, version = "1.0", description = "Lists all types of the loaded file.")
@CommandLink(states = { ExecutionState.class })
public final class PicoListTypesCommand extends PicoCommand {

	@Option(names = { "-r", "--regex" }, defaultValue = "", paramLabel = "REGEX", description = "Lists all types matching the given regular expression.")
	private String regex;
	
	private Pattern pattern;
	
	@ParentCommand
	private PicoListCommand parent;
	
	@Override
	public final void execute(@NonNull final CommandContext context) throws CommandException {
		
		this.checkArgs();
		
		// Get all file candidates.
		final ComposedFile loaded = this.getContext().getSession().getLoadedFile();
		final List<@NonNull DexFile> dexFiles = loaded.getDexFiles();
		
		// Iterate over all files and their types.
		DexBackedDexFile current;
		for (@NonNull final DexFile dexFile : dexFiles) {
			
			final StringBuilder b = new StringBuilder();
			
			current = dexFile.getDexFile();
			
			DexFileHelper.iterateTypes(current, type -> {
				
				final String name = DexFileHelper.prettyType(type);
				final Matcher matcher = this.pattern.matcher(name);
				if (matcher.find()) {
					b.append("  " + name + System.lineSeparator());
				}
			});
			
			if (b.length() > 0) {
				b.insert(0, String.format("[Offset = %#x]: ", dexFile.getOffset()) + dexFile.getId() + System.lineSeparator());
			}
			this.getTopLevel().out().print(b.toString());
		}
	}

	@Override
	@NonNull 
	public final CommandState next() {
		return new ExecutionState(this.getContext());
	}

	@Override
	@NonNull
	public final PicoTopLevelCommand getTopLevel() {
		if (this.parent == null) {
			throw new UnsupportedOperationException("Cannot access parent before its initialized.");
		}
		return this.parent.getTopLevel();
	}
	
	private final void checkArgs() throws CommandException {
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
}