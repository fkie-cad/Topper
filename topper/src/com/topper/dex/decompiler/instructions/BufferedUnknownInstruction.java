package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.UnknownInstruction;

public class BufferedUnknownInstruction extends BufferedInstruction implements UnknownInstruction {

	public BufferedUnknownInstruction(final DexBuffer buffer, final int instructionStart) {
		super(Opcode.NOP);
	}

	@Override
	public int getOriginalOpcode() {
		return 0;
	}
}