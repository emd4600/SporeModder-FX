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
package sporemodder.file.effects;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sporemodder.HashManager;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.dbpf.DBPFPacker;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

public class EffectDirectory {
	
	public static final String HYPERLINK_FILE = "file";
	public static final String HYPERLINK_TEXTURE = "file-texture";
	public static final String HYPERLINK_IMAGEMAP = "file-imagemap";
	public static final String HYPERLINK_MATERIAL = "material";
	public static final String HYPERLINK_MAP = "map";
	public static final String HYPERLINK_SPLITTER = "splitter";
	
	public static String getHyperlinkType(EffectComponent element) {
		if (element.getFactory() == null) return ImportEffect.KEYWORD;
		else return element.getFactory().getKeyword();
	}
	
	private static final int MAX_TYPECODE = 0x40;
	private static final int MAX_RESOURCECODE = 0x16;
	
	private static final int SUPPORTED_VERSION = 4;
	
	/* particle	0x1 *
	 * metaParticle	0x2
	 * decal	0x3 *
	 * sequence	0x4 *
	 * sound	0x5 *
	 * shake	0x6 *
	 * camera	0x7 *
	 * model	0x8 *
	 * screen	0x9 *
	 * light	0xA *
	 * game		0xB *
	 * fastParticle	0xC *
	 * distribute	0xD
	 * ribbon	0xE *
	 * 
	 * brush	0x20 *
	 * terrainScript	0x21 *
	 * skinpaintSettings	0x22 *
	 * skinpaintDistribute	0x23 *
	 * 
	 * gameModel	0x25
	 * skinpaintParticle	0x26 *
	 * skinpaintFlood	0x27 *
	 * volume	0x28 *
	 * splitController	0x29 *
	 * terrainDistribute	0x2A *
	 * cloud	0x2B *
	 * groundCover	0x2C *
	 * mixEvent	0x2D *
	 * 
	 * text		0x2F *
	 */
	
	private static final EffectComponentFactory[] factories = new EffectComponentFactory[MAX_TYPECODE];
	
	static {
		addFactory(VisualEffect.FACTORY);
		
		addFactory(ParticleEffect.FACTORY);
		addFactory(MetaParticleEffect.FACTORY);
		addFactory(DecalEffect.FACTORY);
		addFactory(SequenceEffect.FACTORY);
		addFactory(SoundEffect.FACTORY);
		addFactory(ShakeEffect.FACTORY);
		addFactory(CameraEffect.FACTORY);
		addFactory(ModelEffect.FACTORY);
		addFactory(ScreenEffect.FACTORY);
		addFactory(LightEffect.FACTORY);
		addFactory(GameEffect.FACTORY);
		addFactory(FastParticleEffect.FACTORY);
		addFactory(DistributeEffect.FACTORY);
		addFactory(RibbonEffect.FACTORY);
		 
		addFactory(BrushEffect.FACTORY);
		addFactory(TerrainScriptEffect.FACTORY);
		addFactory(SkinpaintSettingsEffect.FACTORY);
		addFactory(SkinpaintDistributeEffect.FACTORY);
		
		addFactory(GameModelEffect.FACTORY);
		addFactory(SkinpaintParticleEffect.FACTORY);
		addFactory(SkinpaintFloodEffect.FACTORY);
		addFactory(VolumeEffect.FACTORY);
		addFactory(SplitControllerEffect.FACTORY);
		addFactory(TerrainDistributeEffect.FACTORY);
		addFactory(CloudEffect.FACTORY);
		addFactory(GroundCoverEffect.FACTORY);
		addFactory(MixEventEffect.FACTORY);
		
		addFactory(TextEffect.FACTORY);
	}
	
	private static final EffectResourceFactory[] resourceFactories = new EffectResourceFactory[MAX_RESOURCECODE];
	
	static {
		addFactory(MapResource.FACTORY);
		addFactory(MaterialResource.FACTORY);
		addFactory(SplitterResource.FACTORY);
	}
	
	
	private int version = SUPPORTED_VERSION;
	private final List<List<EffectComponent>> components = new ArrayList<List<EffectComponent>>(MAX_TYPECODE);
	private final List<List<EffectResource>> resources = new ArrayList<List<EffectResource>>(MAX_RESOURCECODE);
	
	private final List<EffectUnit> units = new ArrayList<EffectUnit>();
	
