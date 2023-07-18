package com.topper.dex.decompilation.semanticanalyser;

import com.topper.dex.decompilation.Gadget;

public class EmptySemanticAnalyser implements SemanticAnalyser {

	@Override
	public Gadget analyse(Gadget gadget) {
		return gadget;
	}
}