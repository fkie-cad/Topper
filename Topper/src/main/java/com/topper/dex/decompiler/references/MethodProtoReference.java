package com.topper.dex.decompiler.references;

import java.util.List;

import org.jf.dexlib2.base.reference.BaseMethodProtoReference;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.raw.ProtoIdItem;
import org.jf.dexlib2.dexbacked.raw.TypeListItem;
import org.jf.dexlib2.dexbacked.util.FixedSizeList;

import com.google.common.collect.ImmutableList;

public final class MethodProtoReference extends BaseMethodProtoReference {

	private static final String unknown = "<unknown>";
	
    private final int protoIndex;
    
    private final List<String> parameterTypes;
    private final String returnType;

    public MethodProtoReference(final DexBackedDexFile file, int protoIndex) {
        this.protoIndex = protoIndex;
        
        if (file != null) {
        	
        	// https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/reference/DexBackedMethodProtoReference.java;l=55;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4
            final int parametersOffset = file.getBuffer().readSmallUint(file.getProtoSection().getOffset(protoIndex) +
                    ProtoIdItem.PARAMETERS_OFFSET);
            if (parametersOffset > 0) {
                final int parameterCount = file.getDataBuffer().readSmallUint(
                        parametersOffset + TypeListItem.SIZE_OFFSET);
                final int paramListStart = parametersOffset + TypeListItem.LIST_OFFSET;
                this.parameterTypes = new FixedSizeList<String>() {
                    @Override
                    public String readItem(final int index) {
                        return new String(file.getTypeSection().get(file.getDataBuffer().readUshort(paramListStart + 2*index)));
                    }
                    @Override public int size() { return parameterCount; }
                };
            }
            else {
            	this.parameterTypes = ImmutableList.of();
            }
            
            // https://cs.android.com/android/platform/superproject/+/master:external/google-smali/dexlib2/src/main/java/com/android/tools/smali/dexlib2/dexbacked/reference/DexBackedMethodProtoReference.java;l=75;drc=3713aeddd5faa8ba395999d1ee32c3fb3e68e3f4
            this.returnType = new String(file.getTypeSection().get(file.getBuffer().readSmallUint(
                file.getProtoSection().getOffset(protoIndex) + ProtoIdItem.RETURN_TYPE_OFFSET)));
        } else {
        	this.parameterTypes = ImmutableList.of();
        	this.returnType = MethodProtoReference.unknown;
        }
    }
    
    public final int getProtoIndex() {
    	return this.protoIndex;
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
     * Calculate and return the private size of a method proto.
     *
     * Calculated as: shorty_idx + return_type_idx + parameters_off + type_list size
     *
     * @return size in bytes
     */
    public int getSize() {
        int size = ProtoIdItem.ITEM_SIZE; //3 * uint
        List<String> parameters = getParameterTypes();
        if (!parameters.isEmpty()) {
            size += 4 + parameters.size() * 2; //uint + size * ushort for type_idxs
        }
        return size;
    }

}
