package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction21ih;

public class BufferedInstruction21ih extends BufferedInstruction implements Instruction21ih {

	private final int registerA;
	private final short hatLiteral;
	
	public BufferedInstruction21ih(final DexBuffer buffer, final Opcode opcode, final int instructionStartOffset) {
		super(opcode);
		
		this.registerA = buffer.readUbyte(instructionStartOffset + 1);
		this.hatLiteral = (short)buffer.readShort(instructionStartOffset + 2);
	}

	@Override
	public int getRegisterA() {
		return this.registerA;
	}

	@Override
	public short getHatLiteral() {
		return this.hatLiteral;
	}

	@Override
	public int getNarrowLiteral() {
		return getHatLiteral() << 16;
	}

	@Override
	public long getWideLiteral() {
		return getNarrowLiteral();
	}
}