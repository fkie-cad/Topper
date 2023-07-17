package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction12x;
import org.jf.util.NibbleUtils;

public class BufferedInstruction12x extends BufferedInstruction implements Instruction12x {
	
	private final int registerA;
	private final int registerB;

	public BufferedInstruction12x(DexBuffer buffer, Opcode opcode, int instructionStart) {
		super(opcode);
		
		this.registerA = NibbleUtils.extractLowUnsignedNibble(buffer.readByte(instructionStart + 1));
		this.registerB = NibbleUtils.extractHighUnsignedNibble(buffer.readByte(instructionStart + 1));
	}

	@Override
	public int getRegisterA() {
        return this.registerA;
	}

	@Override
	public int getRegisterB() {
        return this.registerB;
	}
}