package com.topper.dex.ehandling;

import org.eclipse.jdt.annotation.NonNull;

public interface Bytable {
	byte @NonNull [] getBytes();
	int getByteSize();
}