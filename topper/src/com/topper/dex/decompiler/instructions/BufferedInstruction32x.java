package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction32x;

public class BufferedInstruction32x extends BufferedInstruction implements Instruction32x {

	private final int registerA;
	private final int registerB;
	
	public BufferedInstruction32x(final DexBuffer buffer, final Opcode opcode, final int instructionStart) {
		super(opcode, instructionStart);
		
		this.registerA = buffer.readUshort(instructionStart + 2);
		this.registerB = buffer.readUshort(instructionStart + 4);
	}

    @Override public int getRegisterA() { return this.registerA; }
    @Override public int getRegisterB() { return this.registerB; }
}