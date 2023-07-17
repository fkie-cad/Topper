package com.topper.dex.decompiler.references;

import org.jf.dexlib2.ReferenceType;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.util.ExceptionWithContext;

public class BufferedReference {
	
    public static Reference makeReference(final DexBuffer buffer, final int referenceType, final int referenceIndex) {
        switch (referenceType) {
            case ReferenceType.STRING:
                return new StringReference(referenceIndex);
            case ReferenceType.TYPE:
                return new TypeReference(referenceIndex);
            case ReferenceType.METHOD:
                return new MethodReference(referenceIndex);
            case ReferenceType.FIELD:
                return new FieldReference(referenceIndex);
            case ReferenceType.METHOD_PROTO:
                return new MethodProtoReference(referenceIndex);
            case ReferenceType.METHOD_HANDLE:
                return new MethodHandleReference(referenceIndex);
            case ReferenceType.CALL_SITE:
                return new CallSiteReference(referenceIndex);
            default:
                throw new ExceptionWithContext("Invalid reference type: %d", referenceType);
        }
    }
}