package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.UnknownInstruction;

public class BufferedUnknownInstruction extends BufferedInstruction implements UnknownInstruction {

	private int opcode;
	
	public BufferedUnknownInstruction(final DexBuffer buffer, final int instructionStart) {
		super(Opcode.NOP, instructionStart);
		
		opcode = buffer.readUbyte(instructionStart);
		if (opcode == 0) {
			opcode = buffer.readUshort(instructionStart);
		}
	}

	@Override
	public int getOriginalOpcode() {
		return opcode;
	}
}