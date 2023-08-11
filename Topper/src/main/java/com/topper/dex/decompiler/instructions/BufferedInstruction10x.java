package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction10x;

public class BufferedInstruction10x extends BufferedInstruction implements Instruction10x {

	public BufferedInstruction10x(DexBuffer buffer, Opcode opcode, int instructionStart) {
		super(opcode, instructionStart);
	}
}