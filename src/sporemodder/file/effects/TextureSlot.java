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

import emord.filestructures.Structure;
import emord.filestructures.StructureEndian;
import emord.filestructures.metadata.StructureMetadata;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.view.editors.PfxEditor;

@Structure(StructureEndian.BIG_ENDIAN)
public class TextureSlot {
	
	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<TextureSlot> STRUCTURE_METADATA = StructureMetadata.generate(TextureSlot.class);
	
	public static final int DRAWMODE_NONE = 0x0F;
	
	public static final ArgScriptEnum ENUM_DRAWMODE = new ArgScriptEnum();
	static {
		ENUM_DRAWMODE.add(0x00, "decal");
		ENUM_DRAWMODE.add(0x01, "decalInvertDepth");
		ENUM_DRAWMODE.add(0x02, "decalIgnoreDepth");
		ENUM_DRAWMODE.add(0x03, "depthDecal");
		ENUM_DRAWMODE.add(0x03, "decalDepth");
		ENUM_DRAWMODE.add(0x04, "additive");
		ENUM_DRAWMODE.add(0x05, "additiveInvertDepth");
		ENUM_DRAWMODE.add(0x06, "additiveIgnoreDepth");
		ENUM_DRAWMODE.add(0x07, "modulate");
		ENUM_DRAWMODE.add(0x08, "normalMap");
		ENUM_DRAWMODE.add(0x09, "depthNormalMap");
		ENUM_DRAWMODE.add(0x09, "normalMapDepth");
		ENUM_DRAWMODE.add(0x0A, "alphaTestDissolve");
		ENUM_DRAWMODE.add(0x0B, "user1");
		ENUM_DRAWMODE.add(0x0C, "user2");
		ENUM_DRAWMODE.add(0x0D, "user3");
		ENUM_DRAWMODE.add(0x0E, "user4");
		ENUM_DRAWMODE.add(DRAWMODE_NONE, null);
	}
	
	public static final int DRAWFLAG_LIGHT = 0x1;
	public static final int DRAWFLAG_NO_FOG= 0x2;
	public static final int DRAWFLAG_SHADOW = 0x8;
	public static final int DRAWFLAG_NO_SHADOW = 0xF7;
	public static final int DRAWFLAG_NO_CULL = 0x10;
	public static final int DRAWFLAG_USER1 = 0x20;
	public static final int DRAWFLAG_USER2 = 0x40;
	public static final int DRAWFLAG_USER3 = 0x80;

	public final ResourceID resource = new ResourceID();
	public byte format;
	public byte drawMode;
	public byte drawFlags;
	public byte buffer;
	public short layer;
	public float sortOffset;
	public final ResourceID resource2 = new ResourceID();
	
	public void copy(TextureSlot texture) {
		resource.copy(texture.resource);
		format = texture.format;
		drawMode = texture.drawMode;
		drawFlags = texture.drawFlags;
		buffer = texture.buffer;
		layer = texture.layer;
		sortOffset = texture.sortOffset;
		resource2.copy(texture.resource2);
	}
	
