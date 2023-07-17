package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction22cs;
import org.jf.util.NibbleUtils;

public class BufferedInstruction22cs extends BufferedInstruction implements Instruction22cs {

	private final int registerA;
	private final int registerB;
	private final int fieldOffset;
	
	public BufferedInstruction22cs(final DexBuffer buffer, final Opcode opcode, final int instructionStartOffset) {
		super(opcode);
		
		this.registerA = NibbleUtils.extractLowUnsignedNibble(buffer.readByte(instructionStartOffset + 1));
		this.registerB = NibbleUtils.extractHighUnsignedNibble(buffer.readByte(instructionStartOffset + 1));
		this.fieldOffset = buffer.readUshort(instructionStartOffset + 2);
	}
	
    @Override
    public int getRegisterA() {
        return this.registerA;
    }

    @Override
    public int getRegisterB() {
        return this.registerB;
    }

    @Override
    public int getFieldOffset() {
        return this.fieldOffset;
    }
}