package com.topper.scengine.commands.file;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.dex.decompilation.DexHelper;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompilation.staticanalyser.Gadget;

public final class BasedGadget {

	@NonNull
	private final Gadget gadget;

	private final int base;

	public BasedGadget(@NonNull final Gadget gadget, final int base) {
		this.gadget = gadget;
		this.base = base;
	}

	@NonNull
	public final Gadget getGadget() {
		return gadget;
	}

	public final int getBase() {
		return base;
	}

	@Override
	public final String toString() {

		final int offset;
		final CFG cfg = this.gadget.getCFG();
		if (cfg != null) {
			offset = this.base + cfg.getEntry();
		} else {
			offset = this.base + this.gadget.getInstructions().get(0).getOffset();
		}

		final StringBuilder b = new StringBuilder();

		// Print entry
		b.append(String.format("Entry: %#08x" + System.lineSeparator(), offset));

		// Convert instructions to string
		b.append(DexHelper.instructionsToString(this.gadget.getInstructions()));

		return b.toString();
	}
}