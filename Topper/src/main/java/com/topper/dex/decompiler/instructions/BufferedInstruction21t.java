package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction21t;

public class BufferedInstruction21t extends BufferedInstruction implements Instruction21t {

	private final int registerA;
	private final int offset;
	
	public BufferedInstruction21t(final DexBuffer buffer, final Opcode opcode, final int instructionStart) {
		super(opcode, instructionStart);
		
		this.registerA = buffer.readUbyte(instructionStart + 1);
		this.offset = buffer.readShort(instructionStart + 2);
	}

    @Override public int getRegisterA() { return this.registerA; }
    @Override public int getCodeOffset() { return this.offset; }
}