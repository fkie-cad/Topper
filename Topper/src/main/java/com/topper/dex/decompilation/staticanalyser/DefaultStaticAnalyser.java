package com.topper.dex.decompilation.staticanalyser;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.StaticAnalyserConfig;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompilation.graphs.DFG;
import com.topper.dex.decompilation.pipeline.Pipeline;
import com.topper.dex.decompilation.pipeline.PipelineArgs;
import com.topper.dex.decompilation.pipeline.PipelineContext;
import com.topper.dex.decompilation.pipeline.StaticInfo;
import com.topper.dex.decompilation.pipeline.SweeperInfo;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;
import com.topper.exceptions.pipeline.DuplicateInfoIdException;
import com.topper.exceptions.pipeline.MissingStageInfoException;

/**
 * Default implementation of {@link StaticAnalyser}. It is used in
 * {@link Pipeline#createDefaultPipeline()}.
 * 
 * @author Pascal Kühnemann
 * @since 21.08.2023
 */
public final class DefaultStaticAnalyser extends StaticAnalyser {

	/**
	 * Performs static analysis on the given {@link PipelineContext}.
	 * 
	 * If analysis is successful, then <code>context</code> will be
	 * augmented with {@link StaticInfo}. Among other things, a list
	 * of {@link Gadget}s is returned.
	 * 
	 * Analysis is configurable via {@link StaticAnalyserConfig} and
	 * may decide whether to skip e.g. {@link CFG} extraction.
	 * 
	 * @param context <code>PipelineContext</code>, in which to perform static analysis.
	 * 
	 * @throws MissingStageInfoException If {@link PipelineArgs} or
	 *                                   {@link SweeperInfo} is missing.
	 * @throws DuplicateInfoIdException  If {@link StaticInfo} is already part of
	 *                                   the <code>context</code>.
	 * @throws IllegalArgumentException  If an instruction sequence obtained from
	 *                                   <code>SweeperInfo</code> is empty (must at
	 *                                   least contain pivot instruction).
	 */
	@Override
	public final void execute(@NonNull final PipelineContext context)
			throws MissingStageInfoException, DuplicateInfoIdException {

		final PipelineArgs args = context.getArgs();
		final SweeperInfo sweeper = context.getSweeperInfo(SweeperInfo.class.getSimpleName());
		final StaticAnalyserConfig config = args.getConfig().getStaticAnalyserConfig();

		@NonNull
		final ImmutableList<@NonNull ImmutableList<@NonNull DecompiledInstruction>> sequences = sweeper
				.getInstructionSequences();

		// Try out all instruction sequences from sweeping stage.
		final List<@NonNull Gadget> gadgets = new LinkedList<@NonNull Gadget>();
		int entry;
		CFG cfg;
		DFG dfg;
		for (@NonNull
		final ImmutableList<@NonNull DecompiledInstruction> instructions : sequences) {

			// If an empty instruction sequence had been found, something must have gone
			// wrong in
			// a previous stage.
			if (instructions.size() <= 0) {
				throw new IllegalArgumentException(
						"Encountered empty instruction sequence. Missing pivot instruction.");
			}

			// Compute entry wrt. current instruction sequence.
			entry = instructions.get(0).getOffset();

			// Extract CFG
			cfg = null;
			if (!config.shouldSkipCFG()) {
				cfg = this.getCFGAnalyser().extractCFG(instructions, entry);
			}

			// Extract DFG. Maybe this requires CFG as well.
			dfg = null;
			if (!config.shouldSkipDFG()) {
				dfg = this.getDFGAnalyser().extractDFG(instructions);
			}

			gadgets.add(new Gadget(instructions, cfg, dfg));
		}

		context.putInfo(StaticInfo.class.getSimpleName(), new StaticInfo(ImmutableList.copyOf(gadgets)));
	}
}