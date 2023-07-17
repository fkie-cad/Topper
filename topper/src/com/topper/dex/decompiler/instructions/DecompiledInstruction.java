package com.topper.dex.decompiler.instructions;

import java.util.Arrays;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.ReferenceType;
import org.jf.dexlib2.iface.instruction.SwitchElement;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.util.ExceptionWithContext;

import com.topper.dex.decompiler.references.CallSiteReference;
import com.topper.dex.decompiler.references.FieldReference;
import com.topper.dex.decompiler.references.MethodHandleReference;
import com.topper.dex.decompiler.references.MethodProtoReference;
import com.topper.dex.decompiler.references.MethodReference;
import com.topper.dex.decompiler.references.StringReference;
import com.topper.dex.decompiler.references.TypeReference;

public final class DecompiledInstruction {

	private final BufferedInstruction instruction;

	private byte[] byteCode;

	public DecompiledInstruction(final BufferedInstruction instruction, final byte[] byteCode) {

		this.instruction = instruction;
		this.byteCode = Arrays.copyOf(byteCode, byteCode.length);
	}

	public final BufferedInstruction getInstruction() {
		return this.instruction;
	}

	public final byte[] getByteCode() {
		return this.byteCode;
	}

	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();
		for (final byte b : this.byteCode) {
			s.append(String.format("%02X", b) + " ");
		}
		s.append(this.instructionToString(this.instruction));
		return s.toString();
	}

	public String getInstructionString() {
		return this.instructionToString(this.instruction);
	}

	private final String instructionToString(final BufferedInstruction instruction) {
		
		Reference ref;
		int refType;
		final Opcode opcode = instruction.getOpcode();
		String instructionName = opcode + " ";

		switch (opcode.format) {
		case Format10t:
			instructionName += "+" + ((BufferedInstruction10t)instruction).getCodeOffset();
			break;
		case Format10x:
			break;
		case Format11n:
			instructionName += "v" + ((BufferedInstruction11n)instruction).getRegisterA() + ", ";
			instructionName += "#+" + ((BufferedInstruction11n)instruction).getWideLiteral();
			break;
		case Format11x:
			instructionName += "v" + ((BufferedInstruction11x)instruction).getRegisterA();
			break;
		case Format12x:
			instructionName += "v" + ((BufferedInstruction12x)instruction).getRegisterA() + ", ";
			instructionName += "v" + ((BufferedInstruction12x)instruction).getRegisterB();
			break;
		case Format20bc:
			ref = ((BufferedInstruction20bc)instruction).getReference();
			refType = ((BufferedInstruction20bc)instruction).getReferenceType();
			instructionName += ((BufferedInstruction20bc)instruction).getVerificationError() + ", ";
			instructionName += this.referenceToString(ref, refType);
			break;
		case Format20t:
			instructionName += "+" + ((BufferedInstruction20t)instruction).getCodeOffset();
			break;
		case Format21c:
			ref = ((BufferedInstruction21c)instruction).getReference();
			refType = ((BufferedInstruction21c)instruction).getReferenceType();
			instructionName += "v" + ((BufferedInstruction21c)instruction).getRegisterA() + ", ";
			instructionName += this.referenceToString(ref, refType);
			break;
		case Format21ih:
			instructionName += "v" + ((BufferedInstruction21ih)instruction).getRegisterA() + ", ";
			instructionName += "#+" + ((BufferedInstruction21ih)instruction).getWideLiteral();
			break;
		case Format21lh:
			instructionName += "v" + ((BufferedInstruction21lh)instruction).getRegisterA() + ",";
			instructionName += "#+" + ((BufferedInstruction21lh)instruction).getWideLiteral();
			break;
		case Format21s:
			instructionName += "v" + ((BufferedInstruction21s)instruction).getRegisterA() + ", ";
			instructionName += "#+" + ((BufferedInstruction21s)instruction).getWideLiteral();
			break;
		case Format21t:
			instructionName += "v" + ((BufferedInstruction21t)instruction).getRegisterA() + ", ";
			instructionName += "+" + ((BufferedInstruction21t)instruction).getCodeOffset();
			break;
		case Format22b:
			instructionName += "v" + ((BufferedInstruction22b)instruction).getRegisterA() + ", ";
			instructionName += "v" + ((BufferedInstruction22b)instruction).getRegisterB() + ", ";
			instructionName += "#+" + ((BufferedInstruction22b)instruction).getWideLiteral();
			break;
		case Format22c:
			ref = ((BufferedInstruction22c)instruction).getReference();
			refType = ((BufferedInstruction22c)instruction).getReferenceType();
			instructionName += "v" + ((BufferedInstruction22c)instruction).getRegisterA() + ", ";
			instructionName += "v" + ((BufferedInstruction22c)instruction).getRegisterB() + ", ";
			instructionName += this.referenceToString(ref, refType);
			break;
		case Format22cs:
			instructionName += "v" + ((BufferedInstruction22cs)instruction).getRegisterA() + ", ";
			instructionName += "v" + ((BufferedInstruction22cs)instruction).getRegisterB() + ", ";
			instructionName += "fieldoff@" + ((BufferedInstruction22cs)instruction).getFieldOffset();
			break;
		case Format22s:
			instructionName += "v" + ((BufferedInstruction22s)instruction).getRegisterA() + ", ";
			instructionName += "v" + ((BufferedInstruction22s)instruction).getRegisterB() + ", ";
			instructionName += "#+" + ((BufferedInstruction22s)instruction).getWideLiteral();
			break;
		case Format22t:
			instructionName += "v" + ((BufferedInstruction22t)instruction).getRegisterA() + ", ";
			instructionName += "v" + ((BufferedInstruction22t)instruction).getRegisterB() + ", ";
			instructionName += "+" + ((BufferedInstruction22t)instruction).getCodeOffset();
			break;
		case Format22x:
			instructionName += "v" + ((BufferedInstruction22x)instruction).getRegisterA() + ", ";
			instructionName += "v" + ((BufferedInstruction22x)instruction).getRegisterB();
			break;
		case Format23x:
			instructionName += "v" + ((BufferedInstruction23x)instruction).getRegisterA() + ", ";
			instructionName += "v" + ((BufferedInstruction23x)instruction).getRegisterB() + ", ";
			instructionName += "v" + ((BufferedInstruction23x)instruction).getRegisterC();
			break;
		case Format30t:
			instructionName += "+" + ((BufferedInstruction30t)instruction).getCodeOffset();
			break;
		case Format31c:
			ref = ((BufferedInstruction31c)instruction).getReference();
			refType = ((BufferedInstruction31c)instruction).getReferenceType();
			instructionName += "v" + ((BufferedInstruction31c)instruction).getRegisterA() + ", ";
			instructionName += this.referenceToString(ref, refType);
			break;
		case Format31i:
			instructionName += "v" + ((BufferedInstruction31i)instruction).getRegisterA() + ", ";
			instructionName += "#+" + ((BufferedInstruction31i)instruction).getWideLiteral();
			break;
		case Format31t:
			instructionName += "v" + ((BufferedInstruction31t)instruction).getRegisterA() + ", ";
			instructionName += "+" + ((BufferedInstruction31t)instruction).getCodeOffset();
			break;
		case Format32x:
			instructionName += "v" + ((BufferedInstruction32x)instruction).getRegisterA() + ", ";
			instructionName += "v" + ((BufferedInstruction32x)instruction).getRegisterB();
			break;
		case Format35c:
			final BufferedInstruction35c i35c = (BufferedInstruction35c)instruction;
			instructionName += "{" + this.registersToString(i35c.getRegisterCount(), i35c.getRegisterC(), i35c.getRegisterD(), i35c.getRegisterE(), i35c.getRegisterF(), i35c.getRegisterG()) + "}";
			ref = i35c.getReference();
			refType = i35c.getReferenceType();
			instructionName += ", " + this.referenceToString(ref, refType);
			break;
		case Format35ms:
			final BufferedInstruction35ms i35ms = (BufferedInstruction35ms)instruction;
			instructionName += "{" + this.registersToString(i35ms.getRegisterCount(), i35ms.getRegisterC(), i35ms.getRegisterD(), i35ms.getRegisterE(), i35ms.getRegisterF(), i35ms.getRegisterG()) + "}";
			instructionName += ", vtaboff@" + i35ms.getVtableIndex();
			break;
		case Format35mi:
			final BufferedInstruction35mi i35i = (BufferedInstruction35mi)instruction;
			instructionName += "{" + this.registersToString(i35i.getRegisterCount(), i35i.getRegisterC(), i35i.getRegisterD(), i35i.getRegisterE(), i35i.getRegisterF(), i35i.getRegisterG()) + "}";
			instructionName += ", vtaboff@" + i35i.getInlineIndex();
			break;
		case Format3rc:
			final BufferedInstruction3rc i3rc = (BufferedInstruction3rc)instruction;
			instructionName += "{" + this.variableRegistersToString(i3rc.getStartRegister(), i3rc.getRegisterCount()) + "}, ";
			ref = i3rc.getReference();
			refType = i3rc.getReferenceType();
			instructionName += this.referenceToString(ref, refType);
			break;

		case Format3rmi:
			final BufferedInstruction3rmi i3rmi = (BufferedInstruction3rmi)instruction;
			instructionName += "{" + this.variableRegistersToString(i3rmi.getStartRegister(), i3rmi.getRegisterCount()) + "}, ";
			instructionName += "inline@" + i3rmi.getInlineIndex();
			break;
		case Format3rms:
			final BufferedInstruction3rms i3rms = (BufferedInstruction3rms)instruction;
			instructionName += "{" + this.variableRegistersToString(i3rms.getStartRegister(), i3rms.getRegisterCount()) + "}, ";
			instructionName += "vtaboff@" + i3rms.getVtableIndex();
			break;
		case Format45cc:
			final BufferedInstruction45cc i45cc = (BufferedInstruction45cc)instruction;
			instructionName += "{" + this.registersToString(i45cc.getRegisterCount(), i45cc.getRegisterC(), i45cc.getRegisterD(), i45cc.getRegisterE(), i45cc.getRegisterF(), i45cc.getRegisterG()) + "}, ";
			ref = i45cc.getReference();
			refType =i45cc.getReferenceType();
			instructionName += this.referenceToString(ref, refType) + ", ";
			ref = i45cc.getReference2();
			refType = i45cc.getReferenceType2();
			instructionName += this.referenceToString(ref, refType);
			break;
		case Format4rcc:
			final BufferedInstruction4rcc i4rcc = (BufferedInstruction4rcc)instruction;
			instructionName += "{" + this.variableRegistersToString(i4rcc.getStartRegister(), i4rcc.getRegisterCount()) + "}, ";
			ref = i4rcc.getReference();
			refType = i4rcc.getReferenceType();
			instructionName += this.referenceToString(ref, refType) + ", ";
			ref = i4rcc.getReference2();
			refType = i4rcc.getReferenceType2();
			instructionName += this.referenceToString(ref, refType);
			break;
		case Format51l:
			final BufferedInstruction51l i51l = (BufferedInstruction51l)instruction;
			instructionName += "v" + i51l.getRegisterA() + ", ";
			instructionName += "#+" + i51l.getWideLiteral();
			break;
		case PackedSwitchPayload:
			final BufferedPackedSwitchPayload iPackedSwitch = (BufferedPackedSwitchPayload)instruction;
			String fPackedName = "PackedSwitch@{";
			for (int i = 0; i < iPackedSwitch.getSwitchElements().size(); i++) {

				final SwitchElement e = iPackedSwitch.getSwitchElements().get(i);
				fPackedName += "(" + e.getKey() + ": " + e.getOffset() + ")";
				if (i + 1 < iPackedSwitch.getSwitchElements().size()) {
					fPackedName += ", ";
				}
			}
			fPackedName += "}";
			instructionName += fPackedName;
			break;
		case SparseSwitchPayload:
			final BufferedSparseSwitchPayload iSparseSwitch = (BufferedSparseSwitchPayload)instruction;
			String fSparseName = "SparseSwitch@{";
			for (int i = 0; i < iSparseSwitch.getSwitchElements().size(); i++) {

				final SwitchElement e = iSparseSwitch.getSwitchElements().get(i);
				fSparseName += "(" + e.getKey() + ": " + e.getOffset() + ")";
				if (i + 1 < iSparseSwitch.getSwitchElements().size()) {
					fSparseName += ", ";
				}
			}
			fSparseName += "}";
			instructionName += fSparseName;
			break;
		case ArrayPayload:
			final BufferedArrayPayload iArray = (BufferedArrayPayload)instruction;
			String fArray = "Array@{";
			for (int i = 0; i < iArray.getArrayElements().size(); i++) {

				final Number e = iArray.getArrayElements().get(i);
				fArray += e.longValue();
				if (i + 1 < iArray.getArrayElements().size()) {
					fArray += ", ";
				}
			}
			fArray += "}";
			instructionName += fArray;
			break;
		default:
			throw new ExceptionWithContext("Unexpected opcode format: %s", opcode.format.toString());
		}
		return instructionName;
	}

	private final String registersToString(int count, int ... regs) {
		final StringBuilder s = new StringBuilder();
		for (int i = 0; i < regs.length && i < count; i++) {

			s.append("v" + regs[i]);
			if (i + 1 < regs.length && i + 1 < count) {
				s.append(", ");
			}
		}
		return s.toString();
	}

	private final String variableRegistersToString(int start, int count) {

		final StringBuilder s = new StringBuilder();
		for (int i = 0; i < count; i++) {
			s.append("v");
			s.append(start + i);
			if (i + 1 < count) {
				s.append(", ");
			}
		}
		return s.toString();
	}

	private final String referenceToString(final Reference reference, final int referenceType) {

		String name = "";

		switch (referenceType) {
		case ReferenceType.STRING:
			name += "STRING:";
			name += "\"" + ((StringReference)reference).getString() + "\"(" + ((StringReference)reference).getSize() + ")";
			break;
		case ReferenceType.TYPE:
			name += "TYPE:";
			name += ((TypeReference)reference).getType();
			break;
		case ReferenceType.METHOD:
			name += "METHOD:";
			final MethodReference methodRef = (MethodReference)reference;
			String methodName = methodRef.getDefiningClass() + "->" + methodRef.getName() + "(";
			for (int i = 0; i < methodRef.getParameterTypes().size(); i++) {
				final String param = methodRef.getParameterTypes().get(i);
				methodName += param;
				if (i + 1 < methodRef.getParameterTypes().size()) {
					methodName += ",";
				}
			}
			name += methodName;
			name += ")";
			name += methodRef.getReturnType();
			break;
		case ReferenceType.FIELD:
			name += "FIELD:";
			name += ((FieldReference)reference).getDefiningClass() + "->" + ((FieldReference)reference).getName() + ":" + ((FieldReference)reference).getType();
			break;
		case ReferenceType.METHOD_PROTO:
			name += "PROTO:";
			name += "proto@";
			final MethodProtoReference protoRef = (MethodProtoReference)reference;
			String protoName = protoRef.getReturnType() + "PROTO(";
			for (int i = 0; i < protoRef.getParameterTypes().size(); i++) {
				final String param = protoRef.getParameterTypes().get(i);
				protoName += param;
				if (i + 1 < protoRef.getParameterTypes().size()) {
					protoName += ",";
				}
			}
			name += protoName;
			break;
		case ReferenceType.METHOD_HANDLE:
			name += "METHOD_HANDLE:";
			name += "method_handle@";
			name += ((MethodHandleReference)reference).getMethodHandleType();
			break;
		case ReferenceType.CALL_SITE:
			name += "CALL_SITE:";
			name += "site@";
			final CallSiteReference callRef = (CallSiteReference)reference;
			name += callRef.getMethodName();
			break;
		default:
			throw new ExceptionWithContext("Invalid reference type: %d", referenceType);
		}

		return name;
	}
}
