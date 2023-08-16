package com.topper.dex.decompilation.staticanalyser;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.StaticAnalyserConfig;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompilation.graphs.DFG;
import com.topper.dex.decompilation.pipeline.PipelineArgs;
import com.topper.dex.decompilation.pipeline.PipelineContext;
import com.topper.dex.decompilation.pipeline.StaticInfo;
import com.topper.dex.decompilation.pipeline.SweeperInfo;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;
import com.topper.exceptions.DuplicateInfoIdException;
import com.topper.exceptions.MissingStageInfoException;

public final class DefaultStaticAnalyser extends StaticAnalyser {
	
	@Override
	public final void execute(@NonNull final PipelineContext context) throws MissingStageInfoException, DuplicateInfoIdException {
		
		final PipelineArgs args = context.getArgs();
		final SweeperInfo sweeper = context.getInfo(SweeperInfo.class.getSimpleName());
		final StaticAnalyserConfig config = args.getConfig().getStaticAnalyserConfig();
		
		@NonNull
		final ImmutableList<@NonNull ImmutableList<@NonNull DecompiledInstruction>> sequences = sweeper.getInstructionSequences();
		
		// Try out all instruction sequences from sweeping stage.
		final List<@NonNull Gadget> gadgets = new LinkedList<@NonNull Gadget>();
		int entry;
		CFG cfg;
		DFG dfg;
		for (@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions : sequences) {
		
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