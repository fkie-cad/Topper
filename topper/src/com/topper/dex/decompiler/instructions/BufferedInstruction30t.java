package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction30t;

public class BufferedInstruction30t extends BufferedInstruction implements Instruction30t {

	private final int offset;
	
	public BufferedInstruction30t(final DexBuffer buffer, final Opcode opcode, final int instructionStartOffset) {
		super(opcode);
		
		this.offset = buffer.readInt(instructionStartOffset + 2);
	}

    @Override public int getCodeOffset() { return this.offset; }
}