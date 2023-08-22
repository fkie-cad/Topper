package com.topper.tests.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.lang.reflect.Field;
import java.nio.file.Paths;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.topper.configuration.ConfigManager;
import com.topper.exceptions.InvalidConfigException;
import com.topper.tests.utility.IOHelper;

public class TestConfigManager {
	
	private static final String RESOURCE_PATH = "./src/test/java/resources/";
	
	private static final String VALID_CONFIG_NAME = "test_config.xml";
	private static final String VALID_CONFIG_PATH = RESOURCE_PATH + VALID_CONFIG_NAME;

	private static final String VALID_FILE_INVALID_CONFIG_NAME = "classes7.dex";
	private static final String INVALID_CONFIG_PATH = RESOURCE_PATH + VALID_FILE_INVALID_CONFIG_NAME;
	
	private static final ConfigManager manager = ConfigManager.get();
	
	private static void nullField(@NonNull final Object obj, @NonNull final String fieldName) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		final Field target = obj.getClass().getDeclaredField(fieldName);
		if (target != null) {
			target.setAccessible(true);
			target.set(obj, null);
			target.setAccessible(false);
		}
	}
	
	@BeforeEach
	public void unloadConfig() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		nullField(manager, "config");
	}
	
	@BeforeAll
	public static void clearStreams() {
		IOHelper.get().clearOut();
		IOHelper.get().clearErr();
	}
	
	@AfterAll
	public static void restoreStreams() {
		IOHelper.get().restoreOut();
		IOHelper.get().restoreErr();
	}
	
	@Test
	public void Given_PathValidConfig_When_Loading_Expect_ValidConfigs() throws InvalidConfigException {
		// Reason: Loading valid configuration file must always work.
		
		manager.loadConfig(Paths.get(VALID_CONFIG_PATH));
		assertNotNull(manager.getConfig());
		assertNotNull(manager.getGeneralConfig());
		assertNotNull(manager.getDecompilerConfig());
		assertNotNull(manager.getSweeperConfig());
		assertNotNull(manager.getStaticAnalyserConfig());
	}
	
	@Test
	public void Given_InvalidPath_When_Loading_Expect_InvalidConfigException() {
		// Reason: Path to config file must be valid.
		
		assertThrowsExactly(InvalidConfigException.class, () -> manager.loadConfig(Paths.get("./invalid_path")));
	}
	
	@Test
	public void Given_PathValidConfig_When_AccessConfigBeforeLoad_Expect_UnsupportedOperationException() {
		// Reason: Prevents accessing configurations before they are loaded.
		
		assertThrowsExactly(UnsupportedOperationException.class, () -> manager.getConfig());
		assertThrowsExactly(UnsupportedOperationException.class, () -> manager.getGeneralConfig());
		assertThrowsExactly(UnsupportedOperationException.class, () -> manager.getDecompilerConfig());
		assertThrowsExactly(UnsupportedOperationException.class, () -> manager.getSweeperConfig());
		assertThrowsExactly(UnsupportedOperationException.class, () -> manager.getStaticAnalyserConfig());
	}
	
	@Test
	public void Given_PathInvalidConfig_When_Loading_Expect_InvalidConfigException() throws InvalidConfigException {
		// Reason: Configuration file must be .xml and parsable.
		
		assertThrowsExactly(InvalidConfigException.class, () ->  manager.loadConfig(Paths.get(INVALID_CONFIG_PATH)));
	}
}