package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction31t;

public class BufferedInstruction31t extends BufferedInstruction implements Instruction31t {

	private final int registerA;
	private final int offset;
	
	public BufferedInstruction31t(final DexBuffer buffer, final Opcode opcode, final int instructionStartOffset) {
		super(opcode);
		
		this.registerA = buffer.readUbyte(instructionStartOffset + 1);
		this.offset = buffer.readInt(instructionStartOffset + 2);
	}

    @Override public int getRegisterA() { return this.registerA; }
    @Override public int getCodeOffset() { return this.offset; }
}