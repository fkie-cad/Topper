package com.topper.tests.utility;

import org.jf.dexlib2.Opcode;

import com.topper.configuration.TopperConfig;
import com.topper.exceptions.InvalidConfigException;

public class TestConfig {

	public static TopperConfig getDefault() {
		try {
			return new TopperConfig(10, Opcode.THROW, 8, 500000, 38, false);
		} catch (InvalidConfigException ignored) {
		}
		return null;
	}

}