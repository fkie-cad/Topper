package com.topper.dex.decompilation.pipeline;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompilation.graphs.DFG;
import com.topper.dex.decompilation.seeker.PivotSeeker;
import com.topper.dex.decompilation.seeker.Seeker;
import com.topper.dex.decompilation.semanticanalyser.DefaultSemanticAnalyser;
import com.topper.dex.decompilation.semanticanalyser.SemanticAnalyser;
import com.topper.dex.decompilation.staticanalyser.DefaultStaticAnalyser;
import com.topper.dex.decompilation.staticanalyser.Gadget;
import com.topper.dex.decompilation.staticanalyser.StaticAnalyser;
import com.topper.dex.decompilation.sweeper.BackwardLinearSweeper;
import com.topper.dex.decompilation.sweeper.Sweeper;
import com.topper.exceptions.StageException;

/**
 * A pipeline consisting of at least four main {@link Stage}s. Its task is to
 * take in a description of raw bytes, and output a higher level description of
 * these bytes in terms of {@link Gadget}s.
 * 
 * The four mandatory stages include:
 * <ul>
 * <li>{@link Seeker}: Identifies offsets of interest in a given buffer.</li>
 * <li>{@link Sweeper}: Identifies instruction sequences starting from given
 * offsets.</li>
 * <li>{@link StaticAnalyser}: Extracts {@link CFG} and {@link DFG} from all
 * instruction sequences.</li>
 * <li>{@link SemanticAnalyser}: Performs e.g. reachability analysis, taint
 * analysis etc. to determine what gadgets are eligible. Also determines
 * what gadgets are correct.</li>
 * </ul>
 * 
 * Beyond the four mandatory <code>Stage</code>s, it is possible to add additional
 * <code>Stage</code> implementations.
 * 
 * Finally, all <code>Stage</code> results are feed into a {@link Finalizer} that
 * summarizes those results.
 * 
 * @author Pascal Kühnemann
 * @since 07.08.2023
 */
public final class Pipeline {

	/**
	 * List of stages to run in this pipeline.
	 * */
	@NonNull
	private final List<@NonNull Stage> stages;

	/**
	 * Finalizer to invoke after <code>stages</code> have been executed.
	 * Defaults to {@link DefaultFinalizer}.
	 * */
	@NonNull
	private Finalizer finalizer;

	public Pipeline() {
		this.stages = new LinkedList<@NonNull Stage>();
		this.finalizer = new DefaultFinalizer();
	}

	@NonNull
	public final PipelineResult execute(@NonNull final PipelineArgs args) throws StageException {

		if (!this.isValid()) {
			throw new StageException(
					"Pipeline must at least contain a Seeker, a Sweeper, a StaticAnalyser and a SemanticAnalyser.");
		}

		// Catch runtime exceptions like IllegalArgumentException
		// and wrap them in StageException.
		try {
			final PipelineContext context = new PipelineContext(args);
			for (final Stage stage : this.stages) {
				stage.execute(context);
			}

			return this.finalizer.finalize(context);
		} catch (final Exception e) {
			throw new StageException("An internal error occurred.", e);
		}
	}

	/**
	 * Checks whether this pipeline contains at least
	 * <ul>
	 * <li>Seeker</li>
	 * <li>Sweeper</li>
	 * <li>StaticAnalyser</li>
	 * <li>SemanticAnalyser</li>
	 * </ul>
	 * Also checks on finalizer.
	 * Otherwise a pipeline would be useless.
	 */
	public final boolean isValid() {

		boolean hasSeeker = false;
		boolean hasSweeper = false;
		boolean hasStatic = false;
		boolean hasSemantic = false;

		for (final Stage stage : this.stages) {

			if (Seeker.class.isAssignableFrom(stage.getClass())) {
				hasSeeker = true;
			} else if (Sweeper.class.isAssignableFrom(stage.getClass())) {
				hasSweeper = true;
			} else if (StaticAnalyser.class.isAssignableFrom(stage.getClass())) {
				hasStatic = true;
			} else if (SemanticAnalyser.class.isAssignableFrom(stage.getClass())) {
				hasSemantic = true;
			}
		}

		return hasSeeker && hasSweeper && hasStatic && hasSemantic && (this.finalizer != null);
	}

	public final void addStage(@NonNull final Stage stage) {
		this.stages.add(stage);
	}

	public final void removeStage(@NonNull final Stage stage) {
		this.stages.remove(stage);
	}

	public final void removeStage(final int index) {
		this.stages.remove(index);
	}

	public final void setFinalizer(@NonNull final Finalizer finalizer) {
		this.finalizer = finalizer;
	}

	@NonNull
	public static final Pipeline createDefaultPipeline() {

		final Pipeline pipeline = new Pipeline();
		pipeline.addStage(new PivotSeeker());
		pipeline.addStage(new BackwardLinearSweeper());
		pipeline.addStage(new DefaultStaticAnalyser());
		pipeline.addStage(new DefaultSemanticAnalyser());

		// Make sure default finalizer is used
		pipeline.setFinalizer(new DefaultFinalizer());

		return pipeline;
	}
}