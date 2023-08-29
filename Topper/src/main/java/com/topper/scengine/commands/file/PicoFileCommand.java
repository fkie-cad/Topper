package com.topper.scengine.commands.file;

import java.io.File;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.TopperConfig;
import com.topper.dex.decompilation.pipeline.DecompilationDriver;
import com.topper.dex.decompilation.pipeline.PipelineArgs;
import com.topper.dex.decompilation.pipeline.PipelineContext;
import com.topper.dex.decompilation.pipeline.PipelineResult;
import com.topper.dex.decompilation.pipeline.StaticInfo;
import com.topper.exceptions.pipeline.StageException;
import com.topper.exceptions.scripting.IllegalCommandException;
import com.topper.exceptions.scripting.InternalExecutionException;
import com.topper.file.AugmentedFile;
import com.topper.file.DexFile;
import com.topper.file.DexMethod;
import com.topper.file.FileType;
import com.topper.file.FileUtil;
import com.topper.file.RawFile;
import com.topper.file.VDexFile;
import com.topper.scengine.commands.PicoCommand;
import com.topper.scengine.commands.PicoTopLevelCommand;
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
	
	@ParentCommand
	private PicoTopLevelCommand parent;
	
	@Override
	public final void execute(@NonNull final ScriptContext context) throws IllegalCommandException, InternalExecutionException {

		// Check inputs.
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

		// 2. Identify file and load methods. Depending on the type, use a different set
		// of gadgets.
		ImmutableList<@NonNull DexMethod> methods = null;
		AugmentedFile aug;
		try {
			switch (this.type) {
			case DEX: {
				aug = new DexFile(file, content, context.getConfig());
				break;
			}
			case VDEX: {
				aug = new VDexFile(file, content, context.getConfig());
				break;
			}
			default: {
				// Raw should work regardless of the file type!
				aug = new RawFile(file, content);
				break;
			}
			}
		} catch (final IllegalArgumentException e) {
			throw new IllegalCommandException("File type mismatch. Requested type " + type.name() + " does not match actual file type.");
		}

		methods = aug.getMethods();

		// For raw files, the entire file is analysed,
		// whereas for .vdex and .dex, only methods are considered.
		@NonNull
		final ImmutableList<com.topper.scengine.commands.file.BasedGadget> gadgets;
		try {
			if (type.equals(FileType.RAW)) {
				gadgets = this.loadGadgetsFromRaw(context.getConfig(), content, 0);
			} else {

				final ImmutableList.Builder<com.topper.scengine.commands.file.BasedGadget> builder = new ImmutableList.Builder<>();

				for (@NonNull
				final DexMethod method : methods) {
					builder.addAll(this.loadGadgetsFromMethod(context.getConfig(), method));
				}

				gadgets = builder.build();
			}
		} catch (final StageException ignored) {
			throw new InternalExecutionException("Decompilation of file " + file.getPath() + " failed.");
		}

		// 3. Adjust session info in script context.
		context.getSession().setLoadedFile(file);
		context.getSession().setGadgets(gadgets);
	}
	
	@Override
	@NonNull
	public final CommandState next() {
		return new ExecutionState(this.getContext());
	}

	@NonNull
	private final ImmutableList<@NonNull BasedGadget> loadGadgetsFromRaw(
			@NonNull final TopperConfig config, final byte @NonNull [] content, final int offset)
			throws StageException {

		try {
			// Extract gadgets using a pipeline.
			final PipelineArgs args = new PipelineArgs(config, content);
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

	@NonNull
	private final ImmutableList<@NonNull BasedGadget> loadGadgetsFromMethod(
			@NonNull final TopperConfig config, @NonNull final DexMethod method) throws StageException {

		if (method.getBuffer() != null) {
			// Account for code item
			return this.loadGadgetsFromRaw(config, method.getBuffer(), method.getOffset() + 0x10);
		}
		// Buffer will only be null, if method is abstract or native.
		return ImmutableList.of();
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