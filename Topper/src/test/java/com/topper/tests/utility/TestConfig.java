package com.topper.tests.utility;

import java.nio.file.Paths;

import com.topper.configuration.ConfigManager;
import com.topper.configuration.TopperConfig;
import com.topper.exceptions.InvalidConfigException;

public class TestConfig {

	private static final String CONFIG_PATH = "./src/test/java/resources/test_config.xml";

	public static TopperConfig getDefault() throws InvalidConfigException {
		ConfigManager.get().loadConfig(Paths.get(CONFIG_PATH));
		return ConfigManager.get().getConfig();
	}
}