package com.topper.dex.decompiler;

import java.util.Iterator;

import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.dexbacked.DexReader;
import org.jf.dexlib2.dexbacked.util.VariableSizeLookaheadIterator;

import com.google.common.collect.ImmutableList;
import com.topper.dex.decompiler.instructions.BufferedInstruction;

/**
 * Problem: Given an array of bytes, interpret these bytes as Smali instructions
 * 			and decompile them into Smali.
 * 
 * */
public final class SmaliDecompiler {
	
	public final DecompilationResult decompile(final byte[] bytecode) {
		
		final DexBuffer buffer = new DexBuffer(bytecode);
		final ImmutableList<BufferedInstruction> instructions = ImmutableList.copyOf(this.getInstructions(buffer));
		return new DecompilationResult(buffer, instructions);
	}

    private Iterable<? extends BufferedInstruction> getInstructions(final DexBuffer buffer) {
        // instructionsSize is the number of 16-bit code units in the instruction list, not the number of instructions
        int instructionsSize = buffer.getBuf().length / 2; // dexFile.readSmallUint(codeOffset + CodeItem.INSTRUCTION_COUNT_OFFSET);

        final int instructionsStartOffset = 0; //codeOffset + CodeItem.INSTRUCTION_START_OFFSET;
        final int endOffset = instructionsStartOffset + (instructionsSize*2);
        
        return new Iterable<BufferedInstruction>() {
            @Override
            public Iterator<BufferedInstruction> iterator() {
                return new VariableSizeLookaheadIterator<BufferedInstruction>(buffer, instructionsStartOffset) {
                    @Override
                    protected BufferedInstruction readNextItem(@SuppressWarnings("rawtypes") final DexReader reader) {
                        if (reader.getOffset() >= endOffset) {
                            return endOfData();
                        }

                        BufferedInstruction instruction = BufferedInstruction.readFrom(reader);

                        // Does the instruction extend past the end of the method?
                        int offset = reader.getOffset();
                        if (offset > endOffset || offset < 0) {
                            return endOfData();
                            //throw new ExceptionWithContext("The last instruction is truncated");
                        }
                        return instruction;
                    }
                };
            }
        };
    }
}