package com.topper.configuration;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Opcode;

import com.google.common.collect.ImmutableList;
import com.topper.exceptions.InvalidConfigException;

/**
 * Configuration used by {@link Sweeper}. It includes, but is not limited to, a
 * definition for a pivot instruction (often <code>Opcode.THROW</code>).
 * 
 * @author Pascal Kühnemann
 * @since 14.08.2023
 */
public class SweeperConfig extends Config {
	
	private static final int UPPER_BOUND_MAX_NUMBER_INSTRUCTIONS = 100;

	/**
	 * Upper bound on the number of instructions that may precede a pivot
	 * instruction (e.g. throw v0) including the pivot instruction. E.g. if a gadget
	 * needs to have 4 operations and a throw, then this value must be at least 5.
	 */
	private int maxNumberInstructions;

	/**
	 * Opcode that signals the end of a gadget.
	 */
	private Opcode pivotOpcode;

	/**
	 * Gets current upper bound on the number of instructions to obtain from a
	 * sweeper.
	 * 
	 * Defaults to <code>10</code>. Upper - bounded by <code>100</code>.
	 * 
	 * @throws UnsupportedOperationException If {@link Config#load} has not been
	 *                                       executed yet or execution has not been
	 *                                       successful.
	 */
	public final int getMaxNumberInstructions() {
		this.check();
		return this.maxNumberInstructions;
	}

	/**
	 * Sets the maximum number of instructions to extract during a sweep.
	 * 
	 * Upper - bounded by <code>100</code>.
	 * 
	 * @throws InvalidConfigException If {@code maxNumberInstructions <= 0}.
	 */
	public final void setMaxNumberInstructions(final int maxNumberInstructions) throws InvalidConfigException {
		if (maxNumberInstructions <= 0) {
			throw new InvalidConfigException("maxNumberInstructions must be >= 1.");
		} else if (maxNumberInstructions > UPPER_BOUND_MAX_NUMBER_INSTRUCTIONS) {
			throw new InvalidConfigException("maxNumberInstructions must be <= 100");
		}
		this.maxNumberInstructions = maxNumberInstructions;
	}

	/**
	 * Gets current pivot instruction that signals the end of a gadget.
	 * 
	 * Defaults to <code>Opcode.THROW</code>.
	 * 
	 * @throws UnsupportedOperationException If {@link Config#load} has not been
	 *                                       executed yet or execution has not been
	 *                                       successful.
	 */
	@NonNull
	public final Opcode getPivotOpcode() {
		this.check();
		return this.pivotOpcode;
	}

	/**
	 * Sets the pivot opcode to use during sweeping by name.
	 * 
	 * @throws InvalidConfigException If <code>pivotOpcodeName</code> is not a valid
	 *                                {@link Opcode}.
	 */
	public final void setPivotOpcode(@NonNull final String pivotOpcodeName) throws InvalidConfigException {
		try {
			this.pivotOpcode = Opcode.valueOf(pivotOpcodeName.toUpperCase());
		} catch (final Exception e) {
			throw new InvalidConfigException(pivotOpcodeName + " is unknown.");
		}
	}

	/**
	 * Gets the <code>"sweeper"</code> tag.
	 */
	@Override
	@NonNull
	public String getTag() {
		return "sweeper";
	}

	/**
	 * Gets a list of valid {@link Sweeper} configurations. E.g.
	 * <ul>
	 * <li>maxNumberInstructions(int)</li>
	 * <li>pivotOpcode(String)</code>
	 * </ul>
	 */
	@SuppressWarnings("null")
	@Override
	@NonNull
	public ImmutableList<@NonNull ConfigElement<?>> getElements() {
		return ImmutableList.of(new ConfigElement<Integer>("maxNumberInstructions", 10, this::setMaxNumberInstructions),
				new ConfigElement<@NonNull String>("pivotOpcode", "throw", this::setPivotOpcode));
	}
	
	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append("Sweeper Config:" + System.lineSeparator());
		b.append("- maxNumberInstructions: " + this.getMaxNumberInstructions() + System.lineSeparator());
		b.append("- pivotOpcode: " + this.getPivotOpcode().name + System.lineSeparator());
		return b.toString();
	}
}