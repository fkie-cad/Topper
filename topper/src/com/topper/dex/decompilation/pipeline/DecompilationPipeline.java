package com.topper.dex.decompilation.pipeline;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompilation.Gadget;
import com.topper.dex.decompilation.semanticanalyser.DefaultSemanticAnalyser;
import com.topper.dex.decompilation.semanticanalyser.SemanticAnalyser;
import com.topper.dex.decompilation.staticanalyser.DefaultStaticAnalyser;
import com.topper.dex.decompilation.staticanalyser.StaticAnalyser;
import com.topper.dex.decompilation.sweeper.BackwardLinearSweeper;
import com.topper.dex.decompilation.sweeper.Sweeper;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;
import com.topper.exceptions.SweeperException;

/**
 * Pipeline that processes raw bytes into a list of gadgets.
 * It is currently composed of three stages:
 * 1. Sweeping: Detect valid sequences of instructions.
 * 2. Static Analysis: For each instruction sequence extract
 * 	the CFG and DFG and construct a gadget.
 * 3. Semantic Analysis: For each gadget add further information
 * 	by conducting e.g. a symbolic analysis. This stage
 * 	may filter gadgets e.g. in case a gadget is contradictory.
 * 
 * Adding a stage to this pipeline requires:
 * 1. Adjusting <code>DecompilationPipeline.decompile</code> so
 * 	that it executes the new stage.
 * 2. Adding an expandable stage class hierarchy that actually
 * 	does the heavy lifting.
 * 
 * This pipeline is essentially an extended strategy pattern. I.e.
 * a user may request a different <code>Sweeper</code> at runtime,
 * or reconfigure the <code>StaticAnalyser</code>.
 * 
 * @author Pascal Kühnemann
 * */
public final class DecompilationPipeline {

	/**
	 * Sweeper that extracts instruction sequences from a
	 * given byte array.
	 * */
	private Sweeper sweeper;
	
	/**
	 * Analyser responsible for perform static analysis.
	 * This involves CFG and DFG extraction.
	 * */
	private StaticAnalyser staticAnalyser;
	
	/**
	 * Analyser for semantical tasks. E.g. if a gadget is
	 * contradictory to the task, or behaves like a NOP, it
	 * can be filtered.
	 * */
	private SemanticAnalyser semanticAnalyser;
	
	/**
	 * Initializes this pipeline with default stages. Currently
	 * the default stages are:
	 * - <code>BackwardLinearSweeper</code>
	 * - <code>DefaultStaticAnalyser</code>
	 * - <code>DefaultSemanticAnalysr</code>
	 * 
	 * The default values can be overwritten by calling their
	 * respective setters.
	 * 
	 * @see BackwardLinearSweeper
	 * @see DefaultStaticAnalyser
	 * @see DefaultSemanticAnalyser
	 * */
	public DecompilationPipeline() {
		this.sweeper = new BackwardLinearSweeper();
		this.staticAnalyser = new DefaultStaticAnalyser();
		this.semanticAnalyser = new DefaultSemanticAnalyser();
	}
	
	/**
	 * Executes the stages of this pipeline given a byte array.
	 * 
	 * There are currently three different stages in this pipeline:
	 * 1. Sweeping: Detect valid sequences of instructions.
	 * 2. Static Analysis: For each instruction sequence extract
	 * 	the CFG and DFG and construct a gadget.
	 * 3. Semantic Analysis: For each gadget add further information
	 * 	by conducting e.g. a symbolic analysis. This stage
	 * 	may filter gadgets e.g. in case a gadget is contradictory.
	 * 
	 * Currently, it is possible to skip a stage by supplying
	 * - <code>EmptySweeper</code> to <code>setSweeper</code>
	 * - <code>EmptyCFGAnalyser</code> and <code>EmptyDFGAnalyser</code>
	 * 	to an instance of <code>StaticAnalyser</code> and then
	 * 	providing that instance to <code>setStaticAnalyser</code>
	 * - <code>EmptySemanticAnalyser</code> to <code>setSemanticAnalyser</code>
	 * If e.g. <code>EmptySweeper</code> is used, then the sweeper
	 * will not find any instruction sequences and thus all
	 * following stages will not produce any results.
	 * 
	 * Disabling the <code>StaticAnalyser</code> still produces
	 * <code>Gadget</code>s, but without <code>CFG</code> and <code>DFG</code>.
	 * 
	 * Finally, using <code>EmptySemanticAnalyser</code> filters
	 * all gadgets, thus resulting in this method returning an
	 * empty list.
	 * 
	 * @param bytes Byte array, from which to extract gadgets.
	 * @param offset Offset relative to the beginning of <code>bytes</code>.
	 * 	It represents the starting point of decompilation.
	 * @return List of gadgets extracted from <code>bytes</code>
	 * 	by applying above mentioned stages.
	 * @throws SweeperException If sweeping fails.
	 * */
	@NonNull
	public final ImmutableList<@NonNull Gadget> decompile(final byte @NonNull [] bytes, final int offset) throws SweeperException {
		
		// Extract instructions from bytes
		final ImmutableList<@NonNull ImmutableList<@NonNull DecompiledInstruction>> instructionSequences
			= this.sweeper.sweep(bytes, offset);
		
		// Apply static analysis to obtain initial gadgets
		final List<@NonNull Gadget> gadgets = new LinkedList<@NonNull Gadget>();
		for (final ImmutableList<@NonNull DecompiledInstruction> sequence : instructionSequences) {
			gadgets.add(this.staticAnalyser.analyse(sequence));
		}
		
		// Apply semantic analysis to obtain annotated gadgets.
		// Also functions as filter.
		Gadget gadget;
		for (@NonNull final Gadget candidate : gadgets) {
			
			gadget = this.semanticAnalyser.analyse(candidate);
			if (gadget == null) {
				gadgets.remove(candidate);
			}
		}
		
		return ImmutableList.copyOf(gadgets);
	}
	
	/**
	 * Replace current sweeper with a new sweeper.
	 * */
	public final void setSweeper(@NonNull final Sweeper sweeper) {
		this.sweeper = sweeper;
	}
	
	/**
	 * Replace current static analyser with a new static analyser.
	 * */
	public final void setStaticAnalyser(@NonNull final StaticAnalyser sa) {
		this.staticAnalyser = sa;
	}
	
	/**
	 * Replace current semantic analyser with a new semantic analyser.
	 * */
	public final void setSemanticAnalyser(@NonNull final SemanticAnalyser sa) {
		this.semanticAnalyser = sa;
	}
}