package sporemodder.file.shaders;

import java.io.IOException;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;

public class ShaderDataUniform {
	public int dataIndex;  // short;
	public int field_2;  // short;
	public int registerSize;  // short; register size?
	public int register;  // short;
	public int flags;
	
	public void readCompiled(StreamReader in) throws IOException {
		dataIndex = in.readShort();
		field_2 = in.readShort();
		registerSize = in.readShort();
		register = in.readShort();
		flags = in.readInt();
	}

	public void write(StreamWriter out) throws IOException {
		out.writeShort(dataIndex);
		out.writeShort(field_2);
		out.writeShort(registerSize);
		out.writeShort(register);
		out.writeInt(flags);
	}
}
