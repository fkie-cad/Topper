package com.topper.tests.utility;

import java.nio.file.Paths;

import com.topper.configuration.ConfigManager;
import com.topper.configuration.TopperConfig;
import com.topper.exceptions.InvalidConfigException;

public class TestConfig {

	private static final String CONFIG_PATH = "./src/test/java/resources/test_config.xml";
	private static boolean loaded = false;
	
	public static TopperConfig getDefault() {
		try {
			if (!loaded) {
				ConfigManager.get().loadConfig(Paths.get(CONFIG_PATH));
				loaded = true;
			}
			return ConfigManager.get().getConfig();
		} catch (InvalidConfigException ignored) {
		}
		return null;
	}
}