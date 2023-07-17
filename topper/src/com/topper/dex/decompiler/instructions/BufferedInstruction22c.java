package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction22c;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.util.NibbleUtils;

import com.topper.dex.decompiler.references.BufferedReference;

public class BufferedInstruction22c extends BufferedInstruction implements Instruction22c {

	private final int registerA;
	private final int registerB;
	private final Reference reference;
	private final int referenceType;
	
	public BufferedInstruction22c(final DexBuffer buffer, final Opcode opcode, final int instructionStartOffset, final DexBackedDexFile file) {
		super(opcode);
		
		this.registerA = NibbleUtils.extractLowUnsignedNibble(buffer.readByte(instructionStartOffset + 1));
		this.registerB = NibbleUtils.extractHighUnsignedNibble(buffer.readByte(instructionStartOffset + 1));
		this.reference = BufferedReference.makeReference(buffer, this.getOpcode().referenceType, buffer.readUshort(instructionStartOffset + 2), file);
		this.referenceType = this.getOpcode().referenceType;
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
    public Reference getReference() {
        return this.reference;
    }

    @Override
    public int getReferenceType() {
        return this.referenceType;
    }
}