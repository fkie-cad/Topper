package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction35c;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.util.NibbleUtils;

import com.topper.dex.decompiler.references.BufferedReference;

public class BufferedInstruction35c extends BufferedInstruction implements Instruction35c {

	private final int registerCount;
	private final int registerC;
	private final int registerD;
	private final int registerE;
	private final int registerF;
	private final int registerG;
	private final Reference reference;
	private final int referenceType;
	
	public BufferedInstruction35c(final DexBuffer buffer, final Opcode opcode, final int instructionStartOffset, final DexBackedDexFile file) {
		super(opcode);
		
		this.registerCount = NibbleUtils.extractHighUnsignedNibble(buffer.readUbyte(instructionStartOffset + 1));
		this.registerC = NibbleUtils.extractLowUnsignedNibble(buffer.readUbyte(instructionStartOffset + 4));
		this.registerD = NibbleUtils.extractHighUnsignedNibble(buffer.readUbyte(instructionStartOffset + 4));
		this.registerE = NibbleUtils.extractLowUnsignedNibble(buffer.readUbyte(instructionStartOffset + 5));
		this.registerF = NibbleUtils.extractHighUnsignedNibble(buffer.readUbyte(instructionStartOffset + 5));
		this.registerG = NibbleUtils.extractLowUnsignedNibble(buffer.readUbyte(instructionStartOffset + 1));
		this.reference = BufferedReference.makeReference(buffer, this.getOpcode().referenceType,
                buffer.readUshort(instructionStartOffset + 2), file);
		this.referenceType = this.getOpcode().referenceType;
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
    public Reference getReference() {
        return this.reference;
    }

    @Override
    public int getReferenceType() {
        return this.referenceType;
    }
}
