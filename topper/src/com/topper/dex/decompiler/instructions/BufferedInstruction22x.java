package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction22x;

public class BufferedInstruction22x extends BufferedInstruction implements Instruction22x {

	private final int registerA;
	private final int registerB;
	
	public BufferedInstruction22x(final DexBuffer buffer, final Opcode opcode, final int instructionStartOffset) {
		super(opcode);
		
		this.registerA = buffer.readUbyte(instructionStartOffset + 1);
		this.registerB = buffer.readUshort(instructionStartOffset + 2);
	}

    @Override public int getRegisterA() { return this.registerA; }
    @Override public int getRegisterB() { return this.registerB; }
}