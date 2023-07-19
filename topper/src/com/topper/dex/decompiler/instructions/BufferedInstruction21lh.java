package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction21lh;

public class BufferedInstruction21lh extends BufferedInstruction implements Instruction21lh {

	private final int registerA;
	private final short hatLiteral;
	
	public BufferedInstruction21lh(final DexBuffer buffer, final Opcode opcode, final int instructionStart) {
		super(opcode, instructionStart);
		
		this.registerA = buffer.readUbyte(instructionStart + 1);
		this.hatLiteral = (short)buffer.readShort(instructionStart + 2);
	}
	
    @Override public int getRegisterA() { return this.registerA; }
    @Override public long getWideLiteral() { return ((long)getHatLiteral()) << 48; }
    @Override public short getHatLiteral() { return this.hatLiteral; }
}