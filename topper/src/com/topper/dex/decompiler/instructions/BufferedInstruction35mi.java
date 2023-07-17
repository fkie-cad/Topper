package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction35mi;
import org.jf.util.NibbleUtils;

public class BufferedInstruction35mi extends BufferedInstruction implements Instruction35mi {

	private final int registerCount;
	private final int registerC;
	private final int registerD;
	private final int registerE;
	private final int registerF;
	private final int registerG;
	private final int inlineIndex;
	
	public BufferedInstruction35mi(final DexBuffer buffer, final Opcode opcode, final int instructionStartOffset) {
		super(opcode);

		this.registerCount = NibbleUtils.extractHighUnsignedNibble(buffer.readUbyte(instructionStartOffset + 1));
		this.registerC = NibbleUtils.extractLowUnsignedNibble(buffer.readUbyte(instructionStartOffset + 4));
		this.registerD = NibbleUtils.extractHighUnsignedNibble(buffer.readUbyte(instructionStartOffset + 4));
		this.registerE = NibbleUtils.extractLowUnsignedNibble(buffer.readUbyte(instructionStartOffset + 5));
		this.registerF = NibbleUtils.extractHighUnsignedNibble(buffer.readUbyte(instructionStartOffset + 5));
		this.registerG = NibbleUtils.extractLowUnsignedNibble(buffer.readUbyte(instructionStartOffset + 1));
		this.inlineIndex = buffer.readUshort(instructionStartOffset + 2);
	}

    @Override public int getRegisterCount() {
        return this.registerCount;
    }

    @Override
    public int getRegisterC() {
        return this.registerC;
    }

    @Override
    public int getRegisterD() {
        return this.registerD;
    }

    @Override
    public int getRegisterE() {
        return this.registerE;
    }

    @Override
    public int getRegisterF() {
        return this.registerF;
    }

    @Override
    public int getRegisterG() {
        return this.registerG;
    }

    @Override
    public int getInlineIndex() {
        return this.inlineIndex;
    }
}