package sporemodder.file.shaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import emord.filestructures.StreamReader;

public class CompiledShader {

	public final int[] fragmentIndices = new int[32];
	public byte[] data;
	public final List<ShaderDataUniform> dataUniforms = new ArrayList<>();
	public final List<Integer> startRegisters = new ArrayList<>();
	public int flags;  // field_12C
	
	public String getSignatureString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fragmentIndices.length; ++i) {
			if (fragmentIndices[i] == 0) break;
			String text = Integer.toHexString(fragmentIndices[i]);
			if (text.length() == 1) sb.append('0');
			sb.append(text);
		}
		return sb.toString();
	}
	
	public void read(StreamReader in) throws IOException {
		in.readUBytes(fragmentIndices);
		
		data = new byte[in.readInt()];
		in.read(data);
		
		int count = in.readInt();
		for (int i = 0; i < count; i++) {
			ShaderDataUniform uniform = new ShaderDataUniform();
			uniform.readCompiled(in);
			dataUniforms.add(uniform);
		}
		
		for (int i = 0; i < count; ++i) {
			startRegisters.add(in.readInt());
		}
		
		flags = in.readInt();
	}
}
