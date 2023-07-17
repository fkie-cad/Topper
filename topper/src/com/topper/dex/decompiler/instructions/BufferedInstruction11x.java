package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction11x;

public class BufferedInstruction11x extends BufferedInstruction implements Instruction11x {

	private final int registerA;
	
	public BufferedInstruction11x(DexBuffer buffer, Opcode opcode, int instructionStart) {
		super(opcode);
		
		this.registerA = buffer.readUbyte(instructionStart + 1);
	}

	@Override
	public int getRegisterA() {
		return this.registerA;
	}
}