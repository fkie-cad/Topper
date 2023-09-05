package com.topper.commands.file;

import java.io.File;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;

import com.google.common.collect.ImmutableList;
import com.topper.commands.PicoCommand;
import com.topper.commands.PicoTopLevelCommand;
import com.topper.configuration.TopperConfig;
import com.topper.dex.decompilation.pipeline.DecompilationDriver;
import com.topper.dex.decompilation.pipeline.PipelineArgs;
import com.topper.dex.decompilation.pipeline.PipelineContext;
import com.topper.dex.decompilation.pipeline.PipelineResult;
import com.topper.dex.decompilation.pipeline.StaticInfo;
import com.topper.exceptions.commands.IllegalCommandException;
import com.topper.exceptions.commands.InternalExecutionException;
import com.topper.exceptions.pipeline.StageException;
import com.topper.file.ComposedFile;
import com.topper.file.DexFile;
import com.topper.file.FileType;
import com.topper.file.RawFile;
import com.topper.file.VDexFile;
import com.topper.helpers.FileUtil;
import com.topper.sstate.CommandState;
import com.topper.sstate.ExecutionState;
import com.topper.sstate.PicoState;
import com.topper.sstate.ScriptContext;
import com.topper.sstate.SelectionState;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "file", mixinStandardHelpOptions = true, version = "1.0", description = "Loads a given file from file system and parses it.")
@PicoState(states = { SelectionState.class, ExecutionState.class })
public final class PicoFileCommand extends PicoCommand {


	@Option(names = { "-t",
			"--type" }, required = true, paramLabel = "FILE_TYPE", description = "Type to assume for a file while loading. It may differ from the actual file type.")
	private FileType type;

	@Option(names = { "-f",
			"--file" }, required = true, paramLabel = "FILE_PATH", description = "Path of the file to load.")
	private String fileName;
	
	@Option(names = { "-i", "--index" }, defaultValue = "0", paramLabel = "INDEX", description = "Index of the dex file to use. Defaults to 0. Only Considered for .vdex files." )
	private int index;
	
	@ParentCommand
	private PicoTopLevelCommand parent;
	
	@Override
	public final void execute(@NonNull final ScriptContext context) throws IllegalCommandException, InternalExecutionException {

		// Check inputs.
		if (this.index < 0) {
			throw new IllegalCommandException("Dex index must be non - negative.");
		}
		
		final File file;
		try {
			file = FileUtil.openIfValid(this.fileName);
		} catch (final IllegalArgumentException e) {
			throw new IllegalCommandException("Given file is invalid: " + e.getMessage(), e);
		}

		final byte[] content;
		try {
			// 1. Read file contents into memory.
			content = FileUtil.readContents(file);
		} catch (final IllegalArgumentException ignored) {
			throw new IllegalCommandException("File " + file.getPath() + " cannot be read.");
		}

		// 2. Identify file. Depending on the type, use a different set
		// of gadgets.
		final DexBackedDexFile current;
		ComposedFile aug;
		try {
			switch (this.type) {
			case DEX: {
				final DexFile dex = new DexFile(file.getName(), content, 0, context.getConfig());
				current = dex.getDexFile();
				aug = dex;
				break;
			}
			case VDEX: {
				final VDexFile vdex = new VDexFile(file.getName(), content, context.getConfig());
				if (this.index >= vdex.getDexFiles().size()) {
					throw new IllegalCommandException("Dex index exceeds total number of dex files in vdex (" + vdex.getDexFiles().size() + ").");
				}
				current = vdex.getDexFiles().get(this.index).getDexFile();
				aug = vdex;
				break;
			}
			default: {
				// Raw should work regardless of the file type!
				aug = new RawFile(file.getName(), content);
				current = null;
				break;
			}
			}
		} catch (final IllegalArgumentException e) {
			throw new IllegalCommandException("File type mismatch. Requested type " + type.name() + " does not match actual file type.");
		}

		// For raw files, the entire file is analysed,
		// whereas for .vdex and .dex, only methods are considered.
		@NonNull
		final ImmutableList<@NonNull BasedGadget> gadgets;
		try {
			gadgets = this.loadGadgetsFromRaw(context.getConfig(), content, 0, current);
		} catch (final StageException ignored) {
			throw new InternalExecutionException("Decompilation of file " + file.getPath() + " failed.");
		}

		// 3. Adjust session info in script context.
		context.getSession().setLoadedFile(aug);
		context.getSession().setGadgets(gadgets);
	}
	
	@Override
	@NonNull
	public final CommandState next() {
		return new ExecutionState(this.getContext());
	}

	@NonNull
	private final ImmutableList<@NonNull BasedGadget> loadGadgetsFromRaw(
			@NonNull final TopperConfig config, final byte @NonNull [] content, final int offset, @Nullable final DexBackedDexFile augmentation)
			throws StageException {

		try {
			// Extract gadgets using a pipeline.
			final PipelineArgs args = new PipelineArgs(config, content, augmentation);
			final DecompilationDriver driver = new DecompilationDriver();

			final PipelineResult result = driver.decompile(args);
			final PipelineContext pipelineContext = result.getContext();
			final StaticInfo info = pipelineContext.getStaticInfo(StaticInfo.class.getSimpleName());
			return ImmutableList.copyOf(
					info.getGadgets().stream().map(g -> new BasedGadget(g, offset)).collect(Collectors.toList()));
		} catch (final StageException e) {
			throw e;
		}
	}

	private final void println(@NonNull final ScriptContext context, @NonNull final String s) {
		parent.out().println(s);
	}

	@SuppressWarnings("null")
	@Override
	@NonNull 
	public final PicoTopLevelCommand getTopLevel() {
		if (this.parent == null) {
			throw new UnsupportedOperationException("Cannot access parent before its initialized.");
		}
		return this.parent;
	}

}