	public<T> void parse(ArgScriptStream<T> stream, ArgScriptLine line, String hyperlinkType) {
		ArgScriptArguments args = new ArgScriptArguments();
		Number value = null;
		
		if (line.getArguments(args, 0, 1) && args.size() == 1) {
			String[] originals = new String[2];
			resource.parse(args, 0, originals);
			line.addHyperlinkForArgument(hyperlinkType, originals, 0);
		}
		
		if (drawMode == DRAWMODE_NONE) {
			if (line.getOptionArguments(args, "texture", 1)) {
				String[] originals = new String[2];
				resource2.parse(args, 0, originals);
				line.addHyperlinkForOptionArgument(PfxEditor.HYPERLINK_TEXTURE, originals, "texture", 0);
			}
		}
		else {
			if (line.getOptionArguments(args, "draw", 1, 2)) {
				drawMode = (byte) ENUM_DRAWMODE.get(args, 0);
				
				if (args.size() == 2) {
					String[] originals = new String[2];
					resource2.parse(args, 1, originals);
					// probably material?
					line.addHyperlinkForOptionArgument(PfxEditor.HYPERLINK_MATERIAL, originals, "draw", 1);
				}
			}
		}
		
		if (line.getOptionArguments(args, "buffer", 1) && 
				(value = stream.parseByte(args, 0)) != null) {
			buffer = value.byteValue();
		}
		
		if (line.getOptionArguments(args, "layer", 1) && 
				(value = stream.parseInt(args, 0, Short.MIN_VALUE, Short.MAX_VALUE)) != null) {
			layer = value.shortValue();
		}
		
		if (line.getOptionArguments(args, "sortOffset", 1) && 
				(value = stream.parseFloat(args, 0)) != null) {
			sortOffset = value.floatValue();
		}
		
		drawFlags = 0;
		if (line.hasFlag("light")) drawFlags |= DRAWFLAG_LIGHT;
		if (line.hasFlag("noFog")) drawFlags |= DRAWFLAG_NO_FOG;
		if (line.hasFlag("shadow")) drawFlags |= DRAWFLAG_SHADOW;
		if (line.hasFlag("noShadow")) drawFlags &= DRAWFLAG_NO_SHADOW;
		if (line.hasFlag("noCull")) drawFlags |= DRAWFLAG_NO_CULL;
		if (line.hasFlag("user1")) drawFlags |= DRAWFLAG_USER1;
		if (line.hasFlag("user2")) drawFlags |= DRAWFLAG_USER2;
		if (line.hasFlag("user3")) drawFlags |= DRAWFLAG_USER3;
		
		// Apparently, these aren't used by Spore. We'll leave them here anyways
		
		if (line.getOptionArguments(args, "format", 1) && 
				(value = stream.parseByte(args, 0)) != null) {
			format = value.byteValue();
		}
	}
	
	public void toArgScript(String keyword, ArgScriptWriter writer) {
		toArgScript(keyword, writer, true, true);
	}
	
	public void toArgScript(String keyword, ArgScriptWriter writer, boolean include2nd, boolean includeShadow) {
		if (keyword != null) {
			writer.command(keyword);
			if (!resource.isDefault()) writer.arguments(resource);
		}
		
		if (drawMode == DRAWMODE_NONE && !resource2.isDefault()) {
			if (include2nd) writer.option("texture").arguments(resource2);
		} 
		else if (drawMode != DRAWMODE_NONE && (drawMode != 0 || !resource2.isDefault())) {
			writer.option("draw").arguments(ENUM_DRAWMODE.get(drawMode));
			if (!resource2.isDefault()) writer.arguments(resource2);
		}
		if (buffer != 0) writer.option("buffer").ints(buffer);
		if (layer != 0) writer.option("layer").ints(layer);
		if (sortOffset != 0) writer.option("sortOffset").floats(sortOffset);
		
		writer.flag("light", (drawFlags & DRAWFLAG_LIGHT) == DRAWFLAG_LIGHT);
		writer.flag("noFog", (drawFlags & DRAWFLAG_NO_FOG) == DRAWFLAG_NO_FOG);
		writer.flag("shadow", includeShadow && (drawFlags & DRAWFLAG_SHADOW) == DRAWFLAG_SHADOW);
		writer.flag("noCull", (drawFlags & DRAWFLAG_NO_CULL) == DRAWFLAG_NO_CULL);
		writer.flag("user1", (drawFlags & DRAWFLAG_USER1) == DRAWFLAG_USER1);
		writer.flag("user2", (drawFlags & DRAWFLAG_USER2) == DRAWFLAG_USER2);
		writer.flag("user3", (drawFlags & DRAWFLAG_USER3) == DRAWFLAG_USER3);
		
		// Apparently, these aren't used by Spore. We'll leave them here anyways
		if (format != 0) writer.option("format").arguments("0x" + Integer.toHexString(Byte.toUnsignedInt(format)));
	}
	
	public boolean isDefault() {
		return resource2.isDefault() && resource.isDefault() && format == 0 && drawMode == 0 && drawFlags == 0 && buffer == 0 && layer == 0 && sortOffset == 0;
	}
}