	private final Map<String, VisualEffect> exports = new HashMap<String, VisualEffect>();
	private final List<ImportEffect> imports = new ArrayList<ImportEffect>();
	private final Map<String, ImportEffect> exportedImports = new HashMap<String, ImportEffect>();
	
	/** For debugging purposes. */
	private final Map<Object, Long> fileOffsets = new HashMap<>();
	private static final boolean OUTPUT_FILE_OFFSETS = false;
	
	public EffectDirectory() {
		
		for (int i = 0; i < MAX_TYPECODE; i++) {
			components.add(null);
		}
		
		for (int i = 0; i < MAX_RESOURCECODE; i++) {
			resources.add(null);
		}
	}
	
	public static void addFactory(EffectComponentFactory factory) {
		factories[factory.getTypeCode()] = factory;
	}
	
	public static EffectComponentFactory[] getFactories() {
		return factories;
	}
	
	public static void addFactory(EffectResourceFactory factory) {
		resourceFactories[factory.getTypeCode()] = factory;
	}
	
	public static EffectResourceFactory[] getResourceFactories() {
		return resourceFactories;
	}

	public EffectComponent getEffect(int type, int index) {
		if (index == -1) return null;
		
		if ((index & ImportEffect.MASK) == ImportEffect.MASK) {
			index = index & ~ImportEffect.MASK;
			// Now, imports are read last which means that probably, this index does not exist
			// Since import effects are just a name, we can create them here and then assign it later when we read it
			if (index >= imports.size()) {
				for (int i = imports.size(); i <= index; ++i) {
					imports.add(new ImportEffect(this));
				}
			}
			return imports.get(index);
		}
		else {
			List<EffectComponent> list = components.get(type);
			
			if (list != null) return list.get(index);
			else return null;
		}
	}
	
	public EffectResource getResource(int type, int index) {
		if (index == -1) return null;
		List<EffectResource> list = resources.get(type);
		
		if (list != null) {
			return list.get(index);
		}
		else {
			return null;
		}
	}
	
	public EffectResource getResource(int type, ResourceID resource) {
		int name = resource.getInstanceID();
		if (name == -1) return null;
		List<EffectResource> list = resources.get(type);
		
		if (list != null) {
			for (EffectResource res : list) {
				if (res.resourceID.getInstanceID() == name && res.resourceID.getGroupID() == resource.getGroupID()) {
					return res;
				}
			}
		}
		
		return null;
	}
	
	public List<EffectComponent> getComponents(int type) {
		return Collections.unmodifiableList(components.get(type) != null ? components.get(type) : Collections.emptyList());
	}
	
	public void addComponent(EffectComponent component) {
		EffectComponentFactory factory = component.getFactory();
		
		// Imports do not use factories
		if (factory == null) {
			ImportEffect imp = (ImportEffect) component;
			imports.add(imp);
		}
		else {
			int index = factory.getTypeCode();
			List<EffectComponent> list = components.get(index);
			if (list == null) {
				list = new ArrayList<EffectComponent>();
				components.set(index, list);
			}
			list.add(component);
		}
	}
	
	public void addResource(EffectResource resource) {
		int index = resource.getFactory().getTypeCode();
		List<EffectResource> list = resources.get(index);
		if (list == null) {
			list = new ArrayList<EffectResource>();
			resources.set(index, list);
		}
		list.add(resource);
	}
	
	public void addEffectUnit(EffectUnit unit) {
		units.add(unit);
		
		for (EffectComponent component : unit.getComponents().values()) {
			addComponent(component);
		}
		
		for (EffectResource resource : unit.getResources().values()) {
			addResource(resource);
		}
		
		exports.putAll(unit.getExports());
		
		exportedImports.putAll(unit.getExportedImports());
	}
	
