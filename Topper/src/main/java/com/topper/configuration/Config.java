package com.topper.configuration;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConversionException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.topper.exceptions.InvalidConfigException;

/**
 * Abstract representation of a configuration. It manages loading configuration
 * information from a {@link XMLConfiguration} object by taking into account
 * configuration - specific tags.
 * 
 * @author Pascal Kühnemann
 * @since 14.08.2023
 */
public abstract class Config {

	/**
	 * Indicates whether {@link Config#load} finished successfully or not.
	 */
	private boolean hasLoaded;

	/**
	 * Loads registered configuration data from a given {@link XMLConfiguration}
	 * object.
	 * 
	 * Each subclass of this class must register configuration data by overwriting
	 * {@link Config#getElements}. These elements are used to search for tags in the
	 * given .xml file. If a tag matches a registered configuration, its value will
	 * be loaded into the subclass. Otherwise a default value will be loaded. Loading
	 * default values will always be done, if loading the actual value fails, e.g.
	 * due to type conversion errors.
	 * 
	 * It is possible to leave out unused tags. E.g. leaving out {@code <general>}
	 * forces {@link GeneralConfig} to use its default values.
	 * 
	 * A minimalistic configuration file may look like this:
	 * 
	 * <pre>{@code 
	 * <configuration>
	 * </configuration>
	 * }</pre>
	 * 
	 * @param config A {@code XMLConfiguration} containing information used by
	 *               individual components.
	 * @throws InvalidConfigException If a tag of a configuration subclass is
	 *                                invalid in the context of this application.
	 * @see ConfigElement
	 */
	public final void load(@NonNull final XMLConfiguration config) throws InvalidConfigException {

		// Parse all requested tags, if available.
		Getter<?> getter;
		for (@NonNull
		final ConfigElement<?> e : this.getElements()) {

			final String tag = this.getTag() + "." + e.getTag();
			getter = null;

			if (e.getDefault() instanceof Boolean) {
				getter = (defaultValue) -> {
					return config.getBoolean(tag, (boolean) e.getDefault());
				};
			} else if (e.getDefault() instanceof Integer) {
				getter = (defaultValue) -> {
					return config.getInt(tag, (int) e.getDefault());
				};
			} else if (e.getDefault() instanceof String) {
				getter = (defaultValue) -> {
					final String value = config.getString(tag, (String) e.getDefault());
					return (value != null) ? value : (String) e.getDefault();
				};
			}

			this.setValue(e, getter, e.getDefault());
		}

		// Loading is done.
		this.hasLoaded = true;
	}

	/**
	 * Checks whether {@link Config#load} has been executed successfully.
	 * 
	 * @throws UnsupportedOperationException If {@code load} has <b>not</b> been
	 *                                       executed yet or execution has not been
	 *                                       successful.
	 */
	public final void check() {
		if (!this.hasLoaded) {
			throw new UnsupportedOperationException("Cannot access config before loading it.");
		}
	}

	@SuppressWarnings("unchecked") // Actually it is checked, but not in this method
	private final <T> void setValue(@NonNull final ConfigElement<?> e, @Nullable final Getter<?> getter,
			@NonNull final T defaultValue) throws InvalidConfigException {

		if (getter != null) {
			try {
				((ConfigElement<T>) e).set(((Getter<T>) getter).get(defaultValue));

			} catch (final ConversionException ignored) {
				((ConfigElement<T>) e).set(defaultValue);
			}
		} else {
			((ConfigElement<T>) e).set(defaultValue);
		}
	}

	/**
	 * Gets the xml tag of this configuration class.
	 */
	@NonNull
	public abstract String getTag();

	/**
	 * Gets a list of configuration elements to be used during loading.
	 * 
	 * @see ConfigElement
	 */
	@NonNull
	public abstract ImmutableList<@NonNull ConfigElement<?>> getElements();

	private static interface Getter<S> {
		@NonNull
		S get(final S s);
	}
}