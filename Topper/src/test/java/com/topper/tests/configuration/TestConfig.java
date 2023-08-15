package com.topper.tests.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.Config;
import com.topper.configuration.ConfigElement;
import com.topper.exceptions.InvalidConfigException;

public class TestConfig {

	private static final String TMP_CONFIG_FILE_PATH = "./src/test/java/resources/tmp.xml";
	private static File configFile;

	private static final String VALID_CONFIG_HEADER = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>";
	private static final String CONFIG_START = "<configuration>";
	private static final String CONFIG_END = "</configuration>";

	private static final String VALID_EMPTY_CONFIG = VALID_CONFIG_HEADER + CONFIG_START + CONFIG_END;

	private static final String CONFIG_STRING_VALUE = "non-default";
	private static final String CONFIG_STRING = TmpConfig.STRING_START + CONFIG_STRING_VALUE + TmpConfig.STRING_END;
	private static final int CONFIG_INT_VALUE = 1337;
	private static final String CONFIG_INT = TmpConfig.INT_START + Integer.toString(CONFIG_INT_VALUE)
			+ TmpConfig.INT_END;
	private static final boolean CONFIG_BOOLEAN_VALUE = !TmpConfig.DEFAULT_BOOLEAN;
	private static final String CONFIG_BOOLEAN = TmpConfig.BOOLEAN_START + Boolean.toString(CONFIG_BOOLEAN_VALUE)
			+ TmpConfig.BOOLEAN_END;

	private static final String VALID_PARTIAL_CONFIG = VALID_CONFIG_HEADER + CONFIG_START + TmpConfig.TMP_START
			+ CONFIG_STRING + TmpConfig.TMP_END + CONFIG_END;

	private static final String PARTIAL_CONFIG_UNUSED_TAG_VALUE = "unused";
	private static final String UNUSED_START = "<" + PARTIAL_CONFIG_UNUSED_TAG_VALUE + ">";
	private static final String UNUSED_END = "</" + PARTIAL_CONFIG_UNUSED_TAG_VALUE + ">";
	private static final String CONFIG_UNUSED = UNUSED_START + PARTIAL_CONFIG_UNUSED_TAG_VALUE + UNUSED_END;
	private static final String VALID_PARTIAL_CONFIG_UNUSED_TAG = VALID_CONFIG_HEADER + CONFIG_START + TmpConfig.TMP_START
			+ CONFIG_STRING + CONFIG_UNUSED + TmpConfig.TMP_END + CONFIG_END;

	private static final String VALID_ALL_CONFIG = VALID_CONFIG_HEADER + CONFIG_START + TmpConfig.TMP_START
			+ CONFIG_STRING + CONFIG_INT + CONFIG_BOOLEAN + TmpConfig.TMP_END + CONFIG_END;

	private static final String INVALID_PARTIAL_CONFIG = VALID_CONFIG_HEADER + CONFIG_START + TmpConfig.TMP_START
			+ CONFIG_STRING + TmpConfig.INT_START + "42.314" + TmpConfig.INT_END + TmpConfig.TMP_END + CONFIG_END;

	private static class TmpConfig extends Config {
		public static final String TMP_START = "<tmp>";
		public static final String TMP_END = "</tmp>";
		public static final String STRING_START = "<string>";
		public static final String STRING_END = "</string>";
		public static final String INT_START = "<int>";
		public static final String INT_END = "</int>";
		public static final String BOOLEAN_START = "<boolean>";
		public static final String BOOLEAN_END = "</boolean>";

		public static final int DEFAULT_INT = 42;
		public static final String DEFAULT_STRING = "42";
		public static final boolean DEFAULT_BOOLEAN = false;

		@Override
		public @NonNull String getTag() {
			return "tmp";
		}

		private int i;

		private final void setInt(final int i) {
			this.i = i;
		}

		public final int getInt() {
			return this.i;
		}

		private String s;

		private final void setString(final String s) {
			this.s = s;
		}

		private final String getString() {
			return this.s;
		}

		private boolean b;

		private final void setBoolean(final boolean b) {
			this.b = b;
		}

		public final boolean getBoolean() {
			return this.b;
		}

		@Override
		public @NonNull ImmutableList<@NonNull ConfigElement<?>> getElements() {
			return ImmutableList.of(new ConfigElement<Integer>("int", DEFAULT_INT, this::setInt),
					new ConfigElement<String>("string", DEFAULT_STRING, this::setString),
					new ConfigElement<Boolean>("boolean", DEFAULT_BOOLEAN, this::setBoolean));
		}
	}
	
	private static final class InvalidConfig extends Config {
		
		public static final TmpConfig DEFAULT_CONFIG_VALUE = new TmpConfig();

		@Override
		public @NonNull String getTag() {
			return "invalid";
		}
		
		private TmpConfig c;
		public final TmpConfig getConfig() {return this.c;}
		private final void setConfig(@NonNull final TmpConfig c) {this.c = c;}

		@Override
		public @NonNull ImmutableList<@NonNull ConfigElement<?>> getElements() {
			return ImmutableList.of(
					new ConfigElement<TmpConfig>("config", DEFAULT_CONFIG_VALUE, this::setConfig)
			);
		}
	}

