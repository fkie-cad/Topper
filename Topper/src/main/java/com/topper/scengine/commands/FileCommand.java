package com.topper.scengine.commands;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.pipeline.DecompilationDriver;
import com.topper.dex.decompilation.pipeline.PipelineArgs;
import com.topper.dex.decompilation.pipeline.StageInfo;
import com.topper.dex.decompilation.pipeline.StaticInfo;
import com.topper.dex.decompilation.staticanalyser.Gadget;
import com.topper.exceptions.CommandException;
import com.topper.exceptions.StageException;
import com.topper.file.FileUtil;
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
	 * Executes the semantics linked to the "file [path]" command.
	 * 
	 * @throws IOException
	 * @throws CommandException
	 */
	@Override
	public final void execute(final ScriptContext context) throws IOException, CommandException {

		try {
			// 1. Read file contents into memory.
			final byte[] content = FileUtil.readContents(file);

			// 2. Extract gadgets using a pipeline.
			final PipelineArgs args = new PipelineArgs(context.getConfig(), 0, content);
			final DecompilationDriver driver = new DecompilationDriver();

			final TreeMap<@NonNull String, @NonNull StageInfo> results = (TreeMap<@NonNull String, @NonNull StageInfo>) driver
					.decompile(args).getResults();
			final ImmutableList<@NonNull Gadget> gadgets = ((StaticInfo)results.get(StaticInfo.class.getSimpleName())).getGadgets();
			
			// 3. Adjust session info in script context.
			context.getSession().setLoadedFile(file);
			context.getSession().setGadgets(gadgets);

		} catch (final IllegalArgumentException e) {
			context.getIO().error("Failed to read file contents of " + file.getPath() + System.lineSeparator());
		} catch (final StageException e) {
			context.getIO().error("Failed to decompile " + file.getPath() + System.lineSeparator());
		}
	}

	/**
	 * Get the file linked to this file command.
	 */
	public final File getFile() {
		return this.file;
	}
}