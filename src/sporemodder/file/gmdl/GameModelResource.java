package sporemodder.file.gmdl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import sporemodder.file.BoundingBox;
import sporemodder.file.ResourceKey;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.rw4.RWVertexElement;
import sporemodder.file.shaders.ShaderData;
import sporemodder.util.Transform;

/**
 * GMDL files
 */
public class GameModelResource {
	
	public static final int DATA_TEXTURE_SET = 0x20D;
	
	public static class IndexBuffer {
		public int[] indices;
		public int primitiveType;
		public int numBits;
		
		public void read(StreamReader stream) throws IOException {
			primitiveType = stream.readLEInt();
			int count = stream.readLEInt();
			numBits = stream.readLEInt();
			stream.readLEInt();  // buffer size
			
			indices = new int[count];
			
			if (numBits == 16) {
				stream.readLEUShorts(indices);
			}
			else if (numBits == 32) {
				stream.readLEInts(indices);
			}
			else {
				throw new IOException("Unsupported GMDL index buffer bits " + numBits);
			}
		}
		
		public void write(StreamWriter stream) throws IOException {
			stream.writeLEInt(primitiveType);
			stream.writeLEInt(indices.length);
			stream.writeLEInt(numBits);
			if (numBits == 16) {
				stream.writeLEInt(indices.length * 2);
				stream.writeLEUShorts(indices);
			}
			else if (numBits == 32) {
				stream.writeLEInt(indices.length * 4);
				stream.writeLEInts(indices);
			}
			else {
				throw new IOException("Unsupported GMDL index buffer bits " + numBits);
			}
		}
	}
	
	public static class VertexDescriptor {
		public final List<RWVertexElement> elements = new ArrayList<RWVertexElement>();
		
		public void read(StreamReader stream) throws IOException {
			int count = stream.readLEInt();
			for (int i = 0; i < count; i++) {
				RWVertexElement element = new RWVertexElement();
				element.read(stream);
				elements.add(element);
			}
		}
		
		public void write(StreamWriter stream) throws IOException {
			stream.writeLEInt(elements.size());
			for (RWVertexElement element : elements) {
				element.write(stream);
			}
		}
	}
	
	public static class VertexBuffer {
		public byte[] data;
		public int vertexCount;
		public VertexDescriptor descriptor;
		
		public void read(StreamReader stream, List<VertexDescriptor> descriptors) throws IOException {
			int index = stream.readLEInt();
			descriptor = descriptors.get(index);
			vertexCount = stream.readLEInt();
			int bufferSize = stream.readLEInt();
			data = new byte[bufferSize];
			stream.read(data);
		}
		
		public void write(StreamWriter stream, List<VertexDescriptor> descriptors) throws IOException {
			int index = -1;
			for (int i = 0; i < descriptors.size(); i++) {
				if (descriptors.get(i) == descriptor) {
					index = i;
					break;
				}
			}
			stream.writeLEInt(index);
			stream.writeLEInt(vertexCount);
			stream.writeLEInt(data.length);
			stream.write(data);
		}
	}
	
	public static class Mesh {
		public VertexBuffer vertexBuffer;
		public IndexBuffer indexBuffer;
		public int materialID;
	}
	
	public static class TextureEntry {
		public static final int FLAGS_ADDRESS = 1;
		public static final int FLAGS_FILTER = 2;
		public static final int FLAGS_MIP_FILTER = 4;
		public static final int FLAGS_UV_MAPPING = 8;
		
		public int samplerIndex;
		public final byte[] extraData = new byte[12];
		public int instanceID;
		public int groupID;
		
		public void copy(TextureEntry other) {
			samplerIndex = other.samplerIndex;
			System.arraycopy(other.extraData, 0, extraData, 0, extraData.length);
			instanceID = other.instanceID;
			groupID = other.groupID;
		}
		
		public void read(StreamReader stream) throws IOException {
			samplerIndex = stream.readLEInt();
			stream.read(extraData);
			instanceID = stream.readLEInt();
			groupID = stream.readLEInt();
		}
		
		public void write(StreamWriter stream) throws IOException {
			stream.writeLEInt(samplerIndex);
			stream.write(extraData);
			stream.writeLEInt(instanceID);
			stream.writeLEInt(groupID);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(extraData);
			result = prime * result + Objects.hash(groupID, instanceID, samplerIndex);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TextureEntry other = (TextureEntry) obj;
			return Arrays.equals(extraData, other.extraData) && groupID == other.groupID
					&& instanceID == other.instanceID && samplerIndex == other.samplerIndex;
		}
	}
	
