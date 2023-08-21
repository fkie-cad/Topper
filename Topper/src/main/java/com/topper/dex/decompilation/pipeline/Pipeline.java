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
import com.topper.exceptions.pipeline.StageException;

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
 * analysis etc. to determine what gadgets are eligible. Also determines what
 * gadgets are correct.</li>
 * </ul>
 * 
 * Beyond the four mandatory <code>Stage</code>s, it is possible to add
 * additional <code>Stage</code> implementations.
 * 
 * Finally, all <code>Stage</code> results are feed into a {@link Finalizer}
 * that summarizes those results.
 * 
 * @author Pascal Kühnemann
 * @since 07.08.2023
 */
public final class Pipeline {

	/**
	 * List of stages to run in this pipeline.
	 */
	@NonNull
	private final List<@NonNull Stage> stages;

	/**
	 * Finalizer to invoke after <code>stages</code> have been executed. Defaults to
	 * {@link DefaultFinalizer}.
	 */
	@NonNull
	private Finalizer finalizer;

	public Pipeline() {
		this.stages = new LinkedList<@NonNull Stage>();
		this.finalizer = new DefaultFinalizer();
	}

	/**
	 * Executes this {@link Pipeline} by iterating through the list of registered
	 * {@link Stage}s. Each <code>Stage</code> is given a {@link PipelineContext},
	 * which is initialized with <code>args</code>.
	 * 
	 * If this <code>Pipeline</code> is not valid, i.e. {@link Pipeline#isValid()}
	 * returns <code>false</code>, then execution will not take place. Also, if any
	 * of the registered <code>Stage</code>s throws an exception, execution will be
	 * interrupted.
	 * 
	 * After all <code>Stage</code>s are executed, a {@link Finalizer} is run to
	 * collect the final <code>Pipeline</code> output.
	 * 
	 * Currently, a <code>Pipeline</code> must at least contain the following
	 * <code>Stage</code>s:
	 * <ol>
	 * <li>{@link Seeker}: Identifies offsets of interest in a given buffer.</li>
	 * <li>{@link Sweeper}: Identifies instruction sequences starting from given
	 * offsets.</li>
	 * <li>{@link StaticAnalyser}: Extracts {@link CFG} and {@link DFG} from all
	 * instruction sequences.</li>
	 * <li>{@link SemanticAnalyser}: Performs e.g. reachability analysis, taint
	 * analysis etc. to determine what gadgets are eligible. Also determines what
	 * gadgets are correct.</li>
	 * </ol>
	 * 
	 * The above <code>Stage</code>s must be in that order.
	 */
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
		} catch (final RuntimeException e) {
			throw new StageException("An internal error occurred.", e);
		}
	}

	/**
	 * Checks whether this pipeline contains at least
	 * <ol>
	 * <li>Seeker</li>
	 * <li>Sweeper</li>
	 * <li>StaticAnalyser</li>
	 * <li>SemanticAnalyser</li>
	 * </ol>
	 * in that order. Also checks on finalizer. Otherwise a pipeline would be
	 * useless.
	 */
	public final boolean isValid() {

		int seekerIndex = -1;
		int sweeperIndex = -1;
		int staticIndex = -1;
		int semanticIndex = -1;

		Stage stage;
		for (int i = 0; i < this.stages.size(); i++) {

			stage = this.stages.get(i);

			if (Seeker.class.isAssignableFrom(stage.getClass()) && seekerIndex == -1) {
				seekerIndex = i;
			} else if (Sweeper.class.isAssignableFrom(stage.getClass()) && sweeperIndex == -1) {
				sweeperIndex = i;
			} else if (StaticAnalyser.class.isAssignableFrom(stage.getClass()) && staticIndex == -1) {
				staticIndex = i;
			} else if (SemanticAnalyser.class.isAssignableFrom(stage.getClass()) && semanticIndex == -1) {
				semanticIndex = i;
			}
		}

		return 0 <= seekerIndex && seekerIndex < sweeperIndex && sweeperIndex < staticIndex
				&& staticIndex < semanticIndex && semanticIndex < this.stages.size();
	}

	/**
	 * Adds a {@link Stage} to this {@link Pipeline}.
	 */
	public final void addStage(@NonNull final Stage stage) {
		this.stages.add(stage);
	}

	/**
	 * Removes a {@link Stage} from this {@link Pipeline}.
	 */
	public final void removeStage(@NonNull final Stage stage) {
		this.stages.remove(stage);
	}

	/**
	 * Removes a {@link Stage} at a specified <code>index</code> from this
	 * {@link Pipeline}.
	 */
	public final void removeStage(final int index) {
		this.stages.remove(index);
	}

	/**
	 * Overwrites the {@link Finalizer} with <code>finalizer</code>.
	 * */
	public final void setFinalizer(@NonNull final Finalizer finalizer) {
		this.finalizer = finalizer;
	}

	/**
	 * Creates a minimalistic default {@link Pipeline} that works out of the box.
	 * It consists of*
	 * <ol>
	 * <li>{@link PivotSeeker}</li>
	 * <li>{@link BackwardLinearSweeper}</li>
	 * <li>{@link DefaultStaticAnalyser}</li>
	 * <li>{@link DefaultSemanticAnalyser}</li>
	 * </ol>
	 * 
	 * @return Minimalistic, editable, default <code>Pipeline</code>.
	 * */
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