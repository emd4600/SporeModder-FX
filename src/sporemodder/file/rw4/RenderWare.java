/****************************************************************************
* Copyright (C) 2019 Eric Mor
*
* This file is part of SporeModder FX.
*
* SporeModder FX is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
****************************************************************************/
package sporemodder.file.rw4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import emord.filestructures.FileStream;
import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.MainApp;
import sporemodder.file.dds.DDSTexture;
import sporemodder.file.rw4.RWHeader.RenderWareType;
import sporemodder.file.rw4.RWSectionSubReferences.SubReference;

public class RenderWare {
	
	public static final int INDEX_OBJECT = 0;
	public static final int INDEX_NO_OBJECT = 1;
	public static final int INDEX_SUB_REFERENCE = 2;

	private final List<RWObject> objects = new ArrayList<RWObject>();
	private final RWHeader header = new RWHeader(this);
	
	// Only updated when reading
	private final List<RWSectionInfo> sectionInfos = new ArrayList<>();
	
	public void read(StreamReader stream) throws IOException {
		header.read(stream, sectionInfos);
		
		// First we must create the objects
		// Don't read the objects themselves as they might reference an object that does not yet exist.
		for (RWSectionInfo info : sectionInfos) {
			//info.print();
			RWObject object = createObject(info.typeCode);
			objects.add(object);
			
			if (object != null) {
				object.sectionInfo = info;
			}
			else {
				System.err.println("Unrecognised RW section type: 0x" + Integer.toHexString(info.typeCode));
			}
		}
		// Now that all objects have been created, read the sub references
		header.sectionManifest.subReferences.readReferences(stream);
		
		// Read the objects
		for (RWObject object : objects) {

			if (object != null) {
				stream.seek(object.sectionInfo.pData);
				object.read(stream);
			}
		}
	}
	
	public void printInfo() {
		for (int i = 0; i < objects.size(); ++i) {
			System.out.println("##- " + i + "\t- " + objects.get(i).getClass().getSimpleName());
			objects.get(i).sectionInfo.print();
			System.out.println();
		}
	}
	
	private void writeAlignment(StreamWriter stream, int alignment) throws IOException {
		long offset = stream.getFilePointer();
		stream.writePadding((int) (((offset + alignment-1) & ~(alignment-1)) - offset));
	}
	
