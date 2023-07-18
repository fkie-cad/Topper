package com.topper.dex.decompiler.instructions;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.dexbacked.DexReader;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.util.ExceptionWithContext;

/**
 * Buffered version of an <code>Instruction</code>. It is "buffered",
 * because all subclasses store their bytecode into fields. This aims
 * to support storing instructions to file.
 * 
 * Its implementation is based on <a href="https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/instruction/DexBackedInstruction.java;l=42;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4">AOSP's dexlib2</a>.
 * 
 * @author Pascal Kühnemann
 * @see Opcode
 * @see Instruction
 * */
public class BufferedInstruction implements Instruction {

	/**
	 * Opcode of this instruction.
	 * */
	private final Opcode opcode;
	
	public BufferedInstruction(final Opcode opcode) {
		this.opcode = opcode;
	}
	
	/**
	 * Determines the number of code units required to encode
	 * this instruction.
	 * 
	 * A code unit consists of two bytes.
	 * */
    @Override
    public int getCodeUnits() {
    	return opcode.format.size / 2;
    }

    /**
     * Get the opcode of this instruction
     * */
    public final Opcode getOpcode() {
    	return opcode;
    }
	
    /**
     * Reads from <code>reader</code> the next <code>BufferedInstruction</code>.
     * 
     * Optionally, <code>file</code> is set to allow for reference resolution
     * in case the underlying buffer represents an entire .dex file.
     * 
     * Its implementation is based on <a href="https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/instruction/DexBackedInstruction.java;l=59;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4">AOSP's dexlib2</a>.
     * 
     * @param reader Reader, from which to fetch the next instruction.
     * @param file If not <code>null</code>, it will represent the underlying
     * 	buffer of <code>reader</code> as a dex file.
     * @return The next instruction in <code>reader</code>.
     * */
    @NonNull
    public static BufferedInstruction readFrom(@NonNull final DexReader<?> reader, @Nullable final DexBackedDexFile file) {
    	
        int opcodeValue = reader.peekUbyte();

        if (opcodeValue == 0) {
            opcodeValue = reader.peekUshort();
        }

        final Opcode opcode = Opcodes.getDefault().getOpcodeByValue(opcodeValue);

        final BufferedInstruction instruction = buildInstruction(reader.dexBuf, opcode, reader.getOffset(), file);
        reader.moveRelative(instruction.getCodeUnits()*2);
        return instruction;
    }
    
    /**
     * Constructs an instruction from a fixed position in <code>buffer</code>.
     * 
     * Optionally, <code>file</code> is an alternative, but parallel, view on
     * <code>buffer</code> that enables reference resolution.
     * 
     * Its implementation is based on <a href="https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/instruction/DexBackedInstruction.java;l=75;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4">AOSP's dexlib2</a>.
     * 
     * @param buffer Buffer, from which to fetch the next instruction.
     * @param opcode Opcode of the next instruction. This e.g. determines the format.
     * @param instructionStartOffset Offset into <code>buffer</code>, from which to
     * 	start fetching the next instruction.
     * @param file Alternative view on <code>buffer</code> for reference resolution.
     * 	Can be <code>null</code>.
     * @return Next instruction in <code>buffer</code> at offset <code>instructionStartOffset</code>.
     * 	The instruction may be annotated with additional reference information, if
     * 	<code>file</code> is not <code>null</code>.
     * */
    @NonNull
    private static BufferedInstruction buildInstruction(
    		@NonNull final DexBuffer buffer,
    		@Nullable final Opcode opcode,
    		final int instructionStartOffset,
    		@Nullable final DexBackedDexFile file) {
    	
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