package com.topper.dex.decompiler.references;

import java.util.List;

import org.jf.dexlib2.base.reference.BaseMethodProtoReference;
import org.jf.dexlib2.dexbacked.raw.ProtoIdItem;

import com.google.common.collect.ImmutableList;

public final class MethodProtoReference extends BaseMethodProtoReference {

	private static final String unknown = "<unknown>";
	
    private final int protoIndex;

    public MethodProtoReference(int protoIndex) {
        this.protoIndex = protoIndex;
    }
    
    public final int getProtoIndex() {
    	return this.protoIndex;
    }

    @Override
    public List<String> getParameterTypes() {
        return ImmutableList.of();
    }
    
    @Override
    public String getReturnType() {
        return MethodProtoReference.unknown;
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
