package com.topper.configuration;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Opcode;

import com.google.common.collect.ImmutableList;
import com.topper.exceptions.InvalidConfigException;

public class SweeperConfig extends Config {
	
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
	 * Defaults to 10.
	 */
	public final int getSweeperMaxNumberInstructions() {
		return this.maxNumberInstructions;
	}
	
	public final void setMaxNumberInstructions(final int maxNumberInstructions) throws InvalidConfigException {
		if (maxNumberInstructions <= 0) {
			throw new InvalidConfigException("maxNumberInstructions must be >= 1.");
		}
		this.maxNumberInstructions = maxNumberInstructions;
	}

	/**
	 * Gets current pivot instruction that signals the end of a gadget.
	 * Defaults to {@code Opcode.THROW}.
	 */
	@NonNull
	public final Opcode getPivotOpcode() {
		this.check();
		return this.pivotOpcode;
	}
	
	public final void setPivotOpcode(final String pivotOpcodeName) throws InvalidConfigException {
		try {
			this.pivotOpcode = Opcode.valueOf(pivotOpcodeName);
		} catch (final Exception e) {
			throw new InvalidConfigException(pivotOpcodeName + " is unknown.");
		}
	}

	@Override
	@NonNull 
	public String getTag() {
		return "sweeper";
	}

	@Override
	@NonNull 
	public ImmutableList<@NonNull ConfigElement<?>> getElements() {
		return ImmutableList.of(
				new ConfigElement<Integer>("maxNumberInstructions", 10, this::setMaxNumberInstructions),
				new ConfigElement<String>("pivotOpcode", "throw", this::setPivotOpcode)
		);
	}
}