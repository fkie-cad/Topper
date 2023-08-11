package com.topper.configuration;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.exceptions.InvalidConfigException;

public final class GeneralConfig extends Config {
	
	/**
	 * Default number of threads to use in case multi - threading is used to speed
	 * things up.
	 */
	private int defaultAmountThreads;
	
	/**
	 * Gets default number of threads to create in case multi - threading
	 * is used to speed things up.
	 * */
	public final int getDefaultAmountThreads() {
		this.check();
		return this.defaultAmountThreads;
	}
	
	private final void setDefaultAmountThreads(final int defaultAmountThreads) throws InvalidConfigException {
		if (defaultAmountThreads <= 0) {
			throw new InvalidConfigException("defaultAmountThreads must be >= 1.");
		}
		this.defaultAmountThreads = defaultAmountThreads;
	}

	@Override
	@NonNull 
	public String getTag() {
		return "general";
	}

	@Override
	@NonNull 
	public ImmutableList<@NonNull ConfigElement<?>> getElements() {
		return ImmutableList.of(
				new ConfigElement<Integer>("defaultAmountThreads", 1, this::setDefaultAmountThreads)
		);
	}
}