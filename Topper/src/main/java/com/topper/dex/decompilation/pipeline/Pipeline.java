package com.topper.dex.decompilation.pipeline;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompilation.graphs.DFG;
import com.topper.dex.decompilation.seeker.PivotSeeker;
import com.topper.dex.decompilation.semanticanalyser.DefaultSemanticAnalyser;
import com.topper.dex.decompilation.semanticanalyser.SemanticAnalyser;
import com.topper.dex.decompilation.staticanalyser.DefaultStaticAnalyser;
import com.topper.dex.decompilation.staticanalyser.Gadget;
import com.topper.dex.decompilation.staticanalyser.StaticAnalyser;
import com.topper.dex.decompilation.sweeper.BackwardLinearSweeper;
import com.topper.dex.decompilation.sweeper.Sweeper;
import com.topper.exceptions.StageException;

/**
 * A pipeline consisting of at least three main {@link Stage}s. Its task is to
 * take in a description of raw bytes, and output a higher level description of
 * these bytes in terms of {@link Gadget}s.
 * 
 * The three mandatory stages include:
 * <ul>
 * <li>{@link Sweeper}: Identifies instruction sequences starting from a given
 * offset.</li>
 * <li>{@link StaticAnalyser}: Extracts {@link CFG} and {@link DFG} from all
 * instruction sequences.</li>
 * <li>{@link SemanticAnalyser}: Performs e.g. reachability analysis, taint
 * analysis etc. to determine what gadgets are eligible.</li>
 * </ul>
 * 
 * Beyond the three mandatory {@code Stages}, it is possible to add additional
 * {@code Stage} implementations.
 * 
 * Finally, all {@code Stage} results are feed into a {@link Finalizer} that
 * summarizes those results.
 * 
 * @author Pascal Kühnemann
 * @since 07.08.2023
 */
public final class Pipeline<@NonNull T extends Map<@NonNull String, @NonNull StageInfo>> {

	@NonNull
	private final List<Stage<T>> stages;

	@NonNull
	private Finalizer<T> finalizer;

	private final Pipeline.@NonNull Builder<T> builder;

	/**
	 * 
	 */
	public Pipeline(final Pipeline.@NonNull Builder<T> builder) {
		this.stages = new LinkedList<Stage<T>>();
		this.builder = builder;
		this.finalizer = new DefaultFinalizer<T>();
	}

	@NonNull
	public final PipelineResult<T> execute(@NonNull final PipelineArgs args) throws StageException {

		if (!this.isValid()) {
			throw new StageException(
					"Pipeline must at least contain a Sweeper, a StaticAnalyser and a SemanticAnalyser.");
		}

		@NonNull
		T results = this.builder.build();
		results.put(PipelineArgs.class.getSimpleName(), args);

		for (final Stage<T> stage : this.stages) {
			results = stage.execute(results);
		}

		return this.finalizer.finalize(results);
	}

	/**
	 * Checks whether this pipeline contains at least
	 * <ul>
	 * <li>Sweeper</li>
	 * <li>StaticAnalyser</li>
	 * <li>SemanticAnalyser</li>
	 * </ul>
	 * 
	 * Otherwise a pipeline would be useless.
	 */
	public final boolean isValid() {

		boolean hasSweeper = false;
		boolean hasStatic = false;
		boolean hasSemantic = false;

		for (final Stage<T> stage : this.stages) {

			if (Sweeper.class.isAssignableFrom(stage.getClass())) {
				hasSweeper = true;
			} else if (StaticAnalyser.class.isAssignableFrom(stage.getClass())) {
				hasStatic = true;
			} else if (SemanticAnalyser.class.isAssignableFrom(stage.getClass())) {
				hasSemantic = true;
			}
		}

		return hasSweeper && hasStatic && hasSemantic;
	}

	public final void addStage(@NonNull final Stage<T> stage) {
		this.stages.add(stage);
	}

	public final void removeStage(@NonNull final Stage<T> stage) {
		this.stages.remove(stage);
	}

	public final void removeStage(final int index) {
		this.stages.remove(index);
	}

	public final void setFinalizer(@NonNull final Finalizer<T> finalizer) {
		this.finalizer = finalizer;
	}

	@NonNull
	public static final Pipeline<@NonNull TreeMap<@NonNull String, @NonNull StageInfo>> createDefaultPipeline() {

		final Pipeline<@NonNull TreeMap<@NonNull String, @NonNull StageInfo>> pipeline = new Pipeline<@NonNull TreeMap<@NonNull String, @NonNull StageInfo>>(
				TreeMap::new);
		pipeline.addStage(new PivotSeeker<@NonNull TreeMap<@NonNull String, @NonNull StageInfo>>());
		pipeline.addStage(new BackwardLinearSweeper<@NonNull TreeMap<@NonNull String, @NonNull StageInfo>>());
		pipeline.addStage(new DefaultStaticAnalyser<@NonNull TreeMap<@NonNull String, @NonNull StageInfo>>());
		pipeline.addStage(new DefaultSemanticAnalyser<@NonNull TreeMap<@NonNull String, @NonNull StageInfo>>());

		// Make sure default finalizer is used
		pipeline.setFinalizer(new DefaultFinalizer<@NonNull TreeMap<@NonNull String, @NonNull StageInfo>>());

		return pipeline;
	}

	public static interface Builder<T> {
		@NonNull
		T build();
	}
}