package com.topper.tests.dex.decompilation.staticanalyser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.topper.configuration.TopperConfig;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompilation.pipeline.DecompilationDriver;
import com.topper.dex.decompilation.pipeline.Pipeline;
import com.topper.dex.decompilation.pipeline.PipelineArgs;
import com.topper.dex.decompilation.pipeline.PipelineContext;
import com.topper.dex.decompilation.pipeline.StageInfo;
import com.topper.dex.decompilation.pipeline.StaticInfo;
import com.topper.dex.decompilation.pipeline.SweeperInfo;
import com.topper.dex.decompilation.seeker.PivotSeeker;
import com.topper.dex.decompilation.seeker.Seeker;
import com.topper.dex.decompilation.staticanalyser.DefaultStaticAnalyser;
import com.topper.dex.decompilation.staticanalyser.Gadget;
import com.topper.dex.decompilation.staticanalyser.StaticAnalyser;
import com.topper.dex.decompilation.sweeper.BackwardLinearSweeper;
import com.topper.dex.decompilation.sweeper.Sweeper;
import com.topper.exceptions.InvalidConfigException;
import com.topper.exceptions.MissingStageInfoException;
import com.topper.exceptions.StageException;
import com.topper.tests.utility.DexLoader;
import com.topper.tests.utility.TestConfig;

public class TestDefaultStaticAnalyser {
	
	private static TopperConfig config;
	
	@NonNull
	private static final PipelineContext createContext(final byte @NonNull [] bytecode) throws NoSuchFieldException,
			SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException, StageException {
		final PipelineArgs args = new PipelineArgs(config, bytecode);
		final Seeker seeker = new PivotSeeker();
		final PipelineContext context = new PipelineContext(args);
		seeker.execute(context);
		final Sweeper sweeper = new BackwardLinearSweeper();
		sweeper.execute(context);
		return context;
	}
	
	private static final StaticAnalyser create() {
		return new DefaultStaticAnalyser();
	}
	
	@BeforeAll
	public static void init() throws InvalidConfigException {
		config = TestConfig.getDefault();
	}
	
	@BeforeEach
	public void reset() {
		config.getStaticAnalyserConfig().setSkipCFG(false);
		config.getStaticAnalyserConfig().setSkipDFG(false);
	}

	/**
	 * Simulates {@link Pipeline} until reaching static analysis.
	 * @throws InvalidConfigException 
	 */
	private static final PipelineContext createResults() throws NoSuchFieldException,
			SecurityException, IllegalArgumentException, IllegalAccessException, IOException, StageException, InvalidConfigException {
		final PipelineArgs args = new PipelineArgs(config, DexLoader.get().getMethodBytes());
		PipelineContext context = new PipelineContext(args);
		
		final Pipeline pipeline = new Pipeline();
		pipeline.addStage(new PivotSeeker());
		pipeline.addStage(new BackwardLinearSweeper());
		final DecompilationDriver driver = new DecompilationDriver();
		driver.setPipeline(pipeline);
		context = driver.decompile(args).getContext();

		assertNotNull(context.getArgs());
		assertNotNull(context.getInfo(BackwardLinearSweeper.class.getSimpleName()));

		return context;
	}

	private static final void verifyResults(@NonNull final TreeMap<@NonNull String, @NonNull StageInfo> results) {
		// Assumption: Static analyser has been executed successfully.

		assertTrue(results.containsKey(PipelineArgs.class.getSimpleName()));
		assertTrue(results.containsKey(SweeperInfo.class.getSimpleName()));
		assertTrue(results.containsKey(StaticInfo.class.getSimpleName()));

		final PipelineArgs args = (PipelineArgs) results.get(PipelineArgs.class.getSimpleName());
		final SweeperInfo sweeperInfo = (SweeperInfo) results.get(SweeperInfo.class.arrayType().getSimpleName());
		final StaticInfo staticInfo = (StaticInfo) results.get(StaticInfo.class.getSimpleName());

		for (@NonNull
		final Gadget gadget : staticInfo.getGadgets()) {
			// Completeness: Check that each gadget covers an instruction sequence of the
			// sweeper
			assertEquals(1, sweeperInfo.getInstructionSequences().stream()
					.filter(sequence -> sequence.equals(gadget.getInstructions())).count());
			
			// Correctness: Check that basic block and entry coincide.
			final CFG cfg = gadget.getCFG();
			assertNotNull(cfg);
			assertTrue(gadget.hasCFG());	// cfg != null
			assertEquals(gadget.getInstructions().get(0).getOffset(), cfg.getEntry());
			assertEquals(1, cfg.getGraph().nodes().stream().filter(bb -> bb.getOffset() == cfg.getEntry()).count());
		}
	}

	@Test
	public void Given_PartialContext_When_Executing_Expect_MissingStageInfoException() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException {
		// Reason: Static analyser must verify that required information is available.

		// Seeker and Sweeper is missing!
		final PipelineArgs args = new PipelineArgs(config, DexLoader.get().getMethodBytes());
		final PipelineContext context = new PipelineContext(args);
		
		final StaticAnalyser analyser = create();
		assertThrowsExactly(MissingStageInfoException.class,
				() -> analyser.execute(context));
	}

	@Test
	public void Given_ValidContext_When_ExecutingAll_Expect_ValidStaticInfo() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException, StageException {
		// Reason: Static analyser must add a valid result to context.
		
		// No skipping
		config.getStaticAnalyserConfig().setSkipCFG(false);
		config.getStaticAnalyserConfig().setSkipDFG(false);
		
		final byte[] buffer = DexLoader.get().getMethodBytes();
		final PipelineContext c = createContext(buffer);
		
		final StaticAnalyser analyser = create();
		
		// Must not throw
		analyser.execute(c);
		
		final StaticInfo info = c.getStaticInfo(StaticInfo.class.getSimpleName());
		
		assertTrue(info.getGadgets().size() >= 1);
		for (final Gadget gadget : info.getGadgets()) {
			assertTrue(gadget.getInstructions().size() >= 1);
			assertNotNull(gadget.getCFG());
			assertNotNull(gadget.getDFG());
		}
	}
	
	// TODO: CONTINUE WRITING STATIC ANALYSER TESTS
}