package com.topper.dex.decompilation.staticanalyser;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Opcode;

import com.topper.dex.decompilation.pipeline.Stage;
import com.topper.dex.decompilation.pipeline.StageInfo;

public abstract class StaticAnalyser<@NonNull T extends Map<@NonNull String, @NonNull StageInfo>> implements Stage<T> {

	@NonNull
	private CFGAnalyser cfgAnalyser;
	
	@NonNull
	private DFGAnalyser dfgAnalyser;
	
	public StaticAnalyser() {
		this.cfgAnalyser = new BFSCFGAnalyser();
		this.dfgAnalyser = new DefaultDFGAnalyser();
	}
	
//	@NonNull
//	public final Gadget analyse(@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions) {
//		
//		// Extract CFG
//		final CFG cfg = this.cfgAnalyser.extractCFG(instructions);
//		
//		// Extract DFG. Maybe this requires CFG as well.
//		final DFG dfg = this.dfgAnalyser.extractDFG(instructions);
//		
//		return new Gadget(instructions, cfg, dfg);
//	}
	
	// @NonNull public abstract Gadget analyse(@NonNull final ImmutableList<@NonNull DecompiledInstruction> instructions, final int entry);
	
	@NonNull
	public final CFGAnalyser getCFGAnalyser() {
		return this.cfgAnalyser;
	}
	
	public final void setCFGAnalyser(@NonNull final CFGAnalyser ca) {
		this.cfgAnalyser = ca;
	}
	
	@NonNull
	public final DFGAnalyser getDFGAnalyser() {
		return this.dfgAnalyser;
	}
	
	public final void setDFGAnalyser(@NonNull final DFGAnalyser da) {
		this.dfgAnalyser = da;
	}
	
	public static final boolean isIf(@NonNull final Opcode opcode) {
		switch (opcode) {
		case IF_EQ:
		case IF_EQZ:
		case IF_GE:
		case IF_GEZ:
		case IF_GT:
		case IF_GTZ:
		case IF_LE:
		case IF_LEZ:
		case IF_LT:
		case IF_LTZ:
		case IF_NE:
		case IF_NEZ:
			return true;
		default:
			return false;
		}
	}

	public static final boolean isReturn(@NonNull final Opcode opcode) {
		switch (opcode) {
		case RETURN:
		case RETURN_OBJECT:
		case RETURN_VOID:
		case RETURN_VOID_BARRIER:
		case RETURN_VOID_NO_BARRIER:
		case RETURN_WIDE:
			return true;
		default:
			return false;
		}
	}

	public static final boolean isThrow(@NonNull final Opcode opcode) {
		return opcode.equals(Opcode.THROW);
	}
	
	public static final boolean isGoto(@NonNull final Opcode opcode) {
		switch (opcode) {
		case GOTO:
		case GOTO_16:
		case GOTO_32:
			return true;
		default:
			return false;
		}
	}
	
	public static final boolean isSwitch(@NonNull final Opcode opcode) {
		return opcode.equals(Opcode.PACKED_SWITCH) || opcode.equals(Opcode.SPARSE_SWITCH);
	}
}