	public static class MaterialInfo {
		public final List<TextureEntry> textures = new ArrayList<>();
		public final Map<Integer, byte[]> shaderData = new HashMap<>();
		
		public void read(StreamReader stream, int version) throws IOException {
			if (version != 9 || stream.readInt() != 0) {
				int count = stream.readLEInt();
				for (int i = 0; i < count; i++) {
					int dataIndex = stream.readLEInt();
					if (dataIndex == DATA_TEXTURE_SET) {
						int textureCount = stream.readLEInt();
						for (int t = 0; t < textureCount; t++) {
							TextureEntry texture = new TextureEntry();
							texture.read(stream);
							textures.add(texture);
						}
					}
					else {
						int dataSize = ShaderData.getDataSize(dataIndex);
						byte[] data = new byte[dataSize];
						stream.read(data);
						shaderData.put(dataIndex, data);
					}
				}
			}
		}
		
		public void write(StreamWriter stream) throws IOException {
			stream.writeLEInt(1);
			stream.writeLEInt(shaderData.size() + (textures.isEmpty() ? 0 : 1));
			
			if (!textures.isEmpty()) {
				stream.writeLEInt(DATA_TEXTURE_SET);
				stream.writeLEInt(textures.size());
				for (TextureEntry texture : textures) {
					texture.write(stream);
				}
			}
			for (Map.Entry<Integer, byte[]> entry : shaderData.entrySet()) {
				stream.writeLEInt(entry.getKey());
				stream.write(entry.getValue());
			}
		}
	}
	
	public static class BoneRange {
		public int field_0;
		public int field_4;
		
		public void read(StreamReader stream) throws IOException {
			field_0 = stream.readLEInt();
			field_4 = stream.readLEInt();
		}
		
		public void write(StreamWriter stream) throws IOException {
			stream.writeLEInt(field_0);
			stream.writeLEInt(field_4);
		}
	}
	
	public static class BakedDeformBoneInfo {
		public final byte[] data = new byte[0xA0];
	}
	
	public static class BakedDeforms {
		public final List<BakedDeformBoneInfo> boneInfos = new ArrayList<>();
		
		public void read(StreamReader stream) throws IOException {
			int count = stream.readLEInt();
			for (int i = 0; i < count; i++) {
				BakedDeformBoneInfo boneInfo = new BakedDeformBoneInfo();
				stream.read(boneInfo.data);
				boneInfos.add(boneInfo);
			}
		}
		
		public void write(StreamWriter stream) throws IOException {
			stream.writeLEInt(boneInfos.size());
			for (BakedDeformBoneInfo boneInfo : boneInfos) {
				stream.write(boneInfo.data);
			}
		}
	}
	
	public static class GameModelAnimData {
		public final Transform transform = new Transform();
		public final Transform field_38 = new Transform();
		public int field_70;
		public final ResourceKey key = new ResourceKey();
		public final List<BakedDeforms> bakedDeforms = new ArrayList<>();
		
		public void read(StreamReader stream) throws IOException {
			transform.readComplete(stream);
			field_38.readComplete(stream);
			field_70 = stream.readLEInt();
			key.readLE(stream);
			
			int count = stream.readLEInt();
			for (int i = 0; i < count; i++) {
				BakedDeforms deforms = new BakedDeforms();
				deforms.read(stream);
				bakedDeforms.add(deforms);
			}
		}
		
		public void write(StreamWriter stream) throws IOException {
			transform.writeComplete(stream);
			field_38.writeComplete(stream);
			stream.writeLEInt(field_70);
			key.writeLE(stream);
			stream.writeLEInt(bakedDeforms.size());
			for (BakedDeforms deforms : bakedDeforms) {
				deforms.write(stream);
			}
		}
	}
	
	public final List<ResourceKey> referencedFiles = new ArrayList<>();
	public final BoundingBox boundingBox = new BoundingBox();
	public float boundingRadius;
	public final List<IndexBuffer> indexBuffers = new ArrayList<>();
	public final List<VertexDescriptor> vertexDescriptors = new ArrayList<>();
	public final List<VertexBuffer> vertexBuffers = new ArrayList<>();
	public final List<Mesh> meshes = new ArrayList<>();
	public final List<MaterialInfo> materialInfos = new ArrayList<>();
	public final List<BoneRange> boneRanges = new ArrayList<>();
	public final List<GameModelAnimData> animDatas = new ArrayList<>();
	public final ResourceKey unknownKey = new ResourceKey(0, 0, -1); 
	
