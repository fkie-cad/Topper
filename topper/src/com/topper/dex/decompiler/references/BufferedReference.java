package com.topper.dex.decompiler.references;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.ReferenceType;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.util.ExceptionWithContext;

public class BufferedReference {
	
    public static Reference makeReference(final DexBuffer buffer, final int referenceType, final int referenceIndex) {
    	
    	// Temporarily create file backed by buffer
    	DexBackedDexFile file;
    	try {
    		file = new DexBackedDexFile(Opcodes.getDefault(), buffer);
    	} catch (final RuntimeException e) {
    		file = null;
    	}
    	
        switch (referenceType) {
            case ReferenceType.STRING:
                return new StringReference(file, referenceIndex);
            case ReferenceType.TYPE:
                return new TypeReference(file, referenceIndex);
            case ReferenceType.METHOD:
                return new MethodReference(file, referenceIndex);
            case ReferenceType.FIELD:
                return new FieldReference(file, referenceIndex);
            case ReferenceType.METHOD_PROTO:
                return new MethodProtoReference(file, referenceIndex);
            case ReferenceType.METHOD_HANDLE:
                return new MethodHandleReference(file, referenceIndex);
            case ReferenceType.CALL_SITE:
                return new CallSiteReference(file, referenceIndex);
            default:
                throw new ExceptionWithContext("Invalid reference type: %d", referenceType);
        }
    }
}