package com.topper.configuration;

import java.nio.file.Path;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.eclipse.jdt.annotation.NonNull;

import com.topper.dex.decompilation.decompiler.Decompiler;
import com.topper.dex.decompilation.staticanalyser.StaticAnalyser;
import com.topper.dex.decompilation.sweeper.Sweeper;
import com.topper.exceptions.InvalidConfigException;

/**
 * Manager that handles loading {@link Config}s from given xml files and
 * provides a global access point to the globally unique configuration.
 * 
 * Note: One should use dependency injection to pass <code>Config</code>s to
 * configurable components to avoid global references to this manager.
 * 
 * @author Pascal Kühnemann
 * @since 14.08.2023
 */
public final class ConfigManager {

	private static ConfigManager instance;

	/**
	 * Below configurations are mandatory.
	 */
	private TopperConfig config;

	// Prevent default public constructor
	private ConfigManager() {

	}

	/**
	 * Gets the unique instance of this manager.
	 */
	@NonNull
	public static final ConfigManager get() {
		if (ConfigManager.instance == null) {
			ConfigManager.instance = new ConfigManager();
		}
		return ConfigManager.instance;
	}

	/**
	 * Gets the {@link GeneralConfig} configuration.
	 * 
	 * @throws UnsupportedOperationException If this method is called before
	 *                                       <code>loadConfig</code>.
	 */
	@NonNull
	public final GeneralConfig getGeneralConfig() {
		if (this.config == null) {
			throw new UnsupportedOperationException("Missing general config.");
		}
		return this.config.getGeneralConfig();
	}

	/**
	 * Gets the {@link StaticAnalyserConfig} configuration.
	 * 
	 * @throws UnsupportedOperationException If this method is called before
	 *                                       <code>loadConfig</code>.
	 * */
	@NonNull
	public final StaticAnalyserConfig getStaticAnalyserConfig() {
		if (this.config == null) {
			throw new UnsupportedOperationException("Missing static analyser config.");
		}
		return this.config.getStaticAnalyserConfig();
	}

	/**
	 * Gets the {@link SweeperConfig} configuration.
	 * 
	 * @throws UnsupportedOperationException If this method is called before
	 *                                       <code>loadConfig</code>.
	 * */
	@NonNull
	public final SweeperConfig getSweeperConfig() {
		if (this.config == null) {
			throw new UnsupportedOperationException("Missing sweeper config.");
		}
		return this.config.getSweeperConfig();
	}

	/**
	 * Gets the {@link DecompilerConfig} configuration.
	 * 
	 * @throws UnsupportedOperationException If this method is called before
	 *                                       <code>loadConfig</code>.
	 * */
	@NonNull
	public final DecompilerConfig getDecompilerConfig() {
		if (this.config == null) {
			throw new UnsupportedOperationException("Missing decompiler config.");
		}
		return this.config.getDecompilerConfig();
	}

	/**
	 * Gets the {@link TopperConfig} configuration. This is wrapper of all
	 * other configs to ease using dependency injection.
	 * 
	 * @throws UnsupportedOperationException If this method is called before
	 *                                       <code>loadConfig</code>.
	 * */
	@NonNull
	public final TopperConfig getConfig() {
		if (this.config == null) {
			throw new UnsupportedOperationException("Missing topper config.");
		}
		return this.config;
	}

	/**
	 * Loads the configurations to use from a given {@code path}. The configuration
	 * file must be an xml file and specify the following tags:
	 * <ul>
	 * <li>general: General information that applies to more than one
	 * component.</li>
	 * <li>static-analyser: Configurations for the {@link StaticAnalyser}.</li>
	 * <li>sweeper: Configurations for the {@link Sweeper}.</li>
	 * <li>decompiler: Configurations for the {@link Decompiler}.</li>
	 * </ul>
	 * Visit the respective configuration to see details on its tags.
	 * 
	 * If any tags are missing, they will be filled with default values. However, if
	 * e.g. an integer tag is filled with a string, then an
	 * {@link InvalidConfigException} is thrown.
	 * 
	 * @param path File path to the xml configuration file.
	 * @throws InvalidConfigException If loading configurations fails.
	 * @see DecompilerConfig
	 * @see GeneraliConfig
	 * @see StaticAnalyserConfig
	 * @see SweeperConfig
	 */
	public final void loadConfig(@NonNull final Path path) throws InvalidConfigException {

		try {

			final Parameters params = new Parameters();
			final FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<>(
					XMLConfiguration.class).configure(params.xml().setFileName(path.toString()) // use path
//							.setSchemaValidation(true));	// enable xml file validation
			);
			final XMLConfiguration xmlConfig = builder.getConfiguration();

			if (xmlConfig == null) {
				throw new InvalidConfigException("Failed to load config.");
			}

			// General Config
			final GeneralConfig generalConfig = new GeneralConfig();
			generalConfig.load(xmlConfig);

			// Static Analyser Config
			final StaticAnalyserConfig saConfig = new StaticAnalyserConfig();
			saConfig.load(xmlConfig);

			// Sweeper Config
			final SweeperConfig sweeperConfig = new SweeperConfig();
			sweeperConfig.load(xmlConfig);

			// Decompiler Config
			final DecompilerConfig dConfig = new DecompilerConfig();
			dConfig.load(xmlConfig);

			// Finally add them all to this manager
			this.config = new TopperConfig(generalConfig, saConfig, sweeperConfig, dConfig);

		} catch (final ConfigurationException e) {
			throw new InvalidConfigException("Failed to load config.", e);
		}
	}
}