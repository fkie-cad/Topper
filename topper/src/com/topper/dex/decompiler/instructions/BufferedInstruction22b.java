package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction22b;

public class BufferedInstruction22b extends BufferedInstruction implements Instruction22b {

	private final int registerA;
	private final int registerB;
	private final int narrowLiteral;
	
	public BufferedInstruction22b(final DexBuffer buffer, final Opcode opcode, final int instructionStartOffset) {
		super(opcode);
		
		this.registerA = buffer.readUbyte(instructionStartOffset + 1);
		this.registerB = buffer.readUbyte(instructionStartOffset + 2);
		this.narrowLiteral = buffer.readByte(instructionStartOffset + 3);
	}

    @Override public int getRegisterA() { return this.registerA; }
    @Override public int getRegisterB() { return this.registerB; }
    @Override public int getNarrowLiteral() { return this.narrowLiteral; }
    @Override public long getWideLiteral() { return getNarrowLiteral(); }
}