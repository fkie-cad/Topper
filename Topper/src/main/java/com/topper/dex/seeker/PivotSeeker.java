package com.topper.dex.seeker;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;

import com.google.common.collect.ImmutableList;
import com.topper.dex.pipeline.Pipeline;
import com.topper.dex.pipeline.PipelineArgs;
import com.topper.dex.pipeline.PipelineContext;
import com.topper.dex.pipeline.SeekerInfo;
import com.topper.dex.sweeper.Sweeper;
import com.topper.exceptions.pipeline.DuplicateInfoIdException;

/**
 * Default implementation of {@link Seeker}. It is used in
 * {@link Pipeline#createDefaultPipeline()}.
 * 
 * @author Pascal Kühnemann
 * @since 15.08.2023
 */
public class PivotSeeker extends Seeker {

	/**
	 * Searches all occurrences of the pivot instruction. This may serve as input to
	 * a {@link Sweeper}. It takes into account the size of the pivot instruction
	 * determined by its format.
	 * 
	 * If successful, then <code>context</code> will be augmented with
	 * {@link SeekerInfo}.
	 * 
	 * @param context {@link PipelineContext}, in which to perform analysis.
	 * @throws DuplicateInfoIdException If this {@PivotSeeker} has already been
	 *                                  executed.
	 */
	@Override
	public final void execute(@NonNull final PipelineContext context) throws DuplicateInfoIdException {

		final PipelineArgs args = context.getArgs();
		final Opcode pivot = args.getConfig().getSweeperConfig().getPivotOpcode();
		final byte[] buffer = args.getBuffer();
		final Opcodes opcodes = Opcodes.forDexVersion(args.getConfig().getDecompilerConfig().getDexVersion());
		final byte val = (byte) (opcodes.getOpcodeValue(pivot) & 0xff);

		// Forward linear sweep to find pivot opcodes. Fully disregard file structure,
		// if any.
		final ImmutableList.Builder<Integer> builder = new ImmutableList.Builder<>();
		for (int i = 0; i < buffer.length; i++) {
			if (val == buffer[i] && i + pivot.format.size <= buffer.length) {
				builder.add(i);
			}
		}

		// Add list of pivot offsets to results
		context.putInfo(SeekerInfo.class.getSimpleName(), new SeekerInfo(builder.build()));
	}
}