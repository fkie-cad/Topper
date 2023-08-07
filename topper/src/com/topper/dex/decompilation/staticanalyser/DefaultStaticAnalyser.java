package com.topper.dex.decompilation.staticanalyser;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompilation.graphs.DFG;
import com.topper.dex.decompilation.pipeline.PipelineArgs;
import com.topper.dex.decompilation.pipeline.StageInfo;
import com.topper.dex.decompilation.pipeline.StaticInfo;
import com.topper.dex.decompilation.pipeline.SweeperInfo;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public final class DefaultStaticAnalyser<@NonNull T extends Map<@NonNull String, @NonNull StageInfo>> extends StaticAnalyser<T> {
	
	@Override
	@NonNull
	public final T execute(@NonNull T results) {
		
		final PipelineArgs args = (PipelineArgs) results.get(PipelineArgs.class.getSimpleName());
		final SweeperInfo sweeper = (SweeperInfo) results.get(SweeperInfo.class.getSimpleName());
		
		@NonNull
		final ImmutableList<@NonNull ImmutableList<@NonNull DecompiledInstruction>> sequences = sweeper.getInstructionSequences();
		final int entry = args.getEntry();
		
		// Try out all instruction sequences from sweeping stage.
		final List<@NonNull Gadget> gadgets = new LinkedList<@NonNull Gadget>();
		for (final ImmutableList<@NonNull DecompiledInstruction> instructions : sequences) {
		
			// Extract CFG
			final CFG cfg = this.getCFGAnalyser().extractCFG(instructions, entry);
			
			// Extract DFG. Maybe this requires CFG as well.
			final DFG dfg = this.getDFGAnalyser().extractDFG(instructions);
			
			gadgets.add(new Gadget(instructions, cfg, dfg));
		}
		
		results.put(StaticInfo.class.getSimpleName(), new StaticInfo(ImmutableList.copyOf(gadgets)));
		return results;
	}
}