	/**
	 * Fins the index of the component in the given list, the component must exactly be the same object.
	 * Unlike the List.indexOf() method, this does not use equals() but == instead, so it is faster.
	 * @param list
	 * @param component
	 * @return The index, or -1 if the component is not in the list.
	 */
	private static <T> int findIndex(List<T> list, T component) {
		if (list != null) {
			int size = list.size();
			for (int i = 0; i < size; i++) {
				if (list.get(i) == component) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public int getIndex(int blockType, EffectComponent component) {
		if (component == null) return -1;
		if (component.getFactory() == null) {
			return findIndex(imports, (ImportEffect) component) | ImportEffect.MASK;
		}
		else {
			return findIndex(components.get(blockType), component);
		}
	}
	
	public int getIndex(int resourceType, EffectResource resource) {
		if (resource == null) return -1;
		return findIndex(resources.get(resourceType), resource);
	}
	
	public void process(File folder, DBPFPacker packer) throws IOException {
		for (File file : folder.listFiles()) {
			if (file.getName().endsWith(".pfx")) {
				if (packer != null) packer.setCurrentFile(file);
				
				processUnit(file, folder);
			}
		}
	}
	
	public void processUnit(File file, File parentFolder) throws IOException {
		EffectUnit unit = new EffectUnit(this);
		ArgScriptStream<EffectUnit> stream = unit.generateStream();
		stream.setFastParsing(true);
		stream.setFolder(parentFolder);
		
		stream.process(file);
		
		// Stop reading .PFX files if one of them has errors
		if (!stream.getErrors().isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("File ");
			sb.append(file.getName());
			sb.append(" cannot be compiled, ");
			sb.append(stream.getErrors().size());
			sb.append(" errors found.");
			for (int i = 0; i < Math.min(stream.getErrors().size(), 4); i++) {
				sb.append("\n [Line ");
				sb.append(stream.getErrors().get(i).getLine());
				sb.append("]: ");
				sb.append(stream.getErrors().get(i).getMessage());
			}
			throw new IOException(sb.toString());
		}
		
		addEffectUnit(unit);
	}
	
	public void read(StreamReader stream) throws IOException {
		version = stream.readShort();
		if (version != SUPPORTED_VERSION) {
			throw new IOException("Unsupported EFFDIR version " + version);
		}
		
		// 1. Normal components
		// 2. Resources
		// 3. Effects
		// 4. Exports
		// 5. Imports
		
		// Since some components/resources might use other components/resources that have not been read yet,
		// we will do a first pass to create all the elements and a second to read them
		
		long[] componentOffsets = new long[factories.length];
		
		// -- NORMAL COMPONENTS -- //
		int type;
		while ((type = stream.readShort()) != -1) {
			int version = stream.readShort();
			int size = stream.readInt();
			long offset = stream.getFilePointer();
			int count = stream.readInt();
			
			componentOffsets[type] = offset + 4;
			
			if (type < factories.length && factories[type] != null &&
					version >= factories[type].getMinVersion() && version <= factories[type].getMaxVersion()) {
				List<EffectComponent> list = new ArrayList<EffectComponent>(count);
				components.set(type, list);
				
				String keyword = factories[type].getKeyword();
				
				for (int i = 0; i < count; ++i) {
					EffectComponent component = factories[type].create(this, version);
					component.setName(keyword + '-' + Integer.toString(i));
					list.add(component);
				}
			}
			else {
				System.err.println("0x" + Integer.toHexString(type) + " UNSUPPORTED effect version " + version + ": " + offset);
			}
			
			stream.seek(offset + size);
		}
		
		long[] resourceOffsets = new long[resourceFactories.length];
		
		// -- RESOURCES -- //
		while ((type = stream.readShort()) != -1) {
			int version = stream.readShort();
			int size = stream.readInt();
			long offset = stream.getFilePointer();
			int count = stream.readInt();
			
			resourceOffsets[type] = offset + 4;
			
			List<EffectResource> list = new ArrayList<EffectResource>(count);
			resources.set(type, list);
			
			for (int i = 0; i < count; ++i) {
				EffectResource resource = resourceFactories[type].create(this, version);
				if (resource instanceof SplitterResource) {
					((SplitterResource)resource).name = resourceFactories[type].getKeyword() + '-' + Integer.toString(i);
				}
				list.add(resource);
			}
			
			stream.seek(offset + size);
		}
		
		// -- VISUAL EFFECTS -- //
		int version = stream.readShort();
		int count = stream.readInt();
		componentOffsets[VisualEffect.TYPE_CODE] = stream.getFilePointer();
		
		List<EffectComponent> effectsList = new ArrayList<EffectComponent>(count);
		components.set(VisualEffect.TYPE_CODE, effectsList);
		for (int i = 0; i < count; ++i) {
			EffectComponent component = factories[VisualEffect.TYPE_CODE].create(this, version);
			effectsList.add(component);
		}
		
		// Now read all the elements
		for (int i = 1; i < componentOffsets.length; ++i) {
			List<EffectComponent> list = components.get(i);
			if (list != null) {
				
				stream.seek(componentOffsets[i]);
				for (EffectComponent obj : list) {
					fileOffsets.put(obj, stream.getFilePointer());
					obj.read(stream);
				}
			}
		}
		for (int i = 0; i < resourceOffsets.length; ++i) {
			List<EffectResource> list = resources.get(i);
			if (list != null) {
				stream.seek(resourceOffsets[i]);
				for (EffectResource obj : list) {
					fileOffsets.put(obj, stream.getFilePointer());
					obj.read(stream);
				}
			}
		}
		
		// Visual effects must be read last, because after them come the exports
		stream.seek(componentOffsets[0]);
		for (EffectComponent obj : effectsList) {
			fileOffsets.put(obj, stream.getFilePointer());
			obj.read(stream);
		}
		
		// -- EXPORTS -- //
		Map<Integer, String> futureExportedImports = new HashMap<Integer, String>(); 
		
		int effectIndex;
		HashManager hasher = HashManager.get();
		while ((effectIndex = stream.readInt()) != -1) {
			stream.skip(4);  // groupID, never used
			String name = hasher.getFileName(stream.readInt());
			
			if ((effectIndex & ImportEffect.MASK) == ImportEffect.MASK) {
				// It is exporting an imported effect, but we can add it yet cause we haven't read the imports
				futureExportedImports.put(effectIndex & ~ImportEffect.MASK, name);
			} else {
				effectsList.get(effectIndex).setName(name);
				exports.put(name, (VisualEffect) effectsList.get(effectIndex));
			}
		}
		
		// Set the name to those that weren't exported
		for (int i = 0; i < count; ++i) {
			EffectComponent effect = effectsList.get(i);
			if (effect.getName() == null) {
				effect.setName(VisualEffect.KEYWORD + '-' + i);
			}
		}
		
		stream.readInt();  // String assignments, not used?
		
		// -- IMPORTS -- //
		int importCount = stream.readInt();
		for (int i = 0; i < importCount; ++i) {
			stream.skip(4);  // groupID, never used
			String name = hasher.getFileName(stream.readInt());
			
			ImportEffect effect;
			// The import effect might have already been created
			if (i < imports.size()) {
				effect = imports.get(i);
			} else {
				effect = new ImportEffect(this);
				imports.add(effect);
			}
			
			effect.setName(name);
		}
		
		for (Map.Entry<Integer, String> entry : futureExportedImports.entrySet()) {
			exportedImports.put(entry.getValue(), imports.get(entry.getKey()));
		}
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeShort(version);
		
		// 1. Normal components
		// 2. Resources
		// 3. Effects
		// 4. Exports
		// 5. Imports
		
		// -- NORMAL COMPONENTS -- //
		for (int i = 0; i < components.size(); i++) {
			if (i == VisualEffect.TYPE_CODE) {
				continue;
			}
			
			List<EffectComponent> list = components.get(i);
			if (list == null || list.isEmpty()) {
				continue;
			}
			
			// We use the maximum version
			stream.writeShort(factories[i].getTypeCode());
			stream.writeShort(factories[i].getMaxVersion());
			
			// Here goes the size of the section, but we don't have it yet
			long sizeOffset = stream.getFilePointer();
			stream.writeInt(0);
			
			stream.writeInt(list.size());
			
			for (EffectComponent component : list) {
				component.write(stream);
			}
			
			// Store the current position (so we can return to it), go back and write the size
			long finalOffset = stream.getFilePointer();
			stream.seek(sizeOffset);
			stream.writeInt((int) (finalOffset - sizeOffset - 4));
			stream.seek(finalOffset);
		}
		stream.writeShort(-1);
		
		
		// -- RESOURCES -- //
		for (int i = 0; i < resources.size(); i++) {
			List<EffectResource> list = resources.get(i);
			if (list == null || list.isEmpty()) {
				continue;
			}
			
			stream.writeShort(i);  // Type
			stream.writeShort(resourceFactories[i].getMaxVersion());  // Version, we use the maximum one
			
			// Here goes the size of the section, but we don't have it yet
			long sizeOffset = stream.getFilePointer();
			stream.writeInt(0);
			
			stream.writeInt(list.size());
			
			for (EffectResource resource : list) {
				resource.write(stream);
			}
			
			// Store the current position (so we can return to it), go back and write the size
			long finalOffset = stream.getFilePointer();
			stream.seek(sizeOffset);
			stream.writeInt((int) (finalOffset - sizeOffset - 4));
			stream.seek(finalOffset);
			
		}
		stream.writeShort(-1);
		
		
		// -- EFFECTS -- //
		List<EffectComponent> effects = components.get(VisualEffect.TYPE_CODE);
		stream.writeShort(1);
		stream.writeInt(effects == null ? 0 : effects.size());
		
		if (effects != null && !effects.isEmpty()) {
			for (EffectComponent effect : effects) {
				effect.write(stream);
			}
		}
		
		
		// -- EXPORTS -- //
		HashManager hasher = HashManager.get();
		for (Map.Entry<String, VisualEffect> entry : exports.entrySet()) {
			stream.writeInt(findIndex(effects, entry.getValue()));
			stream.writeInt(0);
			stream.writeInt(hasher.getFileHash(entry.getKey()));
		}
		for (Map.Entry<String, ImportEffect> entry : exportedImports.entrySet()) {
			stream.writeInt(getIndex(VisualEffect.TYPE_CODE, entry.getValue()));
			stream.writeInt(0);
			stream.writeInt(hasher.getFileHash(entry.getKey()));
		}
		stream.writeInt(-1);
		stream.writeInt(-1);  // String assignments, not used?

		stream.writeInt(imports.size());
		for (ImportEffect eff : imports) {
			stream.writeInt(0);
			stream.writeInt(hasher.getFileHash(eff.getName()));
		}
	}
	
	public void toArgScript(File outputFolder) throws IOException {
		if (!outputFolder.exists()) outputFolder.mkdir();
		
		if (!exportedImports.isEmpty()) {
			ArgScriptWriter writer = new ArgScriptWriter();
			
			for (ImportEffect effect : exportedImports.values()) {
				writer.command(ImportEffect.KEYWORD).arguments(effect.getName());
			}
			writer.blankLine();
			for (Map.Entry<String, ImportEffect> entry : exportedImports.entrySet()) {
				writer.command("export").arguments(entry.getValue().getName(), entry.getKey());
			}
			
			writer.write(new File(outputFolder, "exported_imports.pfx"));
		}
		
		boolean[][] writtenEffects = new boolean[factories.length][];
		boolean[][] writtenResources = new boolean[resourceFactories.length][];
		
		for (int i = 0; i < writtenEffects.length; ++i) {
			List<EffectComponent> list = components.get(i);
			if (list != null) writtenEffects[i] = new boolean[list.size()];
		}
		
		for (int i = 0; i < writtenResources.length; ++i) {
			List<EffectResource> list = resources.get(i);
			if (list != null) writtenResources[i] = new boolean[list.size()];
		}
		
		Set<VisualEffect> writtenExporteds = new HashSet<VisualEffect>();
		
		for (Map.Entry<String, VisualEffect> export : exports.entrySet()) {
			if (writtenExporteds.contains(export.getValue())) {
				// Just add the renamed export at the end of the file
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFolder, export.getValue().getName() + ".pfx"), true))) {
					writer.newLine();
					if (!export.getKey().equals(export.getValue().getName())) {
						writer.write("export " + export.getValue().getName() + " " + export.getKey());
					}
					else {
						writer.write("export " + export.getKey());
					}
				}
			}
			else {
				ArgScriptWriter writer = new ArgScriptWriter();
				Set<EffectFileElement> writtenElements = new HashSet<EffectFileElement>();
				
				writeEffectsPfxRecursive(export.getValue(), writer, writtenElements, writtenEffects, writtenResources);
			
				writer.command("export").arguments(export.getValue().getName());
				if (!export.getKey().equals(export.getValue().getName())) {
					writer.arguments(export.getKey());
				}
				writtenExporteds.add(export.getValue());
				
				writer.write(new File(outputFolder, export.getValue().getName() + ".pfx"));
			}
		}
		
