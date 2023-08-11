package com.topper.dex.decompilation.staticanalyser;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.StaticAnalyserConfig;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompilation.graphs.DFG;
import com.topper.dex.decompilation.pipeline.PipelineArgs;
import com.topper.dex.decompilation.pipeline.StageInfo;
import com.topper.dex.decompilation.pipeline.StaticInfo;
import com.topper.dex.decompilation.pipeline.SweeperInfo;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;
import com.topper.exceptions.MissingStageInfoException;

public final class DefaultStaticAnalyser<@NonNull T extends Map<@NonNull String, @NonNull StageInfo>> extends StaticAnalyser<T> {
	
	@Override
	@NonNull
	public final T execute(@NonNull final T results) throws MissingStageInfoException {
		
		final PipelineArgs args = (PipelineArgs) results.get(PipelineArgs.class.getSimpleName());
		if (args == null) {
			throw new MissingStageInfoException("DefaultStaticAnalyser requires pipeline arguments.");
		}
		final SweeperInfo sweeper = (SweeperInfo) results.get(SweeperInfo.class.getSimpleName());
		if (sweeper == null) {
			throw new MissingStageInfoException("DefaultStaticAnalyser requires sweeper info.");
		}
		final StaticAnalyserConfig config = args.getConfig().getStaticAnalyserConfig();
		
		@NonNull
		final ImmutableList<@NonNull ImmutableList<@NonNull DecompiledInstruction>> sequences = sweeper.getInstructionSequences();
		final int entry = args.getEntry();
		
		// Try out all instruction sequences from sweeping stage.
		final List<@NonNull Gadget> gadgets = new LinkedList<@NonNull Gadget>();
		CFG cfg;
		DFG dfg;
		for (final ImmutableList<@NonNull DecompiledInstruction> instructions : sequences) {
		
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
		
		results.put(StaticInfo.class.getSimpleName(), new StaticInfo(ImmutableList.copyOf(gadgets)));
		return results;
	}
}