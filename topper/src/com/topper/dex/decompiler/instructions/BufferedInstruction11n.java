package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction11n;
import org.jf.util.NibbleUtils;

public class BufferedInstruction11n extends BufferedInstruction implements Instruction11n {
	
	private final int registerA;
	private final int narrowLiteral;

	public BufferedInstruction11n(DexBuffer buffer, Opcode opcode, int instructionStart) {
		super(opcode);
		
		this.registerA = NibbleUtils.extractLowUnsignedNibble(buffer.readByte(instructionStart + 1));
		this.narrowLiteral = NibbleUtils.extractLowUnsignedNibble(buffer.readByte(instructionStart + 1));
	}

	@Override
	public int getRegisterA() {
		return this.registerA;
	}

	@Override
	public int getNarrowLiteral() {
		return this.narrowLiteral;
	}

	@Override
	public long getWideLiteral() {
		return this.getNarrowLiteral();
	}
}
