package com.topper.dex.decompilation.pipeline;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.Gadget;
import com.topper.dex.decompilation.semanticanalyser.DefaultSemanticAnalyser;
import com.topper.dex.decompilation.semanticanalyser.SemanticAnalyser;
import com.topper.dex.decompilation.staticanalyser.StaticAnalyser;
import com.topper.dex.decompilation.sweeper.BackwardLinearSweeper;
import com.topper.dex.decompilation.sweeper.Sweeper;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

public final class DecompilationPipeline {

	private Sweeper sweeper;
	private StaticAnalyser staticAnalyser;
	private SemanticAnalyser semanticAnalyser;
	
	public DecompilationPipeline() {
		this.sweeper = new BackwardLinearSweeper();
		this.staticAnalyser = new StaticAnalyser();
		this.semanticAnalyser = new DefaultSemanticAnalyser();
	}
	
	public final ImmutableList<Gadget> decompile(final byte[] bytes) {
		
		// Extract instructions from bytes
		final ImmutableList<ImmutableList<DecompiledInstruction>> instructionSequences = this.sweeper.sweep(bytes, 0);
		
		// Apply static analysis to obtain initial gadgets
		final List<Gadget> gadgets = new LinkedList<Gadget>();
		for (final ImmutableList<DecompiledInstruction> sequence : instructionSequences) {
			gadgets.add(this.staticAnalyser.analyse(sequence));
		}
		
		// Apply semantic analysis to obtain annotated gadgets.
		// Also functions as filter.
		Gadget gadget;
		for (final Gadget candidate : gadgets) {
			gadget = this.semanticAnalyser.analyse(candidate);
			if (gadget == null) {
				gadgets.remove(candidate);
			}
		}
		
		return ImmutableList.copyOf(gadgets);
	}
	
	public final void setSweeper(final Sweeper sweeper) {
		this.sweeper = sweeper;
	}
	
	public final void setStaticAnalyser(final StaticAnalyser sa) {
		this.staticAnalyser = sa;
	}
	
	public final void setSemanticAnalyser(final SemanticAnalyser sa) {
		this.semanticAnalyser = sa;
	}
}