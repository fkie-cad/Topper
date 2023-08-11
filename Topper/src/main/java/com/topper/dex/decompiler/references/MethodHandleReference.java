package com.topper.dex.decompiler.references;

import org.jf.dexlib2.MethodHandleType;
import org.jf.dexlib2.base.reference.BaseMethodHandleReference;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.raw.MethodHandleItem;
import org.jf.dexlib2.dexbacked.reference.DexBackedFieldReference;
import org.jf.dexlib2.dexbacked.reference.DexBackedMethodReference;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.util.ExceptionWithContext;

public final class MethodHandleReference extends BaseMethodHandleReference {

    private final int methodHandleIndex;
    
    private final int methodHandleType;
    private final Reference memberReference;

    public MethodHandleReference(final DexBackedDexFile file, int methodHandleIndex) {
        this.methodHandleIndex = methodHandleIndex;
        
        if (file != null) {
        	
        	// https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/reference/DexBackedMethodHandleReference.java;l=54;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4
        	final int methodHandleOffset = file.getMethodHandleSection().getOffset(this.methodHandleIndex);
        	this.methodHandleType = file.getBuffer().readUshort(methodHandleOffset + MethodHandleItem.METHOD_HANDLE_TYPE_OFFSET);
        	
        	// https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/reference/DexBackedMethodHandleReference.java;l=60;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4
            int memberIndex = file.getBuffer().readUshort(methodHandleOffset + MethodHandleItem.MEMBER_ID_OFFSET);
            Reference tmp = null;
            switch (getMethodHandleType()) {
                case MethodHandleType.STATIC_PUT:
                case MethodHandleType.STATIC_GET:
                case MethodHandleType.INSTANCE_PUT:
                case MethodHandleType.INSTANCE_GET:
                    tmp = new DexBackedFieldReference(file, memberIndex);
                    break;
                case MethodHandleType.INVOKE_STATIC:
                case MethodHandleType.INVOKE_INSTANCE:
                case MethodHandleType.INVOKE_CONSTRUCTOR:
                case MethodHandleType.INVOKE_DIRECT:
                case MethodHandleType.INVOKE_INTERFACE:
                    tmp = new DexBackedMethodReference(file, memberIndex);
                    break;
                default:
                    throw new ExceptionWithContext("Invalid method handle type: %d", getMethodHandleType());
            }
            this.memberReference = tmp;
        } else {
        	this.methodHandleType = -1;
        	this.memberReference = null;
        }
    }
    
    public final int getMethodHandleIndex() {
    	return this.methodHandleIndex;
    }

    @Override
    public int getMethodHandleType() {
        return this.methodHandleType;
    }

    @Override
    public Reference getMemberReference() {
        return this.memberReference;
    }
}