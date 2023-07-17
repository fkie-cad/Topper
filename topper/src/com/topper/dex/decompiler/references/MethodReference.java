package com.topper.dex.decompiler.references;

import java.util.List;

import org.jf.dexlib2.base.reference.BaseMethodReference;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.raw.MethodIdItem;
import org.jf.dexlib2.dexbacked.raw.ProtoIdItem;
import org.jf.dexlib2.dexbacked.raw.TypeListItem;
import org.jf.dexlib2.dexbacked.util.FixedSizeList;

import com.google.common.collect.ImmutableList;

public final class MethodReference extends BaseMethodReference {
	
	private static final String unknown = "<unknown>";
	
    private final int methodIndex;
    
    private final String definingClass;
    private final String name;
    private final List<String> parameterTypes;
    private final String returnType;

    public MethodReference(final DexBackedDexFile file, final int methodIndex) {
        this.methodIndex = methodIndex;
        
        if (file != null) {
        	
        	// https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/reference/DexBackedMethodReference.java;l=56;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4
        	this.definingClass = new String(file.getTypeSection().get(file.getBuffer().readUshort(
        			file.getMethodSection().getOffset(this.methodIndex) + MethodIdItem.CLASS_OFFSET
        	)));
        	
        	// https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/reference/DexBackedMethodReference.java;l=63;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4
        	this.name = new String(file.getStringSection().get(file.getBuffer().readSmallUint(
        			file.getMethodSection().getOffset(this.methodIndex) + MethodIdItem.NAME_OFFSET
        	)));
        	
        	// https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/reference/DexBackedMethodReference.java;l=70;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4
            final int protoIdItemOffset = file.getProtoSection().getOffset(file.getBuffer().readUshort(
                    file.getMethodSection().getOffset(methodIndex) + MethodIdItem.PROTO_OFFSET
            ));
            final int parametersOffset = file.getBuffer().readSmallUint(
                    protoIdItemOffset + ProtoIdItem.PARAMETERS_OFFSET);
            if (parametersOffset > 0) {
                final int parameterCount =
                        file.getDataBuffer().readSmallUint(parametersOffset + TypeListItem.SIZE_OFFSET);
                final int paramListStart = parametersOffset + TypeListItem.LIST_OFFSET;
                this.parameterTypes = new FixedSizeList<String>() {
                    @Override
                    public String readItem(final int index) {
                        return new String(file.getTypeSection().get(file.getDataBuffer().readUshort(paramListStart + 2*index)));
                    }
                    @Override public int size() { return parameterCount; }
                };
            } else {
            	this.parameterTypes = ImmutableList.of();
            }
            
            // https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/reference/DexBackedMethodReference.java;l=92;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4
            this.returnType = new String(file.getTypeSection().get(
                    file.getBuffer().readSmallUint(protoIdItemOffset + ProtoIdItem.RETURN_TYPE_OFFSET)
            ));
        } else {
        	
        	this.definingClass = MethodReference.unknown;
        	this.name = MethodReference.unknown;
            this.parameterTypes = ImmutableList.of();
            this.returnType = MethodReference.unknown;
        }
    }
    
    public final int getMethodIndex() {
    	return this.methodIndex;
    }
    
    @Override
    public String getDefiningClass() {
        return this.definingClass;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<String> getParameterTypes() {
        return this.parameterTypes;
    }

    @Override
    public String getReturnType() {
    	return this.returnType;
    }

    /**
     * Calculate and return the private size of a method reference.
     *
     * Calculated as: class_idx + proto_idx + name_idx
     *
     * @return size in bytes
     */
    public int getSize() {
        return MethodIdItem.ITEM_SIZE; //ushort + ushort + uint for indices
    }
}
