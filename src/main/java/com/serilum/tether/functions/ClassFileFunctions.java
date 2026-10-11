package com.serilum.tether.functions;

import com.serilum.tether.data.ClassRenames;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ClassFileFunctions {
	private static final int CONSTANT_UTF8 = 1;
	private static final int CONSTANT_LONG = 5;
	private static final int CONSTANT_DOUBLE = 6;

	public static byte[] renameClasses(byte[] classBytes, ClassRenames renames) throws IOException {
		// Only the constant pool is rewritten, so a newer Java class file version can't break this the way it can break ASM.
		DataInputStream input = new DataInputStream(new ByteArrayInputStream(classBytes));
		ByteArrayOutputStream outputBytes = new ByteArrayOutputStream(classBytes.length + 256);
		DataOutputStream output = new DataOutputStream(outputBytes);

		output.writeInt(input.readInt());
		output.writeShort(input.readUnsignedShort());
		output.writeShort(input.readUnsignedShort());

		int constantPoolCount = input.readUnsignedShort();
		output.writeShort(constantPoolCount);

		boolean renamedAny = false;
		for (int i = 1; i < constantPoolCount; i++) {
			int tag = input.readUnsignedByte();
			output.writeByte(tag);

			if (tag == CONSTANT_UTF8) {
				String value = input.readUTF();
				String renamedValue = RenameFunctions.renameReferences(value, renames);
				if (!renamedValue.equals(value)) {
					renamedAny = true;
				}

				output.writeUTF(renamedValue);
				continue;
			}

			byte[] constant = new byte[getConstantSize(tag)];
			input.readFully(constant);
			output.write(constant);

			if (tag == CONSTANT_LONG || tag == CONSTANT_DOUBLE) {
				// Longs and doubles take up two slots in the constant pool.
				i++;
			}
		}

		if (!renamedAny) {
			return classBytes;
		}

		input.transferTo(output);
		return outputBytes.toByteArray();
	}

	private static int getConstantSize(int tag) throws IOException {
		return switch (tag) {
			case 7, 8, 16, 19, 20 -> 2;
			case 15 -> 3;
			case 3, 4, 9, 10, 11, 12, 17, 18 -> 4;
			case 5, 6 -> 8;
			default -> throw new IOException("Unknown constant pool tag " + tag + ".");
		};
	}
}
