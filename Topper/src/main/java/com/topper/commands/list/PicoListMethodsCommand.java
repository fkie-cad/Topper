package com.topper.commands.list;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;

import com.topper.commands.PicoCommand;
import com.topper.commands.PicoTopLevelCommand;
import com.topper.exceptions.UnreachableException;
import com.topper.exceptions.commands.CommandException;
import com.topper.exceptions.commands.IllegalCommandException;
import com.topper.file.AugmentedFile;
import com.topper.file.DexFile;
import com.topper.file.DexFileHelper;
import com.topper.file.RawFile;
import com.topper.file.VDexFile;
import com.topper.sstate.CommandState;
import com.topper.sstate.ExecutionState;
import com.topper.sstate.PicoState;
import com.topper.sstate.ScriptContext;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "methods", mixinStandardHelpOptions = true, version = "1.0", description = "Lists information on methods provided by loaded file(s).")
@PicoState(states = { ExecutionState.class })
public final class PicoListMethodsCommand extends PicoCommand {
	
	@Option(names = { "-c", "--class" }, defaultValue = "", description = "Lists all available methods of the requested class.")
	private String className;
	
	@Option(names = { "-r", "--regex" }, defaultValue = "", description = "Lists all methods matching the given regular expression.")
	private String regex;
	
	private Pattern pattern;
	
	@ParentCommand
	private PicoListCommand parent;

	@Override
	public final void execute(final @NonNull ScriptContext context) throws CommandException {
		
		// Check arguments.
		this.checkArgs();
		
		// Get all file candidates.
		final AugmentedFile file = this.getContext().getSession().getLoadedFile();
		final List<@NonNull DexFile> dexFiles;
		if (file instanceof VDexFile) {
			dexFiles = ((VDexFile)file).getDexFiles();
		} else if (file instanceof DexFile) {
			dexFiles = List.of((DexFile)file);
		} else {
			// THIS CASE SHOULD BE IMPOSSIBLE!!!
			throw new UnreachableException("Given file is a raw file.");
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
			print(b.toString());
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
		if (this.getContext().getSession().getCurrentDex() == null || this.getContext().getSession().getLoadedFile() instanceof RawFile) {
			throw new IllegalCommandException("Currently no valid .dex file is loaded.");
		}
	}
	
	private final void print(@NonNull final String message) {
		this.getTopLevel().out().print(message);
	}
}