	@BeforeAll
	public static void init() {
		configFile = new File(TMP_CONFIG_FILE_PATH);
	}

	@AfterAll
	public static void clean() {
		if (configFile.exists()) {
			configFile.delete();
		}
	}

	@NonNull
	private static XMLConfiguration loadTmpConfig(@NonNull final String content)
			throws IOException, ConfigurationException {
		new FileOutputStream(configFile).close();
		final FileOutputStream out = new FileOutputStream(configFile);
		out.write(content.getBytes());
		out.flush();
		out.close();
		final XMLConfiguration config = new Configurations().xml(configFile);
		if (config == null) {
			throw new ConfigurationException();
		}
		return config;
	}

	private static final void checkTmpConfig(@NonNull final String configString, final int expectedInt,
			@NonNull final String expectedString, final boolean expectedBoolean)
			throws InvalidConfigException, IOException, ConfigurationException {

		final XMLConfiguration xml = loadTmpConfig(configString);
		final TmpConfig config = new TmpConfig();

		config.load(xml);

		// Must not throw
		config.check();

		// Check default values
		assertEquals(expectedInt, config.getInt());
		assertEquals(expectedString, config.getString());
		assertEquals(expectedBoolean, config.getBoolean());
	}

	@Test
	public void Given_ValidEmptyConfig_When_Loading_Expect_AllDefault()
			throws IOException, ConfigurationException, InvalidConfigException {
		// Reason: Not specified configs must be initialized with default values.

		checkTmpConfig(VALID_EMPTY_CONFIG, TmpConfig.DEFAULT_INT, TmpConfig.DEFAULT_STRING, TmpConfig.DEFAULT_BOOLEAN);
	}

	@Test
	public void Given_ValidPartialConfig_When_Loading_Expect_MixedSpecifiedAndDefault()
			throws IOException, ConfigurationException, InvalidConfigException {
		// Reason: Defined values in a config must be preferred over default values.

		checkTmpConfig(VALID_PARTIAL_CONFIG, TmpConfig.DEFAULT_INT, CONFIG_STRING_VALUE, TmpConfig.DEFAULT_BOOLEAN);
	}

	@Test
	public void Given_AllConfig_When_Loading_Expect_OnlySpecified()
			throws IOException, ConfigurationException, InvalidConfigException {
		// Reason: Parsing different types must work. Also prefer specified over default
		// values.

		checkTmpConfig(VALID_ALL_CONFIG, CONFIG_INT_VALUE, CONFIG_STRING_VALUE, CONFIG_BOOLEAN_VALUE);
	}

	@Test
	public void Given_ValidPartialConfigUnusedTag_When_Loading_Expect_UnusedTagIgnored()
			throws InvalidConfigException, IOException, ConfigurationException {
		// Reason: Silently ignore unused tags.

		checkTmpConfig(VALID_PARTIAL_CONFIG_UNUSED_TAG, TmpConfig.DEFAULT_INT, CONFIG_STRING_VALUE, TmpConfig.DEFAULT_BOOLEAN);
	}

	@Test
	public void Given_UnsupportedTypeUsedTag_When_Loading_Expect_DefaultValue()
			throws IOException, ConfigurationException, InvalidConfigException {
		// Reason: Invalidly used tags must result in error.
		// If tag was unused, it would be ignored. But misusing a valid tag
		// should be mentioned to the user. Also, the former is hard to check.

		checkTmpConfig(INVALID_PARTIAL_CONFIG, TmpConfig.DEFAULT_INT, CONFIG_STRING_VALUE, TmpConfig.DEFAULT_BOOLEAN);
	}
	
	@Test
	public void Given_ValidPartialConfig_When_LoadingConfigWithUnsupportedType_Expect_DefaultValue() throws IOException, ConfigurationException, InvalidConfigException {
		// Reason: Configs that use types that cannot be parsed from xml file must
		// always fall back to their default value.
		
		final XMLConfiguration xml = loadTmpConfig(VALID_PARTIAL_CONFIG);
		final InvalidConfig config = new InvalidConfig();

		config.load(xml);

		// Must not throw
		config.check();

		// Check default values
		assertEquals(InvalidConfig.DEFAULT_CONFIG_VALUE, config.getConfig());
	}
	
	@Test
	public void Given_NonLoadedConfig_When_Checking_Expect_UnsupportedOperationException() throws IOException, ConfigurationException {
		// Reason: Prevents using the configuration before loading finished.
		
		final XMLConfiguration xml = loadTmpConfig(VALID_PARTIAL_CONFIG);
		final TmpConfig config = new TmpConfig();
		
		assertThrowsExactly(UnsupportedOperationException.class, () -> config.check());
	}
	
	@Test
	public void Given_LoadedConfig_When_Checking_Expect_NoException() throws IOException, ConfigurationException, InvalidConfigException {
		// Reason: After loading finished, configuration is usable.
		
		final XMLConfiguration xml = loadTmpConfig(VALID_PARTIAL_CONFIG);
		final TmpConfig config = new TmpConfig();
		
		config.load(xml);
		config.check();
	}
}