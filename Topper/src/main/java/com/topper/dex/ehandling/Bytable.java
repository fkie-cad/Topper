package com.topper.dex.ehandling;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Description of objects that are capable to represent themselves as byte
 * arrays.
 * 
 * @author Pascal Kühnemann
 * @since 05.09.2023
 */
public interface Bytable {
	
	/**
	 * Gets the byte array representing this object.
	 * */
	byte @NonNull [] getBytes();

	/**
	 * Gets the number of bytes returned by {@link Bytable#getBytes()}.
	 * */
	int getByteSize();
}