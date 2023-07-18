package com.topper.dex.decompilation.semanticanalyser;

import com.topper.dex.decompilation.Gadget;

public interface SemanticAnalyser {

	Gadget analyse(final Gadget gadget);
}