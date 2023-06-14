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

import java.io.IOException;

import sporemodder.file.filestructures.Stream;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureCondition;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.StructureFieldEndian;
import sporemodder.file.filestructures.metadata.StructureMetadata;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.util.ColorRGB;
import sporemodder.view.editors.PfxEditor;

@Structure(StructureEndian.BIG_ENDIAN)
public class SkinpaintSettingsEffect extends EffectComponent {

	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<SkinpaintSettingsEffect> STRUCTURE_METADATA = StructureMetadata.generate(SkinpaintSettingsEffect.class);
	
	public static final String KEYWORD = "SPSkinPaintSettings";
	public static final String KEYWORD_CLEAR = "SPSkinPaintClear";
	public static final int TYPE_CODE = 0x0022;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	
	public static final int FLAG_DIFFUSE = 1;
	public static final int FLAG_SPEC = 2;
	public static final int FLAG_BUMP = 4;
	public static final int FLAG_SPECEXP = 8;
	public static final int FLAG_SPECBUMP = 0xE;
	public static final int FLAG_PARTBUMPSCALE = 0x10;
	public static final int FLAG_PARTSPECSCALE = 0x20;
	public static final int FLAG_HAIR = 0x40;
	public static final int FLAG_HAIRTEXTURE = 0x80;
	public static final int FLAG_HAIRPRINTGEOM = 0x100;
	public static final int FLAG_GLOSS = 0x200;
	public static final int FLAG_PHONG = 0x400;
	
