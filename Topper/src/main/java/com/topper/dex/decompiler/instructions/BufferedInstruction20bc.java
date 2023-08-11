package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.ReferenceType;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction20bc;
import org.jf.dexlib2.iface.reference.Reference;

import com.topper.dex.decompiler.references.BufferedReference;

public class BufferedInstruction20bc extends BufferedInstruction implements Instruction20bc {

	private final int verificationError;
	private final Reference reference;
	private final int referenceType;
	
	public BufferedInstruction20bc(DexBuffer buffer, Opcode opcode, int instructionStart, final DexBackedDexFile file) {
		super(opcode, instructionStart);
		
		this.verificationError = buffer.readUbyte(instructionStart + 1) & 0x3f;
		this.referenceType = (buffer.readUbyte(instructionStart + 1) >>> 6) + 1;
        ReferenceType.validateReferenceType(referenceType);
		this.reference = BufferedReference.makeReference(buffer, this.referenceType, buffer.readUshort(instructionStart + 2), file);
	}

	@Override
	public int getVerificationError() {
		return this.verificationError;
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