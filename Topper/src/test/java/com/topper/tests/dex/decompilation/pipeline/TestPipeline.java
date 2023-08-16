package com.topper.tests.dex.decompilation.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.topper.configuration.TopperConfig;
import com.topper.dex.decompilation.pipeline.Pipeline;
import com.topper.dex.decompilation.pipeline.PipelineArgs;
import com.topper.dex.decompilation.pipeline.PipelineResult;
import com.topper.dex.decompilation.seeker.PivotSeeker;
import com.topper.dex.decompilation.seeker.Seeker;
import com.topper.dex.decompilation.semanticanalyser.DefaultSemanticAnalyser;
import com.topper.dex.decompilation.semanticanalyser.SemanticAnalyser;
import com.topper.dex.decompilation.staticanalyser.DefaultStaticAnalyser;
import com.topper.dex.decompilation.staticanalyser.StaticAnalyser;
import com.topper.dex.decompilation.sweeper.BackwardLinearSweeper;
import com.topper.dex.decompilation.sweeper.Sweeper;
import com.topper.exceptions.InvalidConfigException;
import com.topper.exceptions.StageException;
import com.topper.tests.utility.DexLoader;
import com.topper.tests.utility.TestConfig;

public class TestPipeline {

	private static TopperConfig config;

	@NonNull
	private static PipelineArgs createArgs(final byte @NonNull [] bytecode) {
		return new PipelineArgs(config, bytecode);
	}

	@BeforeAll
	public static void init() throws InvalidConfigException {
		config = TestConfig.getDefault();
		assertNotNull(config);
	}

	@Test
	public void Given_SubsetMandatoryStages_When_Validating_Expect_Invalid() {
		// Reason: Any real subset of the mandatory stages must be considered
		// invalid. Only all mandatory stages together (and more) are valid.
		// Also the finalizer must be valid, which is the case when using
		// the default constructor.
		
		// Prepare stages
		final int SEEKER_MASK = 0x1;
		final Seeker seeker = new PivotSeeker();
		final int SWEEPER_MASK = 0x2;
		final Sweeper sweeper = new BackwardLinearSweeper();
		final int STA_MASK = 0x4;
		final StaticAnalyser sta = new DefaultStaticAnalyser();
		final int SMA_MASK = 0x8;
		final SemanticAnalyser sma = new DefaultSemanticAnalyser();

		// 2^4 possibly combinations.
		boolean hasSeeker, hasSweeper, hasSTA, hasSMA;
		for (byte i = 0; i < (1 << 4); i++) {

			final Pipeline p = new Pipeline();

			hasSeeker = ((i & SEEKER_MASK) != 0);
			if (hasSeeker) {
				p.addStage(seeker);
			}
			hasSweeper = ((i & SWEEPER_MASK) != 0);
			if (hasSweeper) {
				p.addStage(sweeper);
			}
			hasSTA = ((i & STA_MASK) != 0);
			if (hasSTA) {
				p.addStage(sta);
			}
			hasSMA = ((i & SMA_MASK) != 0);
			if (hasSMA) {
				p.addStage(sma);
			}

			assertEquals(hasSeeker && hasSweeper && hasSTA && hasSMA, p.isValid());
		}
	}
	
	@Test
	public void Given_SubsetMandatoryStages_When_Executing_Expect_StageException() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException, StageException {
		// Reason: Validity of the pipeline must be checked before executing.
		// Valid combination will result in pipeline execution.
		final PipelineArgs args = createArgs(DexLoader.get().getMethodBytes());
		
		// Prepare stages
		final int SEEKER_MASK = 0x1;
		final Seeker seeker = new PivotSeeker();
		final int SWEEPER_MASK = 0x2;
		final Sweeper sweeper = new BackwardLinearSweeper();
		final int STA_MASK = 0x4;
		final StaticAnalyser sta = new DefaultStaticAnalyser();
		final int SMA_MASK = 0x8;
		final SemanticAnalyser sma = new DefaultSemanticAnalyser();

		// 2^4 possibly combinations.
		boolean hasSeeker, hasSweeper, hasSTA, hasSMA;
		for (byte i = 0; i < (1 << 4); i++) {

			// Default finalizer
			final Pipeline p = new Pipeline();

			hasSeeker = ((i & SEEKER_MASK) != 0);
			if (hasSeeker) {
				p.addStage(seeker);
			}
			hasSweeper = ((i & SWEEPER_MASK) != 0);
			if (hasSweeper) {
				p.addStage(sweeper);
			}
			hasSTA = ((i & STA_MASK) != 0);
			if (hasSTA) {
				p.addStage(sta);
			}
			hasSMA = ((i & SMA_MASK) != 0);
			if (hasSMA) {
				p.addStage(sma);
			}

			if (!(hasSeeker && hasSweeper && hasSTA && hasSMA)) {
				assertThrowsExactly(StageException.class, () -> p.execute(args));
			} else {
				p.execute(args);
			}
		}
	}
	
