package com.topper.configuration;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.topper.exceptions.InvalidConfigException;

/**
 * Configuration used by more than one component.
 * 
 * @author Pascal Kühnemann
 * @since 14.08.2023
 * */
public final class GeneralConfig extends Config {
	
	/**
	 * Default number of threads to use in case multi - threading is used to speed
	 * things up.
	 */
	private int defaultAmountThreads;
	
	/**
	 * Gets default number of threads to create in case multi - threading
	 * is used to speed things up.
	 * 
	 * Defaults to <code>1</code>.
	 * 
	 * @throws UnsupportedOperationException If {@link Config#load} has not been
	 *                                       executed yet or execution has not been
	 *                                       successful.
	 * */
	public final int getDefaultAmountThreads() {
		this.check();
		return this.defaultAmountThreads;
	}
	
	/**
	 * Sets the default amount of threads to use in case multi - threading is utilized.
	 * 
	 * @throws InvalidConfigException If {@code defaultAmountThreads <= 0}.
	 * */
	public final void setDefaultAmountThreads(final int defaultAmountThreads) throws InvalidConfigException {
		if (defaultAmountThreads <= 0) {
			throw new InvalidConfigException("defaultAmountThreads must be >= 1.");
		}
		this.defaultAmountThreads = defaultAmountThreads;
	}

	/**
	 * Gets the <code>"general"</code> tag.
	 * */
	@Override
	@NonNull 
	public String getTag() {
		return "general";
	}

	/**
	 * Gets a list of valid general configurations. E.g.
	 * <ul>
	 * <li>defaultAmountThreads(int)</li>
	 * </ul>
	 * */
	@Override
	@NonNull 
	public ImmutableList<@NonNull ConfigElement<?>> getElements() {
		return ImmutableList.of(
				new ConfigElement<Integer>("defaultAmountThreads", 1, this::setDefaultAmountThreads)
		);
	}
}