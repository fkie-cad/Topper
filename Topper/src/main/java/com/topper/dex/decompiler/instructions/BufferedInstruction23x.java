package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction23x;

public class BufferedInstruction23x extends BufferedInstruction implements Instruction23x {

	private final int registerA;
	private final int registerB;
	private final int registerC;
	
	public BufferedInstruction23x(final DexBuffer buffer, final Opcode opcode, final int instructionStart) {
		super(opcode, instructionStart);
		
		this.registerA = buffer.readUbyte(instructionStart + 1);
		this.registerB = buffer.readUbyte(instructionStart + 2);
		this.registerC = buffer.readUbyte(instructionStart + 3);
	}

    @Override public int getRegisterA() { return this.registerA; }
    @Override public int getRegisterB() { return this.registerB; }
    @Override public int getRegisterC() { return this.registerC; }
}