		// Write unused effects
		// First try to write the missing visual effects, then the rest
		// do them in reverse order, because usually effects use other effects with lower indices 
		if (writtenEffects[VisualEffect.TYPE_CODE] != null) {
			List<EffectComponent> list = components.get(VisualEffect.TYPE_CODE);
			for (int i = 0; i < writtenEffects[VisualEffect.TYPE_CODE].length; ++i) {
				if (!writtenEffects[VisualEffect.TYPE_CODE][i]) {
					
					ArgScriptWriter writer = new ArgScriptWriter();
					Set<EffectFileElement> writtenElements = new HashSet<EffectFileElement>();
					
					writeEffectsPfxRecursive(list.get(i), writer, writtenElements, writtenEffects, writtenResources);
					
					writer.write(new File(outputFolder, list.get(i).getName() + ".pfx"));
				}
			}
		}
		
		for (int j = 0; j < writtenEffects.length; ++j) {
			if (writtenEffects[j] != null) {
				List<EffectComponent> list = components.get(j);
				for (int i = 0; i < writtenEffects[j].length; ++i) {
					if (!writtenEffects[j][i]) {
						
						ArgScriptWriter writer = new ArgScriptWriter();
						Set<EffectFileElement> writtenElements = new HashSet<EffectFileElement>();
						
						writeEffectsPfxRecursive(list.get(i), writer, writtenElements, writtenEffects, writtenResources);
						
						writer.write(new File(outputFolder, list.get(i).getName() + ".pfx"));
					}
				}
			}
		}
		