	public void read(StreamReader stream) throws IOException
	{
		int version = stream.readLEInt();
		if (version > 9)
			throw new IOException("Unsupported GMDL version " + version);
		
		int count = stream.readInt();
		for (int i = 0; i < count; i++) {
			ResourceKey key = new ResourceKey();
			key.setInstanceID(stream.readInt());
			key.setGroupID(stream.readInt());
			key.setTypeID(stream.readInt());
			referencedFiles.add(key);
		}
		
		int meshCount = stream.readLEInt();
		boundingBox.read(stream);
		boundingRadius = stream.readLEFloat();
		
		int numIndexBuffers = stream.readLEInt();
		for (int i = 0; i < numIndexBuffers; i++) {
			IndexBuffer buffer = new IndexBuffer();
			buffer.read(stream);
			indexBuffers.add(buffer);
		}
		
		int numVertexDescriptors = stream.readLEInt();
		for (int i = 0; i < numVertexDescriptors; i++) {
			VertexDescriptor buffer = new VertexDescriptor();
			buffer.read(stream);
			vertexDescriptors.add(buffer);
		}
		
		int numVertexBuffers = stream.readLEInt();
		for (int i = 0; i < numVertexBuffers; i++) {
			VertexBuffer buffer = new VertexBuffer();
			buffer.read(stream, vertexDescriptors);
			vertexBuffers.add(buffer);
		}
		
		for (int i = 0; i < meshCount; i++) {
			Mesh mesh = new Mesh();
			mesh.vertexBuffer = vertexBuffers.get(stream.readLEInt());
			mesh.indexBuffer = indexBuffers.get(stream.readLEInt());
			meshes.add(mesh);
		}
		
		for (int i = 0; i < meshCount; i++) {
			meshes.get(i).materialID = stream.readLEInt();
		}
		
		if (stream.readLEInt() != 0)
			throw new IOException("Unsupported GMDL unk count in position " + stream.getFilePointer());
		
		int materialInfoCount = stream.readLEInt();
		for (int i = 0; i < materialInfoCount; i++) {
			MaterialInfo info = new MaterialInfo();
			info.read(stream, version);
			materialInfos.add(info);
		}
		
		int boneRangeCount = stream.readLEInt();
		for (int i = 0; i < boneRangeCount; i++) {
			BoneRange range = new BoneRange();
			range.read(stream);
			boneRanges.add(range);
		}
		
		int animDataCount = stream.readLEInt();
		for (int i = 0; i < animDataCount; i++) {
			GameModelAnimData animData = new GameModelAnimData();
			animData.read(stream);
			animDatas.add(animData);
		}
		
		unknownKey.readLE(stream);
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(9);
		
		stream.writeInt(referencedFiles.size());
		for (ResourceKey key : referencedFiles) {
			stream.writeInt(key.getInstanceID());
			stream.writeInt(key.getGroupID());
			stream.writeInt(key.getTypeID());
		}
		
		stream.writeLEInt(meshes.size());
		boundingBox.write(stream);
		stream.writeLEFloat(boundingRadius);
		
		stream.writeLEInt(indexBuffers.size());
		for (IndexBuffer entry : indexBuffers) {
			entry.write(stream);
		}
		
		stream.writeLEInt(vertexDescriptors.size());
		for (VertexDescriptor entry : vertexDescriptors) {
			entry.write(stream);
		}
		
		stream.writeLEInt(vertexBuffers.size());
		for (VertexBuffer entry : vertexBuffers) {
			entry.write(stream, vertexDescriptors);
		}
		
		for (Mesh mesh : meshes) {
			stream.writeLEInt(vertexBuffers.indexOf(mesh.vertexBuffer));
			stream.writeLEInt(indexBuffers.indexOf(mesh.indexBuffer));
		}
		
		for (Mesh mesh : meshes) {
			stream.writeLEInt(mesh.materialID);
		}
		
		stream.writeLEInt(0);
		
		stream.writeLEInt(materialInfos.size());
		for (MaterialInfo entry : materialInfos) {
			entry.write(stream);
		}
		
		stream.writeLEInt(boneRanges.size());
		for (BoneRange entry : boneRanges) {
			entry.write(stream);
		}
		
		stream.writeLEInt(animDatas.size());
		for (GameModelAnimData entry : animDatas) {
			entry.write(stream);
		}
		
		unknownKey.writeLE(stream);
	}
}
