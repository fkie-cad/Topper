package com.topper.dex.decompiler.references;

import org.jf.dexlib2.ReferenceType;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.util.ExceptionWithContext;

/**
 * Reference wrapper responsible for creating references based on
 * reference types.
 * 
 * @author Pascal Kühnemann
 * */
public class BufferedReference {
	
	/**
	 * Constructs a reference from a <code>referenceType</code> and <code>referenceIndex</code>.
	 * 
	 * Optionally, <code>file</code> is used to resolve the reference, if not <code>null</code>.
	 * 
	 * Its implementation is based on <a href="https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/reference/DexBackedReference.java;l=41;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4">AOSP's dexlib2</a>.
	 * 
	 * @param buffer Buffer that contains the instruction needing a reference.
	 * @param referenceType Type of the reference.
	 * @param referenceIndex Depending on the type, describes an index into a lookup table.
	 * @param file Dex file view on <code>buffer</code>, or <code>null</code>.
	 * @return Reference matching <code>referenceType</code>.
	 * @throws ExceptionWithContext If <code>referenceType</code> is unknown.
	 * */
    public static Reference makeReference(
    		final DexBuffer buffer,
    		final int referenceType,
    		final int referenceIndex,
    		final DexBackedDexFile file) {
    	
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