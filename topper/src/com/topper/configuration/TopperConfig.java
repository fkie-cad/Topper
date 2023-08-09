package com.topper.configuration;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Opcode;

import com.topper.exceptions.InvalidConfigException;

public final class TopperConfig {

	/**
	 * Upper bound on the number of instructions that may precede a pivot
	 * instruction (e.g. throw v0) including the pivot instruction. E.g. if a gadget
	 * needs to have 4 operations and a throw, then this value must be at least 5.
	 */
	private int sweeperMaxNumberInstructions;

	/**
	 * Instruction that signals the end of a gadget.
	 */
	@NonNull
	private Opcode pivotInstruction;

	/**
	 * Default number of threads to use in case multi - threading is used to speed
	 * things up.
	 */
	private int defaultAmountThreads;

	/**
	 * Threshold for .dex file sizes in a .vdex file. If this is exceeded, then
	 * analysis will skip the respective .dex file. Negative value indicates no threshold.
	 * 0 can be used to disable analysis for all .dex files.
	 */
	private int vdexSkipThreshold;

	public TopperConfig(final int sweeperMaxNumberInstructions, @NonNull final Opcode pivotOpcode,
			final int defaultAmountThreads, final int vdexSkipThreshold) throws InvalidConfigException {

		if (sweeperMaxNumberInstructions <= 0) {
			throw new InvalidConfigException("sweeperMaxNumberInstructions must be >= 1.");
		}
		this.sweeperMaxNumberInstructions = sweeperMaxNumberInstructions;

		this.pivotInstruction = pivotOpcode;
		this.defaultAmountThreads = defaultAmountThreads;
	}

	/**
	 * Gets current upper bound on the number of instructions to obtain from a
	 * sweeper.
	 */
	public final int getSweeperMaxNumberInstructions() {
		return sweeperMaxNumberInstructions;
	}

	/**
	 * Sets the upper bound on the number of instructions to obtain from a sweeper.
	 */
	public final void setSweeperMaxNumberInstructions(final int sweeperMaxNumberInstructions) {
		this.sweeperMaxNumberInstructions = sweeperMaxNumberInstructions;
	}

	/**
	 * Gets current pivot instruction that signals the end of a gadget.
	 */
	@NonNull
	public Opcode getPivotInstruction() {
		return pivotInstruction;
	}

	/**
	 * Sets the pivot instruction that signals the end of a gadget.
	 */
	public void setPivotInstruction(@NonNull final Opcode pivotInstruction) {
		this.pivotInstruction = pivotInstruction;
	}

	/**
	 * Gets default number of threads to create in case multi - threading
	 * is used to speed things up.
	 * */
	public final int getDefaultAmountThreads() {
		return this.defaultAmountThreads;
	}

	/**
	 * Sets default number of threads to create in case multi - threading
	 * is used to speed things up.
	 * */
	public final void setDefaultAmountThreads(final int defaultAmountThreads) {
		this.defaultAmountThreads = defaultAmountThreads;
	}

	/**
	 * Gets threshold for .dex file sizes in a .vdex file. If this is exceeded, then
	 * analysis will skip the respective .dex file. Negative value indicates no threshold.
	 * 0 can be used to disable analysis for all .dex files.
	 * */
	public int getVdexSkipThreshold() {
		return vdexSkipThreshold;
	}

	/**
	 * Sets threshold for .dex file sizes in a .vdex file. If this is exceeded, then
	 * analysis will skip the respective .dex file. Negative value indicates no threshold.
	 * 0 can be used to disable analysis for all .dex files.
	 * 
	 * @param vdexSkipThreshold New threshold on .dex file sizes in .vdex files. 0
	 * 	indicates no analysis. Negative value indicates no threshold.
	 * */
	public void setVdexSkipThreshold(int vdexSkipThreshold) {
		this.vdexSkipThreshold = vdexSkipThreshold;
	}
}