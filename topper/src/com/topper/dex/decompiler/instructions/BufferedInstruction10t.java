package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction10t;

public class BufferedInstruction10t extends BufferedInstruction implements Instruction10t {
	
	private final int offset;

	public BufferedInstruction10t(DexBuffer buffer, Opcode opcode, int instructionStart) {
		super(opcode);
		
		this.offset = buffer.readByte(instructionStart + 1);
	}

	@Override
	public int getCodeOffset() {
		return this.offset;
	}
}