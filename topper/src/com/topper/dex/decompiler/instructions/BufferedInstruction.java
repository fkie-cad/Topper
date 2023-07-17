package com.topper.dex.decompiler.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.dexbacked.DexReader;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.util.ExceptionWithContext;

public class BufferedInstruction implements Instruction {

	private final Opcode opcode;
	public BufferedInstruction(final Opcode opcode) {
		
		this.opcode = opcode;
	}
	
    @Override public int getCodeUnits() { return opcode.format.size / 2; }

    public final Opcode getOpcode() {
    	return opcode;
    }
	
    public static BufferedInstruction readFrom(final DexReader<?> reader, final DexBackedDexFile file) {
        int opcodeValue = reader.peekUbyte();

        if (opcodeValue == 0) {
            opcodeValue = reader.peekUshort();
        }

        Opcode opcode = Opcodes.getDefault().getOpcodeByValue(opcodeValue);

        BufferedInstruction instruction = buildInstruction(reader.dexBuf, opcode, reader.getOffset(), file);
        reader.moveRelative(instruction.getCodeUnits()*2);
        return instruction;
    }
    
    private static BufferedInstruction buildInstruction(
    		final DexBuffer buffer,
    		final Opcode opcode,
    		final int instructionStartOffset,
    		final DexBackedDexFile file) {
    	
    	if (opcode == null) {
    		return new BufferedUnknownInstruction(buffer, instructionStartOffset);
    	}
    	
    	switch (opcode.format) {
	    	case Format10t:
	    		return new BufferedInstruction10t(buffer, opcode, instructionStartOffset);
	    	case Format10x:
	    		return new BufferedInstruction10x(buffer, opcode, instructionStartOffset);
	    	case Format11n:
	    		return new BufferedInstruction11n(buffer, opcode, instructionStartOffset);
	    	case Format11x:
	    		return new BufferedInstruction11x(buffer, opcode, instructionStartOffset);
	    	case Format12x:
	    		return new BufferedInstruction12x(buffer, opcode, instructionStartOffset);
	    	case Format20bc:
	    		return new BufferedInstruction20bc(buffer, opcode, instructionStartOffset, file);
	    	case Format20t:
	    		return new BufferedInstruction20t(buffer, opcode, instructionStartOffset);
	    	case Format21c:
	    		return new BufferedInstruction21c(buffer, opcode, instructionStartOffset, file);
	    	case Format21ih:
	    		return new BufferedInstruction21ih(buffer, opcode, instructionStartOffset);
	    	case Format21lh:
	    		return new BufferedInstruction21lh(buffer, opcode, instructionStartOffset);
	    	case Format21s:
	    		return new BufferedInstruction21s(buffer, opcode, instructionStartOffset);
	    	case Format21t:
	    		return new BufferedInstruction21t(buffer, opcode, instructionStartOffset);
	    	case Format22b:
	    		return new BufferedInstruction22b(buffer, opcode, instructionStartOffset);
	    	case Format22c:
	    		return new BufferedInstruction22c(buffer, opcode, instructionStartOffset, file);
	    	case Format22cs:
	    		return new BufferedInstruction22cs(buffer, opcode, instructionStartOffset);
	    	case Format22s:
	    		return new BufferedInstruction22s(buffer, opcode, instructionStartOffset);
	    	case Format22t:
	    		return new BufferedInstruction22t(buffer, opcode, instructionStartOffset);
	    	case Format22x:
	    		return new BufferedInstruction22x(buffer, opcode, instructionStartOffset);
	    	case Format23x:
	    		return new BufferedInstruction23x(buffer, opcode, instructionStartOffset);
	    	case Format30t:
	    		return new BufferedInstruction30t(buffer, opcode, instructionStartOffset);
	    	case Format31c:
	    		return new BufferedInstruction31c(buffer, opcode, instructionStartOffset, file);
	    	case Format31i:
	    		return new BufferedInstruction31i(buffer, opcode, instructionStartOffset);
	    	case Format31t:
	    		return new BufferedInstruction31t(buffer, opcode, instructionStartOffset);
	    	case Format32x:
	    		return new BufferedInstruction32x(buffer, opcode, instructionStartOffset);
	    	case Format35c:
	    		return new BufferedInstruction35c(buffer, opcode, instructionStartOffset, file);
	    	case Format35ms:
	    		return new BufferedInstruction35ms(buffer, opcode, instructionStartOffset);
	    	case Format35mi:
	    		return new BufferedInstruction35mi(buffer, opcode, instructionStartOffset);
	    	case Format3rc:
	    		return new BufferedInstruction3rc(buffer, opcode, instructionStartOffset, file);
	    	case Format3rmi:
	    		return new BufferedInstruction3rmi(buffer, opcode, instructionStartOffset);
	    	case Format3rms:
	    		return new BufferedInstruction3rms(buffer, opcode, instructionStartOffset);
	    	case Format45cc:
	    		return new BufferedInstruction45cc(buffer, opcode, instructionStartOffset, file);
	    	case Format4rcc:
	    		return new BufferedInstruction4rcc(buffer, opcode, instructionStartOffset, file);
	    	case Format51l:
	    		return new BufferedInstruction51l(buffer, opcode, instructionStartOffset);
	    	case PackedSwitchPayload:
	    		return new BufferedPackedSwitchPayload(buffer, instructionStartOffset);
	    	case SparseSwitchPayload:
	    		return new BufferedSparseSwitchPayload(buffer, instructionStartOffset);
	    	case ArrayPayload:
	    		return new BufferedArrayPayload(buffer, instructionStartOffset);
	    	default:
	    		throw new ExceptionWithContext("Unexpected opcode format: %s", opcode.format.toString());
	    }
    }
}