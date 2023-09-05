package com.topper.dex.staticanalyser;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.Opcode;

import com.topper.dex.pipeline.Stage;

/**
 * Abstract description of an analyser that performs static analysis.
 * 
 * @author Pascal Kühnemann
 * @since 21.08.2023
 */
public abstract class StaticAnalyser implements Stage {

	/**
	 * {@link CFGAnalyser} used for extracting {@link CFG}s.
	 */
	@NonNull
	private CFGAnalyser cfgAnalyser;

	/**
	 * {@link DFGAnalyser} used for extracting {@link DFG}s.
	 */
	@NonNull
	private DFGAnalyser dfgAnalyser;

	/**
	 * Create a new {@link StaticAnalyser} using
	 * <ul>
	 * <li>{@link BFSCFGAnalyser}</li>
	 * <li>{@link DefaultDFGAnalyser}</li>
	 * </ul>
	 * as default analysers.
	 */
	public StaticAnalyser() {
		this.cfgAnalyser = new BFSCFGAnalyser();
		this.dfgAnalyser = new DefaultDFGAnalyser();
	}

	/**
	 * Gets currently selected {@link CFGAnalyser}.
	 */
	@NonNull
	public final CFGAnalyser getCFGAnalyser() {
		return this.cfgAnalyser;
	}

	/**
	 * Overwrites current {@link CFGAnalyser} with <code>ca</code>.
	 */
	public final void setCFGAnalyser(@NonNull final CFGAnalyser ca) {
		this.cfgAnalyser = ca;
	}

	/**
	 * Gets currently selected {@link DFGAnalyser}.
	 */
	@NonNull
	public final DFGAnalyser getDFGAnalyser() {
		return this.dfgAnalyser;
	}

	/**
	 * Overwrites current {@link DFGAnalyser} with <code>da</code>.
	 */
	public final void setDFGAnalyser(@NonNull final DFGAnalyser da) {
		this.dfgAnalyser = da;
	}

	/**
	 * Determines whether <code>opcode</code> represents an if opcode.
	 * 
	 * @return <code>true</code>, if <code>opcode</code> represents an if opcode;
	 *         <code>false</code> otherwise.
	 */
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

	/**
	 * Determines whether <code>opcode</code> is a return opcode.
	 * 
	 * @return <code>true</code>, if <code>opcode</code> is a return opcode; <code>false</code> otherwise.
	 * */
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

	/**
	 * Determines whether <code>opcode</code> is a throw opcode.
	 * 
	 * @return <code>true</code>, if <code>opcode</code> is a throw opcode; <code>false</code> otherwise.
	 * */
	public static final boolean isThrow(@NonNull final Opcode opcode) {
		return opcode.equals(Opcode.THROW);
	}

	/**
	 * Determines whether <code>opcode</code> is a goto opcode.
	 * 
	 * @return <code>true</code>, if <code>opcode</code> is a goto opcode; <code>false</code> otherwise.
	 * */
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

	/**
	 * Determines whether <code>opcode</code> is a switch opcode.
	 * 
	 * @return <code>true</code>, if <code>opcode</code> is a switch opcode; <code>false</code> otherwise.
	 * */
	public static final boolean isSwitch(@NonNull final Opcode opcode) {
		return opcode.equals(Opcode.PACKED_SWITCH) || opcode.equals(Opcode.SPARSE_SWITCH);
	}
}