	public void write(StreamWriter stream) throws IOException {
		header.sectionManifest.subReferences.references.clear();
		// First we need to create the list with all the type codes
		List<Integer> typeCodes = header.sectionManifest.types.typeCodes;
		typeCodes.clear();
		typeCodes.add(0);
		typeCodes.add(RWBaseResource.TYPE_CODE);
		typeCodes.add(0x10031);
		typeCodes.add(0x10032);
		typeCodes.add(0x10010);
		
		// We will do it in a separate list because these have to be sorted
		Set<Integer> newTypeCodes = new TreeSet<Integer>();
		
		for (RWObject object : objects) {
			// Don't add the base resource one as this is special
			// Also don't repeat codes
			int typeCode = object.getTypeCode();
			if (typeCode != RWBaseResource.TYPE_CODE) newTypeCodes.add(typeCode);
		}
		
		typeCodes.addAll(newTypeCodes);
		
		// Now we would write the header BUT we don't have all
		// the information required, so write padding
		stream.writePadding(268 + typeCodes.size()*4);
		
		// Write all objects, creating section info for every one
		// We do not write base resources because they are special
		for (RWObject object : objects) {
			if (object.getTypeCode() != RWBaseResource.TYPE_CODE) {
				object.sectionInfo = new RWSectionInfo();
				object.sectionInfo.alignment = object.getAlignment();
				object.sectionInfo.typeCode = object.getTypeCode();
				object.sectionInfo.typeCodeIndex = typeCodes.indexOf(object.sectionInfo.typeCode);
				
				// Before getting the position, write the correct alignment
				writeAlignment(stream, object.sectionInfo.alignment);
				
				object.sectionInfo.pData = stream.getFilePointer();
				
				object.write(stream);
				
				object.sectionInfo.size = (int) (stream.getFilePointer() - object.sectionInfo.pData);
			}
		}
		
		long pSectionInfo = stream.getFilePointer();
		// Now we would write the section infos BUT we don't have all
		// the information required, so write padding
		stream.writePadding(objects.size()*24);
		
		// Now, the sub references
		header.sectionManifest.subReferences.offset = stream.getFilePointer();
		for (SubReference r : header.sectionManifest.subReferences.references) {
			stream.writeLEInt(indexOf(r.object));
			stream.writeLEInt(r.offset);
		}
		
		// I'm not sure if this is needed, but it doesn't hurt
		stream.writePadding(48);
		
		// Now write the BaseResources
		long pBufferData = stream.getFilePointer();
		for (RWObject object : objects) {
			if (object.getTypeCode() == RWBaseResource.TYPE_CODE) {
				object.sectionInfo = new RWSectionInfo();
				object.sectionInfo.alignment = object.getAlignment();
				object.sectionInfo.typeCode = object.getTypeCode();
				object.sectionInfo.typeCodeIndex = typeCodes.indexOf(object.sectionInfo.typeCode);
				
				// Before getting the position, write the correct alignment
				writeAlignment(stream, object.sectionInfo.alignment);
				
				long pos = stream.getFilePointer();
				object.sectionInfo.pData = pos - pBufferData;
				
				object.write(stream);
				
				object.sectionInfo.size = (int) (stream.getFilePointer() - pos);
			}
		}
		
		long buffersSize = stream.getFilePointer() - pBufferData;
		
		// Now we are done, we must do 2 things: rewrite header and rewrite section infos
		// 1.
		stream.seek(0);
		header.write(stream, pSectionInfo, pBufferData, buffersSize);
		
		// 2. 
		stream.seek(pSectionInfo);
		for (RWObject object : objects) {
			object.sectionInfo.write(stream);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends RWObject> List<T> getObjects(Class<T> type) {
		List<T> result = new ArrayList<T>();
		
		for (RWObject object : objects) {
			if (type.isInstance(object)) result.add((T) object);
		}
		
		return result;
	}
	
	public boolean isModel() {
		return header.type == RenderWareType.MODEL;
	}
	
	public boolean isTexture() {
		return header.type == RenderWareType.TEXTURE;
	}
	
	public boolean isSpecial() {
		return header.type == RenderWareType.SPECIAL;
	}
	
	public void setType(RenderWareType type) {
		header.type = type;
	}
	
	public DDSTexture toTextureNoExcept() throws IOException {
		if (!isTexture()) {
			return null;
		}
		
		List<RWRaster> rasters = getObjects(RWRaster.class);
		if (rasters.size() != 1) {
			return null;
		}
		
		return rasters.get(0).toDDSTexture();
	}
	
	public DDSTexture toTexture() throws IOException {
		if (!isTexture()) {
			throw new IOException("The RenderWare is not of TEXTURE type.");
		}
		
		List<RWRaster> rasters = getObjects(RWRaster.class);
		if (rasters.size() != 1) {
			throw new IOException("Unexpected number of textures in TEXTURE type RenderWare.");
		}
		
		return rasters.get(0).toDDSTexture();
	}
	
	public static RenderWare fromFile(File file) throws IOException {
		try (StreamReader stream = new FileStream(file, "r")) {
			RenderWare renderWare = new RenderWare();
			renderWare.read(stream);
			return renderWare;
		}
	}
	
	public static RenderWare fromTexture(DDSTexture texture) {
		RenderWare renderWare = new RenderWare();
		renderWare.setType(RenderWareType.TEXTURE);
		
		RWBaseResource dataBuffer = new RWBaseResource(renderWare);
		dataBuffer.data = texture.getData();
		
		RWRaster raster = new RWRaster(renderWare);
		raster.fromDDSTexture(texture);
		raster.textureData = dataBuffer;
		
		renderWare.add(dataBuffer);
		renderWare.add(raster);
		
		return renderWare;
	}
	
	public static void toTexture(File inputFile, File outputFile) throws IOException {
		try (StreamWriter stream = new FileStream(outputFile, "rw")) {
			fromFile(inputFile).toTexture().write(stream);
		}
	}
	
	public RWHeader getHeader() {
		return header;
	}

	/**
	 * Adds an object to this render ware.
	 * @param object
	 */
	public void add(RWObject object) {
		objects.add(object);
	}
	
	/**
	 * Returns the list of RenderWare objects contained in this class.
	 * @return
	 */
	public List<RWObject> getObjects() {
		return objects;
	}
	
	/** 
	 * Returns the object at the specified index. The first 2 bytes are used to determine
	 * the type of index (INDEX_OBJECT, etc):
	 * <li>If the index is of type INDEX_OBJECT, it returns an object from the objects list.
	 * <li>If the index is of type INDEX_SUB_REFERENCE, it returns an object from the sub references list.
	 * <li>If the index is of type INDEX_NO_OBJECT, it returns null.
	 * @param index
	 * @return
	 */
	public RWObject get(int index) {
		int sectionType = index >> 22;
		
		switch (sectionType) {
		case INDEX_OBJECT:
			return objects.get(index);
		case INDEX_SUB_REFERENCE:
			return header.sectionManifest.subReferences.references.get(index & 0x3FFFF).object;
		case INDEX_NO_OBJECT:
		default:
			return null;
		}
	}
	
	/**
	 * Returns the index of the specified object, used for referencing. The index is assumed to
	 * be of type INDEX_OBJECT, although it will return INDEX_NO_OBJECT if the parameter is null.
	 * This method returns -1 if the object is not present, and it must be interpreted as an error.
	 * @param object
	 * @return
	 */
	public int indexOf(RWObject object) {
		if (object == null) {
			return INDEX_NO_OBJECT << 22;	 
		}
		
		// We do it manually because List.indexOf calls equals(), which is not necessary
		for (int i = 0; i < objects.size(); i++) {
			if (object == objects.get(i)) {
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * Returns the index of the specified object, used for referencing. The index
	 * already contains information for the specified index type.
	 * @param object
	 * @return
	 */
	public int indexOf(RWObject object, int indexType) {
		
		switch (indexType) {
		case INDEX_OBJECT:
			return indexOf(object);
			
		case INDEX_SUB_REFERENCE:
			int index = INDEX_SUB_REFERENCE << 22;
			for (SubReference ref : header.sectionManifest.subReferences.references) {
				if (ref.object == object) {
					return index;
				}
				index++;
			}
			return -1;
			
		case INDEX_NO_OBJECT:
		default:
			return INDEX_NO_OBJECT << 22;	
		}
	}
	
	public int addReference(RWObject object, int offset) {
		header.sectionManifest.subReferences.references.add(new SubReference(object, offset));
		return (INDEX_SUB_REFERENCE << 22) | (header.sectionManifest.subReferences.references.size() - 1);
	}
	
	public RWObject createObject(int typeCode) {
		switch (typeCode) {
		case RWBaseResource.TYPE_CODE: return new RWBaseResource(this);
		case RWRaster.TYPE_CODE: return new RWRaster(this);
		case RWVertexDescription.TYPE_CODE: return new RWVertexDescription(this);
		case RWVertexBuffer.TYPE_CODE: return new RWVertexBuffer(this);
		case RWIndexBuffer.TYPE_CODE: return new RWIndexBuffer(this);
		case RWSkinMatrixBuffer.TYPE_CODE: return new RWSkinMatrixBuffer(this);
		case RWAnimationSkin.TYPE_CODE: return new RWAnimationSkin(this);
		case RWMesh.TYPE_CODE: return new RWMesh(this);
		case RWMeshCompiledStateLink.TYPE_CODE: return new RWMeshCompiledStateLink(this);
		case RWCompiledState.TYPE_CODE: return new RWCompiledState(this);
		case RWSkinsInK.TYPE_CODE: return new RWSkinsInK(this);
		case RWSkeletonsInK.TYPE_CODE: return new RWSkeletonsInK(this);
		case RWSkeleton.TYPE_CODE: return new RWSkeleton(this);
		case RWBBox.TYPE_CODE: return new RWBBox(this);
		case RWMorphHandle.TYPE_CODE: return new RWMorphHandle(this);
		case RWTriangleKDTreeProcedural.TYPE_CODE: return new RWTriangleKDTreeProcedural(this);
		case RWAnimations.TYPE_CODE: return new RWAnimations(this);
		case RWKeyframeAnim.TYPE_CODE: return new RWKeyframeAnim(this);
		case RWBlendShape.TYPE_CODE: return new RWBlendShape(this);
		case RWBlendShapeBuffer.TYPE_CODE: return new RWBlendShapeBuffer(this);
		default:
			return null;
		}
	}

	/**
	 * Opens the file to check the type of RenderWare it is. This does not read the contents of the file.
	 * @param file
	 * @return 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static RenderWareType peekType(File file) throws FileNotFoundException, IOException {
		try (FileStream stream = new FileStream(file, "r")) {
			stream.skip(28);
			return RenderWareType.get(stream.readLEInt());
		}
	}

	public String getName(RWObject object) {
		return object.getClass().getSimpleName() + '-' + sectionInfos.indexOf(object.sectionInfo);
	}
	
	public static void main(String[] args) throws IOException {
		MainApp.testInit();
		//String path = "C:\\Users\\Eric\\Desktop\\ce_grasper_radial_03.rw4";
		//String path = "C:\\Users\\Eric\\Desktop\\be_classic_01.rw4";
		//String path = "C:\\Users\\Eric\\Desktop\\Willosaur Mouth rig 4.rw4";
		//String path = "C:\\Users\\Eric\\Desktop\\ce_grasper_radial_01.rw4";
		//String path = "C:\\Users\\Eric\\Desktop\\ce_mouth_beak_herbivore_04.rw4";  // ce_mouth_jaw_carnivore_01
		//String path = "C:\\Users\\Eric\\Desktop\\ShapeKeys.rw4";
		//String path = "C:\\Users\\Eric\\Desktop\\ce_weapon_slasher_04.rw4";
		//String path = "C:\\Users\\Eric\\Downloads\\raw.rw4";
		//String path = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\CustomPartTutorial\\editor_rigblocks~\\PartTutorial_ce_mouths_test.rw4";
		//String path = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\DebuggingTest\\editor_rigblocks~\\ve_DI_spikeChest_01.rw4";
		//String path = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\DebuggingTest\\editor_rigblocks~\\be_details_house_10.rw4";
		//String path = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\GridProject\\TestModels\\ce_mouth_mandible_carnivore_04.rw4";
		//String path = "C:\\Users\\Eric\\Desktop\\ce_shapes_droneBase_polished_01.rw4";
//		String path = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Spore (Game & Graphics)\\editor_rigblocks~";
		
		//RenderWare renderWare = RenderWare.fromFile(new File(path));
		//renderWare.printInfo();
		
		String inputPath = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\ModCreatorKit_\\camera_properties~\\ce_prisms_drone_01_A_TL.rw4";
		String outputPath = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\ModCreatorKit_\\camera_properties~\\ce_prisms_drone_01_A_TL test.rw4";
		
		RenderWare renderWare = RenderWare.fromFile(new File(inputPath));
	
		try (FileStream stream = new FileStream(outputPath, "rw")) {
			renderWare.write(stream);
		}
		
//		Map<Integer, List<String>> nameSet = new HashMap<>();
//		
//		for (File file : new File(path).listFiles()) {
//			if (file.getName().endsWith(".rw4") && file.getName().startsWith("ce_details_")) {
//				try {
//					RenderWare renderWare = RenderWare.fromFile(file);
//					List<RWAnimations> anims = renderWare.getObjects(RWAnimations.class);
//					if (!anims.isEmpty()) {
//						for (Integer i : anims.get(0).animations.keySet()) {
//							List<String> list = nameSet.get(i);
//							if (list == null) {
//								list = new ArrayList<>();
//								nameSet.put(i, list);
//							}
//							list.add(file.getName());
//						}
//					}
//				}
//				catch (Exception e) {
//					System.err.println("Error reading " + file.getName());
//				}
//			}
//		}
//		
//		System.out.println();
//		System.out.println();
//		
//		for (Integer i : nameSet.keySet()) {
//			System.out.print(HashManager.get().getFileName(i) + "\t - ");
//			for (String s : nameSet.get(i)) System.out.print(s + " ");
//			System.out.println();
//		}

//		List<RWSkeleton> list = renderWare.getObjects(RWSkeleton.class);
//		if (!list.isEmpty()) {
//			for (RWSkeleton sk : list) {
//				System.out.println("## SKELETON: " + HashManager.get().getFileName(sk.skeletonID));
//				for (Bone bone : sk.bones) {
//					System.out.println(bone);
//				}
//				System.out.println();
//			}
//		}
	}
}