	@Test
	public void Given_DefaultPipeline_When_Validating_Expect_Valid() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException, StageException {
		// Reason: Using a predefined, default pipeline must always work.
		
		final PipelineArgs args = createArgs(DexLoader.get().getMethodBytes());
		final Pipeline p = Pipeline.createDefaultPipeline();
		assertTrue(p.isValid());
		
		// Must not throw
		p.execute(args);
	}
	
	@Test
	public void Given_DefaultPipeline_When_RemovingMandatoryStage_Expect_Invalid() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException {
		// Reason: Removing a mandatory stage without having an alternative must be invalid.
		
		final PipelineArgs args = createArgs(DexLoader.get().getMethodBytes());
		final Pipeline p = Pipeline.createDefaultPipeline();
		p.removeStage(0);	// remove first stage
		assertFalse(p.isValid());
		
		// Must not throw
		assertThrowsExactly(StageException.class, () -> p.execute(args));
	}
	
	@Test
	public void Given_DefaultPipeline_When_OverwritingFinalizer_Expect_Valid() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException, StageException {
		// Reason: Adding a custom finalizer to select custom info must not fail.
		
		final PipelineArgs args = createArgs(DexLoader.get().getMethodBytes());
		final Pipeline p = Pipeline.createDefaultPipeline();
		
		p.setFinalizer(PipelineResult::new);
		assertTrue(p.isValid());
		
		// Must not throw
		p.execute(args);
	}

	@Test
	public void Given_DefaultPipeline_When_AddingRemovingMandatoryStage_Expect_Valid() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException, StageException {
		// Reason: Adding and removing a mandatory stage must not result in an invalid state.
		
		final PipelineArgs args = createArgs(DexLoader.get().getMethodBytes());
		final Pipeline p = Pipeline.createDefaultPipeline();
		
		final Seeker seeker = new PivotSeeker();
		assertTrue(p.isValid());
		p.addStage(seeker);
		assertTrue(p.isValid());
		p.removeStage(seeker);
		assertTrue(p.isValid());
		
		// Must not throw
		p.execute(args);
	}
	
	@Test
	public void Given_DefaultPipeline_When_AddingDefaultMandatoryStageAndExecute_Expect_StageException() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException {
		// Reason: Default stages use their class names as ID with PipelineContext.
		// Thus, using two default stages results in duplicate IDs and thus an error
		// in PipelineArgs.putResult, which is wrapped in a StageException
		// due to the way Pipeline#execute handles RuntimeExceptions in Stages.
		
		final PipelineArgs args = createArgs(DexLoader.get().getMethodBytes());
		final Pipeline p = Pipeline.createDefaultPipeline();
		
		final Seeker seeker = new PivotSeeker();
		assertTrue(p.isValid());
		p.addStage(seeker);
		assertTrue(p.isValid());
		
		// Not using assertThrowsExactly, because the exact exception class
		// use may change in the future. However StageException is guaranteed.
		assertThrows(StageException.class, () -> p.execute(args));
	}
	
	@Test
	public void Given_DefaultPipeline_When_AddingCustomMandatoryStageAndExecute_Expect_Valid() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException, StageException {
		// Reason: Adding custom (mandatory) stages to default pipeline must be valid.
		
		final PipelineArgs args = createArgs(DexLoader.get().getMethodBytes());
		final Pipeline p = Pipeline.createDefaultPipeline();
		
		assertTrue(p.isValid());
		p.addStage(context -> {});
		assertTrue(p.isValid());
		
		p.execute(args);
	}
	
	@Test
	public void Given_DefaultPipeline_When_AddingCustomThrowingStageAndExecute_Expect_StageException() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException, StageException {
		// Reason: Custom stages may throw RuntimeExceptions, which must be wrapped
		// in a StageException.
		
		final PipelineArgs args = createArgs(DexLoader.get().getMethodBytes());
		final Pipeline p = Pipeline.createDefaultPipeline();
		
		assertTrue(p.isValid());
		p.addStage(context -> { throw new RuntimeException();});
		assertTrue(p.isValid());
		
		assertThrowsExactly(StageException.class, () -> p.execute(args));
	}
	
	@Test
	public void Given_EmptyPipeline_When_AddingStagesWrongOrder_Expect_StageException() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException {
		// Reason: Order of mandatory stages must be enforced.
		
		final PipelineArgs args = createArgs(DexLoader.get().getMethodBytes());
		final Pipeline p = new Pipeline();
		p.addStage(new PivotSeeker());
		p.addStage(new BackwardLinearSweeper());
		p.addStage(new DefaultSemanticAnalyser());
		p.addStage(new DefaultStaticAnalyser());
		
		assertFalse(p.isValid());
		assertThrowsExactly(StageException.class, () -> p.execute(args));
	}
}