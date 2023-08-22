package com.topper.scengine.commands;

import java.io.File;
import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.TopperConfig;
import com.topper.dex.decompilation.pipeline.DecompilationDriver;
import com.topper.dex.decompilation.pipeline.PipelineArgs;
import com.topper.dex.decompilation.pipeline.PipelineContext;
import com.topper.dex.decompilation.pipeline.PipelineResult;
import com.topper.dex.decompilation.pipeline.StaticInfo;
import com.topper.dex.decompilation.staticanalyser.Gadget;
import com.topper.exceptions.pipeline.StageException;
import com.topper.exceptions.scripting.CommandException;
import com.topper.file.DexFile;
import com.topper.file.DexMethod;
import com.topper.file.FileUtil;
import com.topper.file.VDexFile;
import com.topper.sstate.ScriptContext;

public final class FileCommand implements ScriptCommand {

	/**
	 * File referenced by the first parameter to the file command.
	 */
	@NonNull
	private final File file;

	public FileCommand(@NonNull final File file) {
		this.file = file;
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

			// 2. Identify file and load methods. Depending on the type, use a different set of gadgets.
			ImmutableList<@NonNull DexMethod> methods = null;
			try {
				final DexFile dex = new DexFile(this.file, content, context.getConfig());
				methods = dex.getMethods();
			} catch (final IllegalArgumentException e) {
				try {
					final VDexFile vdex = new VDexFile(this.file, content, context.getConfig());
					methods = vdex.getMethods();
				} catch (final IllegalArgumentException e1) {
				}
			}
			
			// For raw files, the entire file is analysed,
			// whereas for .vdex and .dex, only methods are considered.
			ImmutableList<@NonNull Gadget> gadgets = null;
			if (methods == null) {
				gadgets = loadGadgetsFromRaw(context.getConfig(), content);
			} else {
				
				final ImmutableList.Builder<@NonNull Gadget> builder = new ImmutableList.Builder<>();
				
				for (@NonNull final DexMethod method : methods) {
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
	private final ImmutableList<@NonNull Gadget> loadGadgetsFromRaw(@NonNull final TopperConfig config, final byte @NonNull [] content) throws StageException {
		
		try {
			// Extract gadgets using a pipeline.
			final PipelineArgs args = new PipelineArgs(config, content);
			final DecompilationDriver driver = new DecompilationDriver();

			final PipelineResult result = driver.decompile(args);
			final PipelineContext pipelineContext = result.getContext();
			final StaticInfo info = pipelineContext.getStaticInfo(StaticInfo.class.getSimpleName());
			return info.getGadgets();
		} catch (final StageException e) {
			throw e;
		}
	}
	
	@NonNull
	private final ImmutableList<@NonNull Gadget> loadGadgetsFromMethod(@NonNull final TopperConfig config, @NonNull final DexMethod method) throws StageException {
		
		if (method.getBuffer() != null) {
			return this.loadGadgetsFromRaw(config, method.getBuffer());
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
}