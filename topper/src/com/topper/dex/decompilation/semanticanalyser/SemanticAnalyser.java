package com.topper.dex.decompilation.semanticanalyser;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.topper.dex.decompilation.Gadget;

public interface SemanticAnalyser {

	@Nullable
	Gadget analyse(@NonNull final Gadget gadget);
}