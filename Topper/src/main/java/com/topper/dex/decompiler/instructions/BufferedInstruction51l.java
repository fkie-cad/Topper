package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction51l;

public class BufferedInstruction51l extends BufferedInstruction implements Instruction51l {

	private final int registerA;
	private final long wideLiteral;
	
	public BufferedInstruction51l(final DexBuffer buffer, final Opcode opcode, final int instructionStart) {
		super(opcode, instructionStart);
		
		this.registerA = buffer.readUbyte(instructionStart + 1);
		this.wideLiteral = buffer.readLong(instructionStart + 2);
	}

    @Override public int getRegisterA() { return this.registerA; }
    @Override public long getWideLiteral() { return this.wideLiteral; }
}