package com.topper.configuration;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.exceptions.InvalidConfigException;

public final class ConfigElement<T> {

	@NonNull
	private final String tag;
	
	@NonNull
	private final T value;
	
	@NonNull
	private final Setter<T> setter;
	
	public ConfigElement(@NonNull final String tag, @NonNull final T value, @NonNull final Setter<T> setter) {
		this.tag = tag;
		this.value = value;
		this.setter = setter;
	}
	
	@NonNull
	public final String getTag() {
		return this.tag;
	}
	
	@NonNull
	public final T getDefault() {
		return this.value;
	}
	
	public final void set(final T value) throws InvalidConfigException {
		this.setter.set(value);
	}
	
	public static interface Setter<S> {
		void set(final S value) throws InvalidConfigException;
	}
}