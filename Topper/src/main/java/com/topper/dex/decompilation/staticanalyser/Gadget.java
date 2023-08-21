package com.topper.dex.decompilation.staticanalyser;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.topper.configuration.ConfigManager;
import com.topper.configuration.SweeperConfig;
import com.topper.dex.decompilation.DexHelper;
import com.topper.dex.decompilation.graphs.CFG;
import com.topper.dex.decompilation.graphs.DFG;
import com.topper.dex.decompiler.instructions.DecompiledInstruction;

/**
 * Gadget extracted using a {@link StaticAnalyser}. It consists of a list of
 * {@link DecompiledInstruction}s, where the last instruction is determined by a
 * configurable opcode, i.e. by {@link SweeperConfig#getPivotOpcode()}.
 * 
 * @author Pascal Kühnemann
 * @since 21.08.2023
 */
public class Gadget {

	/**
	 * List of extracted instructions.
	 */
	@NonNull
	private final ImmutableList<@NonNull DecompiledInstruction> instructions;

	/**
	 * Extracted Control Flow Graph, if configured.
	 */
	@Nullable
	private final CFG cfg;

	/**
	 * Extracted Data Flow Graph, if configured.
	 */
	@Nullable
	private final DFG dfg;

	/**
	 * Creates a new {@link Gadget} from a list of {@link DecompiledInstruction}s,
	 * and optionally a {@link CFG} and {@link DFG}. <code>cfg</code> and
	 * <code>dfg</code> must be linked to <code>instructions</code>.
	 * 
	 * @param instructions List of <code>DecompiledInstruction</code>s that make up
	 *                     this gadget.
	 * @param cfg          Control Flow Graph extracted using static analysis, if
	 *                     configured.
	 * @param dfg          Data Flow Graph extracted using static analysis, if
	 *                     configured.
	 * @throws IllegalArgumentException If <code>instructions</code> is empty or the
	 *                                  last instruction's opcode does not match
	 *                                  {@link SweeperConfig#getPivotOpcode()}.
	 */
	public Gadget(@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions, @Nullable final CFG cfg,
			@Nullable DFG dfg) {

		if (instructions.isEmpty()) {
			throw new IllegalArgumentException("List of instructions must be non - empty.");
		} else if (!instructions.get(instructions.size() - 1).getInstruction().getOpcode()
				.equals(ConfigManager.get().getSweeperConfig().getPivotOpcode())) {
			throw new IllegalArgumentException("Last instruction's opcode must match configured pivot opcode.");
		}
		this.instructions = instructions;
		this.cfg = cfg;
		this.dfg = dfg;
	}

	/**
	 * Gets the list of {@link DecompiledInstruction}s that make up this gadget.
	 * 
	 * The list is guaranteed to be non - empty and end in an instruction, whose {@link Opcode}
	 * matches {@link SweeperConfig#getPivotOpcode()}.
	 */
	@NonNull
	public final ImmutableList<@NonNull DecompiledInstruction> getInstructions() {
		return this.instructions;
	}

	/**
	 * Gets the associated {@link CFG}, if any.
	 * */
	@Nullable
	public final CFG getCFG() {
		return this.cfg;
	}

	/**
	 * Determines whether this gadget has an associated {@link CFG} or not.
	 * 
	 * @return <code>true</code>, if this gadget has an associated <code>CFG</code>; <code>false</code> otherwise.
	 * */
	public final boolean hasCFG() {
		return this.cfg != null;
	}

	/**
	 * Get the associated {@link DFG}, if any.
	 * */
	@Nullable
	public final DFG getDFG() {
		return this.dfg;
	}

	/**
	 * Determines whether this gadget has an associated {@link DFG} or not.
	 * 
	 * @return <code>true</code>, if this gadget has an associated <code>DFG</code>; <code>false</code> otherwise.
	 * */
	public final boolean hasDFG() {
		return this.dfg != null;
	}

	@Override
	public final String toString() {
		final StringBuilder b = new StringBuilder();

		// Print entry
		b.append(String.format("Entry: %#08x" + System.lineSeparator(),
				(this.cfg != null) ? this.cfg.getEntry() : this.instructions.get(0).getOffset()));

		// Convert instructions to string
		b.append(DexHelper.instructionsToString(this.instructions));

		return b.toString();
	}
}