		for (int j = 0; j < writtenResources.length; ++j) {
			if (writtenResources[j] != null) {
				List<EffectResource> list = resources.get(j);
				for (int i = 0; i < writtenResources[j].length; ++i) {
					if (!writtenResources[j][i]) {
						
						ArgScriptWriter writer = new ArgScriptWriter();
						Set<EffectFileElement> writtenElements = new HashSet<EffectFileElement>();
						
						writeEffectsPfxRecursive(list.get(i), writer, writtenElements, writtenEffects, writtenResources);
						
						writer.write(new File(outputFolder, list.get(i).getName() + ".pfx"));
					}
				}
			}
		}
	}
	
	private void writeEffectsPfxRecursive(EffectFileElement element, ArgScriptWriter writer, 
			Set<EffectFileElement> writtenElements, boolean[][] writtenEffects, boolean[][] writtenResources) throws IOException {
		
		if (element.getFactory() == null) {
			// imports
			element.toArgScript(writer);
			writer.blankLine();
		} else {
			List<EffectFileElement> effects = element.getUsedElements();
			
			if (effects != null) {
				for (EffectFileElement effect : effects) {
					if (effect != null && !writtenElements.contains(effect)) {
						writeEffectsPfxRecursive(effect, writer, writtenElements, writtenEffects, writtenResources);
					}
				}
			}
			
			if (element.isEffectComponent()) {
				if (!((EffectComponent) element).getFactory().onlySupportsInline()) {
					if (OUTPUT_FILE_OFFSETS) {
						writer.tabulatedText("# File offset: 0x" + Long.toHexString(fileOffsets.get(element)), true);
					}
					element.toArgScript(writer);
					writer.blankLine();
				}
				
				// Mark the effect as written
				int typeCode = element.getFactory().getTypeCode();
				List<EffectComponent> list = components.get(typeCode);
				int size = list.size();
				for (int i = 0; i < size; ++i) {
					if (list.get(i) == element) {
						writtenEffects[typeCode][i] = true;
						break;
					}
				}
			} else {
				if (OUTPUT_FILE_OFFSETS) {
					writer.tabulatedText("# File offset: 0x" + Long.toHexString(fileOffsets.get(element)), true);
				}
				element.toArgScript(writer);
				writer.blankLine();
				
				// Mark the effect as written
				int typeCode = element.getFactory().getTypeCode();
				List<EffectResource> list = resources.get(typeCode);
				int size = list.size();
				for (int i = 0; i < size; ++i) {
					if (list.get(i) == element) {
						writtenResources[typeCode][i] = true;
						break;
					}
				}
			}
			
			writtenElements.add(element);
		}
	}
	
	public static void copyArray(float[] dest, float[] source) {
		for (int i = 0; i < dest.length; ++i) dest[i] = source[i];
	}
	
	public static void copyArray(int[] dest, int[] source) {
		for (int i = 0; i < dest.length; ++i) dest[i] = source[i];
	}
	
	public static <T> void copyArray(T[] dest, T[] source) {
		for (int i = 0; i < dest.length; ++i) dest[i] = source[i];
	}
}
