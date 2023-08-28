package com.topper.scengine.commands.file;

import java.io.File;
import java.io.IOException;
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
import com.topper.exceptions.scripting.CommandException;
import com.topper.exceptions.scripting.IllegalCommandException;
import com.topper.file.AugmentedFile;
import com.topper.file.DexFile;
import com.topper.file.DexMethod;
import com.topper.file.FileType;
import com.topper.file.FileUtil;
import com.topper.file.RawFile;
import com.topper.file.VDexFile;
import com.topper.scengine.commands.ScriptCommand;
import com.topper.sstate.ScriptContext;

public final class FileCommand implements ScriptCommand {

	/**
	 * File referenced by the first parameter to the file command.
	 */
	@NonNull
	private final File file;

	private final FileType type;

	public FileCommand(@NonNull final File file, @NonNull final FileType type) {
		this.file = file;
		this.type = type;
	}

	/**
	 * Executes the semantics linked to the "file [raw|dex|vdex] [path]" command.
	 * 
	 * @throws IOException
	 * @throws CommandException
	 */
	@Override
	public final void execute(final ScriptContext context) throws IOException, CommandException {

		try {
			// 1. Read file contents into memory.
			final byte[] content = FileUtil.readContents(this.file);

			// 2. Identify file and load methods. Depending on the type, use a different set
			// of gadgets.
			ImmutableList<@NonNull DexMethod> methods = null;
			AugmentedFile aug;
			try {
				switch (this.type) {
				case DEX: {
					aug = new DexFile(this.file, content, context.getConfig());
					break;
				}
				case VDEX: {
					aug = new VDexFile(this.file, content, context.getConfig());
					break;
				}
				default: {
					// Raw should work regardless of the file type!
					aug = new RawFile(file, content);
					break;
				}
				}
			} catch (final IllegalArgumentException e) {
				throw new IllegalCommandException(
						"Requested: " + this.type.name() + ", but provided path points to different file format.");
			}

			methods = aug.getMethods();

			// For raw files, the entire file is analysed,
			// whereas for .vdex and .dex, only methods are considered.
			@NonNull
			final ImmutableList<com.topper.scengine.commands.file.BasedGadget> gadgets;
			if (this.type.equals(FileType.RAW)) {
				gadgets = loadGadgetsFromRaw(context.getConfig(), content, 0);
			} else {

				final ImmutableList.Builder<com.topper.scengine.commands.file.BasedGadget> builder = new ImmutableList.Builder<>();

				for (@NonNull
				final DexMethod method : methods) {
					builder.addAll(this.loadGadgetsFromMethod(context.getConfig(), method));
				}

				gadgets = builder.build();
			}

			// 3. Adjust session info in script context.
			context.getSession().setLoadedFile(file);
			context.getSession().setGadgets(gadgets);

		} catch (final IllegalArgumentException e) {
			context.getIO().error("Failed to read file contents of " + file.getPath() + System.lineSeparator());
		} catch (final StageException e) {
			context.getIO().error("Failed to decompile " + file.getPath() + System.lineSeparator());
		}
	}

	@NonNull
	private final ImmutableList<com.topper.scengine.commands.file.BasedGadget> loadGadgetsFromRaw(@NonNull final TopperConfig config,
			final byte @NonNull [] content, final int offset) throws StageException {

		try {
			// Extract gadgets using a pipeline.
			final PipelineArgs args = new PipelineArgs(config, content);
			final DecompilationDriver driver = new DecompilationDriver();

			final PipelineResult result = driver.decompile(args);
			final PipelineContext pipelineContext = result.getContext();
			final StaticInfo info = pipelineContext.getStaticInfo(StaticInfo.class.getSimpleName());
			return ImmutableList.copyOf(info.getGadgets().stream().map(g -> new BasedGadget(g, offset)).collect(Collectors.toList()));
		} catch (final StageException e) {
			throw e;
		}
	}

	@NonNull
	private final ImmutableList<com.topper.scengine.commands.file.BasedGadget> loadGadgetsFromMethod(@NonNull final TopperConfig config,
			@NonNull final DexMethod method) throws StageException {

		if (method.getBuffer() != null) {
			// Account for code item
			return this.loadGadgetsFromRaw(config, method.getBuffer(), method.getOffset() + 0x10);
		}
		// Buffer will only be null, if method is abstract or native.
		return ImmutableList.of();
	}

	/**
	 * Get the file linked to this file command.
	 */
	public final File getFile() {
		return this.file;
	}

	/**
	 * Get the requested file type. It does not necessarily coincide with the actual
	 * file type of {@link FileCommand#file}.
	 */
	public final FileType getFileType() {
		return this.type;
	}
}