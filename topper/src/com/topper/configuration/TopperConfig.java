package com.topper.configuration;

import org.jf.dexlib2.Opcode;

import com.topper.exceptions.InvalidConfigException;

public class TopperConfig {

	/**
	 * Upper bound on the number of instructions that
	 * may precede a pivot instruction (e.g. throw v0)
	 * including the pivot instruction. E.g. if a
	 * gadget needs to have 4 operations and a throw,
	 * then this value must be at least 5.
	 * */
	private int sweeperMaxNumberInstructions;
	
	/**
	 * Instruction that signals the end of a gadget.
	 * */
	private Opcode pivotInstruction;
	
	public TopperConfig(final int sweeperMaxNumberInstructions, final Opcode pivotOpcode) throws InvalidConfigException {
		
		if (sweeperMaxNumberInstructions <= 0) {
			throw new InvalidConfigException("sweeperMaxNumberInstructions must be >= 1.");
		}
		this.sweeperMaxNumberInstructions = sweeperMaxNumberInstructions;
		
		this.pivotInstruction = pivotOpcode;
	}
	
	/**
	 * Gets current upper bound on the number of instructions
	 * to obtain from a sweeper.
	 * */
	public final int getSweeperMaxNumberInstructions() {
		return sweeperMaxNumberInstructions;
	}

	/**
	 * Sets the upper bound on the number of instructions to
	 * obtain from a sweeper.
	 * */
	public final void setSweeperMaxNumberInstructions(final int sweeperMaxNumberInstructions) {
		this.sweeperMaxNumberInstructions = sweeperMaxNumberInstructions;
	}

	/**
	 * Gets current pivot instruction that signals the
	 * end of a gadget.
	 * */
	public Opcode getPivotInstruction() {
		return pivotInstruction;
	}

	/**
	 * Sets the pivot instruction that signals
	 * the end of a gadget.
	 * */
	public void setPivotInstruction(Opcode pivotInstruction) {
		this.pivotInstruction = pivotInstruction;
	}
}