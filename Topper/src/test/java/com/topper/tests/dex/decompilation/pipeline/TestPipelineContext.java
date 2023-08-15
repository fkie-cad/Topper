package com.topper.tests.dex.decompilation.pipeline;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.topper.configuration.TopperConfig;
import com.topper.dex.decompilation.pipeline.PipelineArgs;
import com.topper.dex.decompilation.pipeline.PipelineContext;
import com.topper.dex.decompilation.pipeline.SeekerInfo;
import com.topper.exceptions.InvalidConfigException;
import com.topper.exceptions.MissingStageInfoException;
import com.topper.tests.utility.DexLoader;
import com.topper.tests.utility.TestConfig;

public class TestPipelineContext {

	private static TopperConfig config;
	
	@BeforeAll
	public static void init() throws InvalidConfigException {
		config = TestConfig.getDefault();
	}
	private static final PipelineContext createContext() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException {
		final PipelineArgs args = new PipelineArgs(config, DexLoader.get().getMethodBytes());
		return new PipelineContext(args);
	}
	
	@Test
	public void Given_ArgsContext_When_GettingNonexistingKey_Expect_MissingStageInfoException() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException {
		// Reason: Requesting non - existing data must fail.
		
		final PipelineContext c = createContext();
		assertThrowsExactly(MissingStageInfoException.class, () -> c.getResult(""));
	}
	
	@Test
	public void Given_ArgsContext_When_GettingArgs_Expect_ValidArgs() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException, MissingStageInfoException {
		// Reason: Fetching pipeline args must work, regardless of whether
		// a convenience method is used or not.
		
		final PipelineContext c = createContext();
		assertInstanceOf(PipelineArgs.class, c.getArgs());
		assertInstanceOf(PipelineArgs.class, c.getResult("PipelineArgs"));
	}
	
	@Test
	public void Given_ArgsContext_When_GettingArgsTypeMismatch_Expect_MissingStageException() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvalidConfigException, IOException {
		// Reason: If stage expects a particular type for a given key, getting
		// a different type must fail --> (key, type) maps to value.
		
		final PipelineContext c = createContext();
		
		assertThrowsExactly(MissingStageInfoException.class, () -> {
			final SeekerInfo info = c.getResult("PipelineArgs");
		});
		
	}
}