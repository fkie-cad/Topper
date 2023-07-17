package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.instruction.formats.Instruction20t;

public class BufferedInstruction20t extends BufferedInstruction implements Instruction20t {

	private final int offset;
	
	public BufferedInstruction20t(final DexBuffer buffer, final Opcode opcode, final int instructionStart) {
		super(opcode);
		
		this.offset = buffer.readShort(instructionStart + 2);
	}

	@Override
	public int getCodeOffset() {
		return this.offset;
	}
}