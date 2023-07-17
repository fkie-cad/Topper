package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction31i;

public class BufferedInstruction31i extends BufferedInstruction implements Instruction31i {

	private final int registerA;
	private final int narrowLiteral;
	
	public BufferedInstruction31i(final DexBuffer buffer, final Opcode opcode, final int instructionStartOffset) {
		super(opcode);
		
		this.registerA = buffer.readUbyte(instructionStartOffset + 1);
		this.narrowLiteral = buffer.readInt(instructionStartOffset + 2);
	}

    @Override public int getRegisterA() { return this.registerA; }
    @Override public int getNarrowLiteral() { return this.narrowLiteral; }
    @Override public long getWideLiteral() { return getNarrowLiteral(); }
}