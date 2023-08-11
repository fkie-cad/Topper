package com.topper.configuration;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConversionException;
import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.exceptions.InvalidConfigException;

public abstract class Config {
	
	private boolean hasLoaded;
	
	public Config() {
		this.hasLoaded = false;
	}

	public final void load(@NonNull final XMLConfiguration config) throws InvalidConfigException {

		if (!config.containsKey(this.getTag())) {
			throw new InvalidConfigException(this.getTag() + " tag is missing.");
		}

		Getter<?> getter;
		for (@NonNull final ConfigElement<?> e : this.getElements()) {

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
			}

			if (getter != null) {
				this.setValue(e, getter, e.getDefault());
			}
		}
		
		this.hasLoaded = true;
	}
	
	public final void check() {
		if (!this.hasLoaded) {
			throw new UnsupportedOperationException("Cannot access config before loading it.");
		}
	}

	private final <T> void setValue(final ConfigElement<?> e, final Getter<?> getter, final T defaultValue) throws InvalidConfigException {

		try {
			((ConfigElement<T>)e).set(((Getter<T>)getter).get(defaultValue));
		} catch (final ConversionException ignored) {
			((ConfigElement<T>)e).set(defaultValue);
		}
	}

	@NonNull
	public abstract String getTag();

	@NonNull
	public abstract ImmutableList<@NonNull ConfigElement<?>> getElements();

	private static interface Getter<S> {
		@NonNull
		S get(final S s);
	}
}