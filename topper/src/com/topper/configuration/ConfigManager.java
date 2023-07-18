package com.topper.configuration;

import org.jf.dexlib2.Opcode;

import com.topper.exceptions.InvalidConfigException;

public final class ConfigManager {

	private static ConfigManager instance;
	
	private final TopperConfig config;
	
	private ConfigManager() {
		this.config = this.loadConfig();
	}
	
	public static final ConfigManager getInstance() {
		if (ConfigManager.instance == null) {
			ConfigManager.instance = new ConfigManager();
		}
		return ConfigManager.instance;
	}
	
	public final TopperConfig getConfig() {
		return this.config;
	}
	
	private final TopperConfig loadConfig() {
		// TODO: Load from file or something
		try {
			return new TopperConfig(10, Opcode.THROW);
		} catch (InvalidConfigException e) {
			System.exit(0);
			return null;
		}
	}
}
