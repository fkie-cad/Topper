package com.topper.dex.decompilation.seeker;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.pipeline.PipelineArgs;
import com.topper.dex.decompilation.pipeline.SeekerInfo;
import com.topper.dex.decompilation.pipeline.Stage;
import com.topper.dex.decompilation.pipeline.StageInfo;
import com.topper.dex.decompilation.sweeper.Sweeper;
import com.topper.exceptions.StageException;

public class PivotSeeker<@NonNull T extends Map<@NonNull String, @NonNull StageInfo>> implements Stage<T> {

	/**
	 * Searches all occurrences of the pivot instruction. This will
	 * serve as input to {@link Sweeper}.
	 * */
	@Override
	public @NonNull T execute(@NonNull final T results) throws StageException {
		
		final PipelineArgs args = (PipelineArgs) results.get(PipelineArgs.class.getSimpleName());
		final Opcode pivot = args.getConfig().getSweeperConfig().getPivotOpcode();
		final byte[] buffer = args.getBuffer();
		final Opcodes opcodes = Opcodes.forDexVersion(args.getConfig().getDecompilerConfig().getDexVersion());
		final byte val = (byte)(opcodes.getOpcodeValue(pivot) & 0xff);
		
		// Forward linear sweep to find pivot opcodes. Fully disregard file structure, if any.
		final ImmutableList.Builder<Integer> builder = new ImmutableList.Builder<>();
		for (int i = 0; i < buffer.length; i++) {
			if (val == buffer[i]) {
				builder.add(i);
			}
		}
		
		// Add list of pivot offsets to results
		results.put(SeekerInfo.class.getSimpleName(), new SeekerInfo(builder.build()));
		
		return results;
	}
}