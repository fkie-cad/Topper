package com.topper.commands.attack;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import com.topper.commands.PicoCommand;
import com.topper.commands.PicoTopLevelCommand;
import com.topper.exceptions.commands.CommandException;
import com.topper.exceptions.commands.IllegalCommandException;
import com.topper.sstate.CommandState;
import com.topper.sstate.ExecutionState;
import com.topper.sstate.PicoState;
import com.topper.sstate.ScriptContext;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "ctop", mixinStandardHelpOptions = true, version = "1.0", description = "Computes a list of patches to apply to the loaded file to achieve gadget chain execution. It does not check whether the method offset actually points to a valid method code item!")
@PicoState(states = { ExecutionState.class })
public final class PicoTOPExceptionHandlerAttackCommand extends PicoCommand {

	@Option(names = { "-g",
			"--gadgets" }, required = true, arity = "1..*", split = ",", description = "Ordered list of gadget offsets relative to the loaded file's base (often base.vdex).")
	private List<@NonNull Integer> gadgets;

	@Option(names = { "-m",
			"--method-offset" }, required = true, description = "Offset of the method to patch relative to the loaded file's base (often base.vdex).")
	private int methodOffset;

	@Option(names = { "-t",
			"--exception-type-index" }, required = true, description = "Type index of the exception type to instantiate and use for throwing. The type is relative to the .dex file used to execute the hijacked method.")
	private int exceptionTypeIndex;

	@Option(names = { "-v",
			"--exception-vreg-index" }, defaultValue = "0", description = "Virtual register used for storing the exception object. E.g. \"v0\" needs \"-v 0\" etc. This dictates what gadgets are eligible for manipulating the control flow.")
	private int exceptionVregIndex;

	@Option(names = { "-p", "--pc-vreg-index" }, defaultValue = "1", description = "Virtual register used for storing the virtual program counter. It dictates what gadget is executed next using a dispatcher.")
	private int pcVregIndex;
	
	@Option(names = { "-x", "--method-handler-padding" }, defaultValue = "0", description = "Padding bytes to introduce between the end of the target method and the encoded exception handler (4-byte aligned).")
	private int methodHandlerPadding;
	
	@Option(names = { "-y", "--handler-dispatcher-padding" }, defaultValue = "0", description = "Padding bytes to introduce between the encoded exception handler and the actual dispatcher code (4-byte aligned).")
	private int handlerDispatcherPadding;
	
	@Option(names = { "-a", "--alignment" }, defaultValue = "8", description = "Determines the alignment to use for describing the patches. Patch data is always divisible by alignment and refers to an aligned offset. Useful for Write - What - Where conditions with fixed - sized writes (qword,...).")
	private int alignment;
	
	@ParentCommand
	private PicoAttackCommand parent;

	@Override
	public final void execute(@NonNull final ScriptContext context) throws CommandException {

		// Goal: Compute list of patches to apply in current dex context.
		this.checkArgs();
		
		@NonNull
		final List<@NonNull Patch> patches = this.computePatches();
		
		for (@NonNull final Patch patch : patches) {
			parent.getTopLevel().out().println(patch);
		}
	}

	@Override
	@NonNull
	public final CommandState next() {
		return new ExecutionState(this.getContext());
	}

	@Override
	@NonNull
	public final PicoTopLevelCommand getTopLevel() {
		if (this.parent == null) {
			throw new UnsupportedOperationException("Cannot access parent before its initialized.");
		}
		return this.parent.getTopLevel();
	}
	
	private final void checkArgs() throws IllegalCommandException {
		
		// Check gadgets
		if (this.gadgets.isEmpty()) {
			throw new IllegalCommandException("At least one gadget must be provided.");
		}
		
		for (@NonNull final Integer offset : this.gadgets) {
			if (offset < 0) {
				throw new IllegalCommandException("Gadget offset must be non - negative.");
			}
		}
		
		// Check method offset. Also methodOffet = 0 does not make much sense... 
		if (this.methodOffset < 0) {
			throw new IllegalCommandException("Method offset must be non - negative.");
		}
		
		// Check type index
		if (this.exceptionTypeIndex < 0) {
			throw new IllegalCommandException("Exception type index must be non - negative.");
		}
		
		// Check exception vreg index
		if (this.exceptionVregIndex < 0) {			
			throw new IllegalCommandException("Exception vreg index must be non - negative.");
		}
		
		// Check program counter vreg index
		if (this.pcVregIndex < 0) {
			throw new IllegalCommandException("Virtual program counter vreg index must be non - negative.");
		}
		
		// Check paddings
		if (this.methodHandlerPadding < 0) {
			throw new IllegalCommandException("Padding between method end and encoded exception handler must be non - negative.");
		} else if (this.methodHandlerPadding % 4 == 0) {
			throw new IllegalCommandException("padding between method end and encoded exception handler must be 4-byte aligned.");
		}
		
		if (this.handlerDispatcherPadding < 0) {
			throw new IllegalCommandException("Padding between encoded exception handler and dispatcher must be non - negative.");
		} else if (this.handlerDispatcherPadding % 4 == 0) {
			throw new IllegalCommandException("padding between encoded exception handler and dispatcher must be 4-byte aligned.");
		}
		
		// Check alignment
		if (this.alignment < 1) {
			throw new IllegalCommandException("Alignment must be positive.");
		}
	}
	
	@NonNull
	private final List<@NonNull Patch> computePatches() {
		
		@NonNull
		final List<@NonNull Patch> patches = new LinkedList<>();
		
		// TODO: Continue here
		
		return patches;
	}
}