	public int diffuseColorIndex;  // 0x08
	public final ColorRGB diffuseColor = ColorRGB.white();  // 0x0C
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] specBump = new float[3];  // 0x18  // spec, specExp, bump
	@StructureCondition("isGlossSupported") public float gloss;  // 0x24  // only in version >= 2
	@StructureCondition("isPhongSupported") public float phong;  // 0x28  // only in version >= 3
	public float partBumpScale;  // 0x2C
	public float partSpecScale;  // 0x30
	//TODO all these are related with 'hair'
	public float field_34;  // 0x34  // an angle in radians
	public float field_38;  // 0x38
	public float field_3C;  // 0x3C
	public float field_40;  // 0x40
	public float field_44;  // 0x44
	public float field_48;  // 0x48
	public float field_4C;  // 0x4C
	public float field_50;  // 0x50
	public boolean field_54;  // 0x54
	public final ResourceID hairTexture = new ResourceID();  // 0x58
	public final ResourceID hairPrintGeom = new ResourceID();  // 0x60
	public int flags; // 0x68
	
	public SkinpaintSettingsEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		SkinpaintSettingsEffect other = (SkinpaintSettingsEffect) _effect;
		
		diffuseColorIndex = other.diffuseColorIndex;
		diffuseColor.copy(other.diffuseColor);
		specBump[0] = other.specBump[0];
		specBump[1] = other.specBump[1];
		specBump[2] = other.specBump[2];
		gloss = other.gloss;
		phong = other.phong;
		partBumpScale = other.partBumpScale;
		partSpecScale = other.partSpecScale;
		field_34 = other.field_34;
		field_38 = other.field_38;
		field_3C = other.field_3C;
		field_40 = other.field_40;
		field_44 = other.field_44;
		field_48 = other.field_48;
		field_4C = other.field_4C;
		field_50 = other.field_50;
		field_54 = other.field_54;
		hairTexture.copy(other.hairTexture);
		hairPrintGeom .copy(other.hairPrintGeom);
		flags = other.flags;
	}
	
	boolean isGlossSupported(String fieldName, Stream stream) {
		return version >= 2;
	}
	
	boolean isPhongSupported(String fieldName, Stream stream) {
		return version >= 3;
	}
	
	// We add it just to warn the user that only the anonymous version is supported
	protected static class Parser extends ArgScriptBlock<EffectUnit> {
		@Override
		public void parse(ArgScriptLine line) {
			stream.addError(line.createError(String.format("Only anonymous version of '%s' effects is supported.", KEYWORD)));
		}
	}
	
	protected static class GroupParser extends ArgScriptParser<EffectUnit> {
		
		@Override
		public void parse(ArgScriptLine line) {
			SkinpaintSettingsEffect effect = new SkinpaintSettingsEffect(data.getEffectDirectory(), FACTORY.getMaxVersion());
			ArgScriptArguments args = new ArgScriptArguments();
			Number value = null;
			boolean specCleared = false;
			boolean bumpCleared = false;
			
			if (line.getOptionArguments(args, "rgb", 1) || line.getOptionArguments(args, "diffuse", 1)) {
				effect.flags |= FLAG_DIFFUSE;
				
				switch (args.get(0)) {
				case "color1":
					effect.diffuseColorIndex = 0;
					break;
				case "color2":
					effect.diffuseColorIndex = 1;
					break;
				case "color3":
					effect.diffuseColorIndex = 2;
					break;
				default:
					effect.diffuseColorIndex = -1;
					stream.parseColorRGB(args, 0, effect.diffuseColor);
				}
				
			}
			
			if (line.getOptionArguments(args, "specBump", 1) && stream.parseVector3(args, 0, effect.specBump)) {
				effect.flags |= FLAG_SPECBUMP;
				effect.specBump[1] = Math.min(Math.max(effect.specBump[1] * 255, 1.0f), 60.0f) / 60.0f;
				specCleared = true;
				bumpCleared = true;
			}
			
			if (line.getOptionArguments(args, "spec", 1) && (value = stream.parseFloat(args, 0)) != null) {
				effect.flags |= FLAG_SPEC;
				effect.specBump[0] = value.floatValue();
				effect.specBump[0] = Math.min(Math.max(effect.specBump[0], 0), 1);
				specCleared = true;
			}
			
			if (line.getOptionArguments(args, "bump", 1) && (value = stream.parseFloat(args, 0)) != null) {
				effect.flags |= FLAG_BUMP;
				effect.specBump[2] = value.floatValue();
				effect.specBump[2] = Math.min(Math.max(effect.specBump[2], 0), 1);
				bumpCleared = true;
			}
			
			if ((line.getOptionArguments(args, "specExp", 1) || line.getOptionArguments(args, "exponent", 1)) && (value = stream.parseFloat(args, 0)) != null) {
				effect.flags |= FLAG_SPECEXP;
				effect.specBump[1] = Math.min(Math.max(value.floatValue(), 1.0f), 60.0f) / 60.0f;
			}
			
			if (line.getOptionArguments(args, "gloss", 1) && (value = stream.parseFloat(args, 0)) != null) {
				effect.flags |= FLAG_GLOSS;
				effect.gloss = value.floatValue();
			}
			
			if (line.getOptionArguments(args, "phong", 1) && (value = stream.parseFloat(args, 0)) != null) {
				effect.flags |= FLAG_PHONG;
				effect.phong = value.floatValue();
			}
			
			if (line.getOptionArguments(args, "partBumpScale", 1) && (value = stream.parseFloat(args, 0)) != null) {
				effect.flags |= FLAG_PARTBUMPSCALE;
				effect.partBumpScale = value.floatValue();
			}
			
			if (line.getOptionArguments(args, "partSpecScale", 1) && (value = stream.parseFloat(args, 0)) != null) {
				effect.flags |= FLAG_PARTSPECSCALE;
				effect.partSpecScale = value.floatValue();
			}
			
			if (line.getOptionArguments(args, "hair", 9) && (value = stream.parseFloat(args, 0)) != null) {
				effect.flags |= FLAG_HAIR;

				float[] values = new float[8];
				for (int i = 0; i < 8; i++) {
					if ((value = stream.parseFloat(args, i)) != null) {
						values[i] = value.floatValue();
					}
				}
				
				effect.field_38 = values[0];
				effect.field_34 = (float) Math.toRadians(values[1]);
				effect.field_3C = values[2];
				effect.field_40 = values[3];
				effect.field_44 = values[4];
				effect.field_48 = values[5];
				effect.field_4C = values[6];
				effect.field_50 = values[7];
				
				if ((value = stream.parseFloat(args, 8)) != null) {
					effect.field_54 = value.intValue() == 0 ? false : true;
				}
			}
			
			if (line.getOptionArguments(args, "hairTexture", 1)) {
				effect.flags |= FLAG_HAIRTEXTURE;
				String[] words = new String[2];
				effect.hairTexture.parse(args, 0, words);
				args.addHyperlink(PfxEditor.HYPERLINK_TEXTURE, words, 0);
			}
			
			if (line.getOptionArguments(args, "hairPrintGeom", 1)) {
				effect.flags |= FLAG_HAIRPRINTGEOM;
				String[] words = new String[2];
				effect.hairPrintGeom.parse(args, 0, words);
				// No idea of the type
				args.addHyperlink(PfxEditor.HYPERLINK_TEXTURE, words, 0);
			}
			
			// It must not have any arguments
			line.getArguments(args, 0);
			
			if (effect.flags == 0) {
				stream.addError(line.createError(String.format("Need at least one option.", KEYWORD)));
			}
			
			if (bumpCleared != specCleared) {
				stream.addError(line.createError(String.format("Bump and spec channels must be cleared simultaneously.", KEYWORD)));
			}
			
			// Add it to the effect
			VisualEffectBlock block = new VisualEffectBlock(data.getEffectDirectory());
			block.blockType = TYPE_CODE;
			block.parse(stream, line, SoundEffect.class);
			
			block.component = effect;
			data.addComponent(effect.toString(), effect);
			data.getCurrentEffect().blocks.add(block);
		}
	}
	
	public static class Factory implements EffectComponentFactory {
		@Override public Class<? extends EffectComponent> getComponentClass() {
			return SkinpaintSettingsEffect.class;
		}
		@Override public String getKeyword() {
			return KEYWORD;
		}
		@Override
		public int getTypeCode() {
			return TYPE_CODE;
		}

		@Override
		public int getMinVersion() {
			return 1;
		}

		@Override
		public int getMaxVersion() {
			return 3;
		}
		
		@Override
		public void addEffectParser(ArgScriptStream<EffectUnit> stream) {
			stream.addParser(KEYWORD, new Parser());
			stream.addParser(KEYWORD_CLEAR, new Parser());
		}
		
		@Override
		public void addGroupEffectParser(ArgScriptBlock<EffectUnit> effectBlock) {
			effectBlock.addParser(KEYWORD, new GroupParser());
			effectBlock.addParser(KEYWORD_CLEAR, new GroupParser());
		}

		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new SkinpaintSettingsEffect(effectDirectory, version);
		}

		@Override
		public boolean onlySupportsInline() {
			return true;
		}
	}

	@Override
	public EffectComponentFactory getFactory() {
		return FACTORY;
	}
	
	@Override
	public void read(StreamReader stream) throws IOException {
		STRUCTURE_METADATA.read(this, stream);
	}

	@Override
	public void write(StreamWriter stream) throws IOException {
		STRUCTURE_METADATA.write(this, stream);
	}

	@Override
	public void toArgScript(ArgScriptWriter writer) {
		writer.command(KEYWORD);
		
		if ((flags & FLAG_DIFFUSE) == FLAG_DIFFUSE) {
			writer.option("diffuse");
			if (diffuseColorIndex != -1) writer.arguments("color" + Integer.toString(diffuseColorIndex + 1));
			else writer.color(diffuseColor);
		}
		if ((flags & FLAG_SPECBUMP) == FLAG_SPECBUMP) {
			writer.option("specBump").vector(specBump[0], specBump[1] * 60.0f, specBump[2]);
		} else {
			if ((flags & FLAG_SPEC) == FLAG_SPEC) writer.option("spec").floats(specBump[0]);
			if ((flags & FLAG_BUMP) == FLAG_BUMP) writer.option("bump").floats(specBump[2]);
			if ((flags & FLAG_SPECEXP) == FLAG_SPECEXP) writer.option("exponent").floats(specBump[1] * 60.0f);
		}
		if ((flags & FLAG_GLOSS) == FLAG_GLOSS) writer.option("gloss").floats(gloss);
		if ((flags & FLAG_PHONG) == FLAG_PHONG) writer.option("phong").floats(phong);
		if ((flags & FLAG_PARTBUMPSCALE) == FLAG_PARTBUMPSCALE) writer.option("partBumpScale").floats(partBumpScale);
		if ((flags & FLAG_PARTSPECSCALE) == FLAG_PARTSPECSCALE) writer.option("partSpecScale").floats(partSpecScale);
		if ((flags & FLAG_HAIR) == FLAG_HAIR) {
			writer.option("hair").floats(field_38, (float)Math.toDegrees(field_34), field_3C,
					field_40, field_44, field_48, field_4C, field_50).ints(field_54 ? 1 : 0);
		}
		if ((flags & FLAG_HAIRTEXTURE) == FLAG_HAIRTEXTURE) writer.option("hairTexture").arguments(hairTexture);
		if ((flags & FLAG_HAIRPRINTGEOM) == FLAG_HAIRPRINTGEOM) writer.option("hairPrintGeom").arguments(hairPrintGeom);
	}
}
