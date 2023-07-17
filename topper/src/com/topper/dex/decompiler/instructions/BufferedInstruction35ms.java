package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction35ms;
import org.jf.util.NibbleUtils;

public class BufferedInstruction35ms extends BufferedInstruction implements Instruction35ms {

	private final int registerCount;
	private final int registerC;
	private final int registerD;
	private final int registerE;
	private final int registerF;
	private final int registerG;
	private final int vtableIndex;
	
	public BufferedInstruction35ms(final DexBuffer buffer, final Opcode opcode, final int instructionStartOffset) {
		super(opcode);

		this.registerCount = NibbleUtils.extractHighUnsignedNibble(buffer.readUbyte(instructionStartOffset + 1));
		this.registerC = NibbleUtils.extractLowUnsignedNibble(buffer.readUbyte(instructionStartOffset + 4));
		this.registerD = NibbleUtils.extractHighUnsignedNibble(buffer.readUbyte(instructionStartOffset + 4));
		this.registerE = NibbleUtils.extractLowUnsignedNibble(buffer.readUbyte(instructionStartOffset + 5));
		this.registerF = NibbleUtils.extractHighUnsignedNibble(buffer.readUbyte(instructionStartOffset + 5));
		this.registerG = NibbleUtils.extractLowUnsignedNibble(buffer.readUbyte(instructionStartOffset + 1));
		this.vtableIndex = buffer.readUshort(instructionStartOffset + 2);
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
    public int getVtableIndex() {
        return this.vtableIndex;
    }
}