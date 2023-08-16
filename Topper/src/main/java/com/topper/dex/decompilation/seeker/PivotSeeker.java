package com.topper.dex.decompilation.seeker;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.pipeline.PipelineArgs;
import com.topper.dex.decompilation.pipeline.PipelineContext;
import com.topper.dex.decompilation.pipeline.SeekerInfo;
import com.topper.dex.decompilation.sweeper.Sweeper;
import com.topper.exceptions.DuplicateInfoIdException;

/**
 * @author Pascal Kühnemann
 * @since 15.08.2023
 * */
public class PivotSeeker extends Seeker {

	/**
	 * Searches all occurrences of the pivot instruction. This will
	 * serve as input to {@link Sweeper}.
	 * @throws DuplicateInfoIdException 
	 * */
	@Override
	public final void execute(@NonNull final PipelineContext context) throws DuplicateInfoIdException {
		
		final PipelineArgs args = context.getArgs();
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
		context.putInfo(SeekerInfo.class.getSimpleName(), new SeekerInfo(builder.build()));
	}
}