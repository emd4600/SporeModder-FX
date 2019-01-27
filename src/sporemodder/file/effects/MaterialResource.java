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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.file.DocumentError;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.view.editors.PfxEditor;

public class MaterialResource extends EffectResource {
	
	public static final String KEYWORD = "material";
	public static final int TYPE_CODE = 0x01;
	
	public static final EffectResourceFactory FACTORY = new Factory();
	
	private static final int X = 0;
	private static final int Y = 1;
	private static final int Z = 2;
	private static final int W = 3;
	
	private static final HashMap<String, Integer> MATERIAL_PARAMS = new HashMap<String, Integer>();
	static {
		MATERIAL_PARAMS.put("lightIndex", MaterialProperty.getMaterialParam(0, X));
		MATERIAL_PARAMS.put("depthOffset", MaterialProperty.getMaterialParam(0, Y));
		MATERIAL_PARAMS.put("aspectRatio", MaterialProperty.getMaterialParam(0, W));
		MATERIAL_PARAMS.put("shDiffuseAtten", MaterialProperty.getMaterialParam(0, X));
		MATERIAL_PARAMS.put("shSpecularAtten", MaterialProperty.getMaterialParam(0, Z));
		MATERIAL_PARAMS.put("shSpecularExponent", MaterialProperty.getMaterialParam(0, W));
		// register 1
		MATERIAL_PARAMS.put("alphaDistances", MaterialProperty.getMaterialParam(1, X));
		// register 2
		MATERIAL_PARAMS.put("alphaValues", MaterialProperty.getMaterialParam(2, X));
		// register 3
		MATERIAL_PARAMS.put("facetAttenSet", MaterialProperty.getMaterialParam(3, X));
		MATERIAL_PARAMS.put("facingNormalJitter", MaterialProperty.getMaterialParam(3, W));
		// register 4
		MATERIAL_PARAMS.put("vertexAttenSet", MaterialProperty.getMaterialParam(4, X));
		MATERIAL_PARAMS.put("vertexNormalJitter", MaterialProperty.getMaterialParam(4, W));
		// register 5
		MATERIAL_PARAMS.put("scaleDistances", MaterialProperty.getMaterialParam(5, X));
		// register 6
		MATERIAL_PARAMS.put("scaleValues", MaterialProperty.getMaterialParam(6, X));
		// register 7
		MATERIAL_PARAMS.put("reflectancePower", MaterialProperty.getMaterialParam(7, X));
		MATERIAL_PARAMS.put("alphaFacingAttenSet", MaterialProperty.getMaterialParam(7, Y));
		// register 8
		MATERIAL_PARAMS.put("texTiles", MaterialProperty.getMaterialParam(8, X));
		MATERIAL_PARAMS.put("texTileSpeed", MaterialProperty.getMaterialParam(8, Z));
		MATERIAL_PARAMS.put("texTileOffset", MaterialProperty.getMaterialParam(8, W));
		// register 9
		MATERIAL_PARAMS.put("texTileLifespanFrames", MaterialProperty.getMaterialParam(9, X));
		MATERIAL_PARAMS.put("texTileLifespanClipPerRow", MaterialProperty.getMaterialParam(9, Y));
		MATERIAL_PARAMS.put("texTileLifespanOffset", MaterialProperty.getMaterialParam(9, Z));
		MATERIAL_PARAMS.put("texTileBias", MaterialProperty.getMaterialParam(9, W));
		// register 10
		MATERIAL_PARAMS.put("lightDesaturation", MaterialProperty.getMaterialParam(10, X));
		MATERIAL_PARAMS.put("fogRemoval", MaterialProperty.getMaterialParam(10, Y));
		MATERIAL_PARAMS.put("shadowRemoval", MaterialProperty.getMaterialParam(10, Z));
		MATERIAL_PARAMS.put("sunColor", MaterialProperty.getMaterialParam(10, X));
		// register 12
		MATERIAL_PARAMS.put("gasGiant_baseColorTiles", MaterialProperty.getMaterialParam(12, X));
		MATERIAL_PARAMS.put("gasGiant_baseDistortTiles", MaterialProperty.getMaterialParam(12, Y));
		MATERIAL_PARAMS.put("gasGiant_cloudColorTiles", MaterialProperty.getMaterialParam(12, Z));
		MATERIAL_PARAMS.put("gasGiant_cloudDistortTiles", MaterialProperty.getMaterialParam(12, W));
		
		MATERIAL_PARAMS.put("planetaryRings_shadowDensity", MaterialProperty.getMaterialParam(12, X));
		MATERIAL_PARAMS.put("planetaryRings_planetRadius", MaterialProperty.getMaterialParam(12, Y));
		
		MATERIAL_PARAMS.put("model_uvSpeed", MaterialProperty.getMaterialParam(12, X));
		MATERIAL_PARAMS.put("model_uvScale", MaterialProperty.getMaterialParam(12, Z));
		
		MATERIAL_PARAMS.put("column_beamVariance", MaterialProperty.getMaterialParam(12, X));
		MATERIAL_PARAMS.put("column_beamCount", MaterialProperty.getMaterialParam(12, Y));
		
		MATERIAL_PARAMS.put("stretch_uTimes", MaterialProperty.getMaterialParam(12, X));
		
		MATERIAL_PARAMS.put("sunBlendDistances", MaterialProperty.getMaterialParam(12, X));
		// register 13
		MATERIAL_PARAMS.put("sizeDistances", MaterialProperty.getMaterialParam(13, X));
		MATERIAL_PARAMS.put("sizeValues", MaterialProperty.getMaterialParam(13, Z));
		
		MATERIAL_PARAMS.put("gasGiant_baseColorSpeed", MaterialProperty.getMaterialParam(13, X));
		MATERIAL_PARAMS.put("gasGiant_baseDistortSpeed", MaterialProperty.getMaterialParam(13, Y));
		MATERIAL_PARAMS.put("gasGiant_baseDistortAmt", MaterialProperty.getMaterialParam(13, Z));
		MATERIAL_PARAMS.put("gasGiant_cloudColorSpeed", MaterialProperty.getMaterialParam(13, W));
		
		MATERIAL_PARAMS.put("model_scale", MaterialProperty.getMaterialParam(13, X));
		MATERIAL_PARAMS.put("column_vPositions", MaterialProperty.getMaterialParam(13, X));
		MATERIAL_PARAMS.put("stretch_uScales", MaterialProperty.getMaterialParam(13, X));
		
		MATERIAL_PARAMS.put("sunBlendValues", MaterialProperty.getMaterialParam(13, X));
		MATERIAL_PARAMS.put("thinRing_widthDistances", MaterialProperty.getMaterialParam(13, X));
		// register 14
		MATERIAL_PARAMS.put("lumDistances", MaterialProperty.getMaterialParam(14, X));
		MATERIAL_PARAMS.put("lumValues", MaterialProperty.getMaterialParam(14, Z));
		
		MATERIAL_PARAMS.put("windTimeFreq", MaterialProperty.getMaterialParam(14, X));
		MATERIAL_PARAMS.put("windSpaceFreq", MaterialProperty.getMaterialParam(14, Y));
		MATERIAL_PARAMS.put("windAmpVerticalScalar", MaterialProperty.getMaterialParam(14, Z));
		MATERIAL_PARAMS.put("windWaveOffset", MaterialProperty.getMaterialParam(14, W));
		
		MATERIAL_PARAMS.put("gasGiant_cloudDistortSpeed", MaterialProperty.getMaterialParam(14, X));
		MATERIAL_PARAMS.put("gasGiant_cloudDistortAmt", MaterialProperty.getMaterialParam(14, Y));
		MATERIAL_PARAMS.put("gasGiant_cloudColorAtten", MaterialProperty.getMaterialParam(14, Z));
		MATERIAL_PARAMS.put("gasGiant_cloudDensity", MaterialProperty.getMaterialParam(14, W));
		
		MATERIAL_PARAMS.put("model_rgbIn", MaterialProperty.getMaterialParam(14, X));
		MATERIAL_PARAMS.put("column_vAlphas", MaterialProperty.getMaterialParam(14, X));
		MATERIAL_PARAMS.put("stretch_vTimes", MaterialProperty.getMaterialParam(14, X));
		MATERIAL_PARAMS.put("emissiveLerpDistances", MaterialProperty.getMaterialParam(14, X));
		MATERIAL_PARAMS.put("borderSpriteNudge", MaterialProperty.getMaterialParam(14, X));
		MATERIAL_PARAMS.put("thinRing_widthValues", MaterialProperty.getMaterialParam(14, X));
		// register 15
		MATERIAL_PARAMS.put("flickerFreq", MaterialProperty.getMaterialParam(15, X));
		MATERIAL_PARAMS.put("flickerLegato", MaterialProperty.getMaterialParam(15, Y));
		MATERIAL_PARAMS.put("flickerLumMin", MaterialProperty.getMaterialParam(15, Z));
		
		MATERIAL_PARAMS.put("ring0spin", MaterialProperty.getMaterialParam(15, X));
		MATERIAL_PARAMS.put("ring0alpha", MaterialProperty.getMaterialParam(15, Y));
		MATERIAL_PARAMS.put("ring1spin", MaterialProperty.getMaterialParam(15, Z));
		MATERIAL_PARAMS.put("ring1alpha", MaterialProperty.getMaterialParam(15, W));
		
		MATERIAL_PARAMS.put("windAmpDistances", MaterialProperty.getMaterialParam(15, X));
		MATERIAL_PARAMS.put("windAmpValues", MaterialProperty.getMaterialParam(15, Z));
		
		MATERIAL_PARAMS.put("borderSpriteOffscreenPct", MaterialProperty.getMaterialParam(15, X));
		MATERIAL_PARAMS.put("borderSpriteAlwaysUp", MaterialProperty.getMaterialParam(15, Y));
		MATERIAL_PARAMS.put("borderSpriteSnapRotation", MaterialProperty.getMaterialParam(15, Z));
		
		MATERIAL_PARAMS.put("rotationSpeed", MaterialProperty.getMaterialParam(15, X));
		MATERIAL_PARAMS.put("rotationVariance", MaterialProperty.getMaterialParam(15, Y));
		MATERIAL_PARAMS.put("tiltVariance", MaterialProperty.getMaterialParam(15, Z));
		
		MATERIAL_PARAMS.put("gasGiant_scatterAtten", MaterialProperty.getMaterialParam(15, X));
		
		MATERIAL_PARAMS.put("model_rgbOut", MaterialProperty.getMaterialParam(15, X));
		
		MATERIAL_PARAMS.put("column_rgb", MaterialProperty.getMaterialParam(15, X));
		MATERIAL_PARAMS.put("column_rotateSpeed", MaterialProperty.getMaterialParam(15, W));
		
		MATERIAL_PARAMS.put("stretch_vValues", MaterialProperty.getMaterialParam(15, X));
		MATERIAL_PARAMS.put("emissiveLerpValues", MaterialProperty.getMaterialParam(15, X));
		
		MATERIAL_PARAMS.put("emissiveLerp", MaterialProperty.getMaterialParam(15, X));
		
		MATERIAL_PARAMS.put("thinRing_discOpacity", MaterialProperty.getMaterialParam(15, X));
		MATERIAL_PARAMS.put("thinRing_discRampScale", MaterialProperty.getMaterialParam(15, Y));
		MATERIAL_PARAMS.put("thinRing_discRampOffset", MaterialProperty.getMaterialParam(15, Z));
		// cloudVanilla.smt/smokeVanilla.smt/effectVanilla.smt
		MATERIAL_PARAMS.put("animBlendRate", MaterialProperty.getMaterialParam(0, X));
		MATERIAL_PARAMS.put("stripLength", MaterialProperty.getMaterialParam(0, Y));
		MATERIAL_PARAMS.put("topFade", MaterialProperty.getMaterialParam(0, Z));
		// grassVanilla.smt
		MATERIAL_PARAMS.put("startFade", MaterialProperty.getMaterialParam(0, X));
		MATERIAL_PARAMS.put("endFade", MaterialProperty.getMaterialParam(0, Y));
		MATERIAL_PARAMS.put("tilt", MaterialProperty.getMaterialParam(0, Z));
		MATERIAL_PARAMS.put("startFadeIn", MaterialProperty.getMaterialParam(0, W));
		
		MATERIAL_PARAMS.put("startScale", MaterialProperty.getMaterialParam(1, X));
		MATERIAL_PARAMS.put("endScale", MaterialProperty.getMaterialParam(1, Y));
		MATERIAL_PARAMS.put("scaleFactor", MaterialProperty.getMaterialParam(1, Z));
		MATERIAL_PARAMS.put("endFadeIn", MaterialProperty.getMaterialParam(1, W));
		// effectVanilla.smt
		MATERIAL_PARAMS.put("minScaleRatio", MaterialProperty.getMaterialParam(0, Z));
		MATERIAL_PARAMS.put("maxScaleRatio", MaterialProperty.getMaterialParam(0, W));
		// effectVanilla.smt/effectPlanetRingMaterial
		MATERIAL_PARAMS.put("ring0vel", MaterialProperty.getMaterialParam(0, X));
		MATERIAL_PARAMS.put("ring1vel", MaterialProperty.getMaterialParam(0, Y));
		MATERIAL_PARAMS.put("ring0fade", MaterialProperty.getMaterialParam(0, Z));
		MATERIAL_PARAMS.put("ring1fade", MaterialProperty.getMaterialParam(0, W));
		// effectVanilla.smt/effectStarMaterial
		MATERIAL_PARAMS.put("fadeStart", MaterialProperty.getMaterialParam(1, X));
		MATERIAL_PARAMS.put("fadeEnd", MaterialProperty.getMaterialParam(1, Y));
		MATERIAL_PARAMS.put("scaleFactor", MaterialProperty.getMaterialParam(1, Z));
		MATERIAL_PARAMS.put("depthBias", MaterialProperty.getMaterialParam(1, W));
	}
	
	public static class MaterialProperty {
		public int type; //byte
		public final ResourceID name = new ResourceID();
		// registerIndex = (group - 0x10000) >> 4
		public float[] valuesF;
		public int[] valuesI;
		public boolean[] valuesB;
		public float valueF;
		public int valueI;
		public boolean valueB;
		public ResourceID valueRes;
		
		public static int getMaterialParam(int index, int offset) {
			int num = index * 4 + offset;
			return (num << 4) + 0x10000;
		}
		
		private static int getRegisterOffset(String str) {
			if (str.startsWith("x")) return X;
			else if (str.startsWith("y")) return Y;
			else if (str.startsWith("z")) return Z;
			else if (str.startsWith("w")) return W;
			return -1;
		}
		
		public <T> boolean parseName(ArgScriptStream<T> stream, ArgScriptArguments args, int index) {
			String string = args.get(index);
			
			if (string.startsWith("0x")) {
				Integer value = null;
				if ((value = stream.parseFileID(args, index)) == null) return false;
				name.setInstanceID(value.intValue());
				name.setGroupID(0);
				
				return true;
			} 
			else {
				int id = 0;
				if (MATERIAL_PARAMS.containsKey(string)) {
					id = MATERIAL_PARAMS.get(string);
				}
				else {
					String[] splits  = string.split("\\.", 2);
					if (splits.length != 2) {
						stream.addError(new DocumentError("Unrecognised property name: introduce a correct name, a hash or a register (such as 'c8.z').", 
								args.getPosition(index), args.getEndPosition(index)));
						return false;
					}
					
					int registerIndex = Integer.parseInt(splits[0].substring(1));
					int registerOffset = getRegisterOffset(splits[1]);
					
					if (registerOffset == -1) {
						stream.addError(new DocumentError("Wrong register name: expected 'x', 'y', 'z' or 'w' after register.", 
								args.getPosition(index), args.getEndPosition(index)));
						return false;
					}
					
					id = getMaterialParam(registerIndex, registerOffset);
				}
				
				name.setInstanceID(id);
				name.setGroupID(0);
				
				return true;
			}
		}
		
		private static String getRegisterOffsetString(int registerOffset, int valueCount) {
			if (registerOffset + valueCount > 4) return null;
			StringBuffer sb =  new StringBuffer(4);
			for (int i = registerOffset; i < registerOffset + valueCount; i++) {
				sb.append(getRegisterOffsetChar(i));
			}
			return sb.toString();
		}
		
		private static char getRegisterOffsetChar(int registerOffset) {
			if (registerOffset == X) return 'x';
			else if (registerOffset == Y) return 'y';
			else if (registerOffset == Z) return 'z';
			else if (registerOffset == W) return 'w';
			else return Character.forDigit(registerOffset, 10);
		}
		
		private String getNameString(int valueCount) {
			int num = (name.getInstanceID() - 0x10000) >> 4;
			int registerIndex = num / 4;
			int registerOffset = num % 4;
			String offsetString = getRegisterOffsetString(registerOffset, valueCount);
			if (offsetString == null) {
				return name.toString();
			} else {
				return "c" + registerIndex + "." + offsetString;
			}
		}
		
		public void toArgScript(ArgScriptWriter writer) {
			if (type == 0) writer.command("float").arguments(getNameString(1)).floats(valueF);
			else if (type == 1) writer.command("int").arguments(getNameString(1)).ints(valueI);
			else if (type == 2) writer.command("boolean").arguments(getNameString(1)).arguments(valueB);
			else if (type == 3) 
				writer.command("floats").arguments(getNameString(valuesF.length)).floats(valuesF);
			else if (type == 4) 
				writer.command("ints").arguments(getNameString(valuesI.length)).ints(valuesI);
			else if (type == 5) 
				writer.command("booleans").arguments(getNameString(valuesB.length)).arguments(valuesB);
			else if (type == 6)
				writer.command("texture").arguments("sampler" + name.getInstanceID(), valueRes);
		}
	}
	
	public final ResourceID shaderID = new ResourceID();
	public final List<MaterialProperty> properties = new ArrayList<MaterialProperty>();
	
	public MaterialResource(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void read(StreamReader in) throws IOException {
		resourceID.read(in);
		shaderID.read(in);
		
		int count = in.readInt();
		for (int i = 0; i < count; i++) {
			MaterialProperty prop = new MaterialProperty();
			prop.name.read(in);
			prop.type = in.readByte();
			
			if (prop.type == 0) {
				prop.valueF = in.readFloat();
				
			} else if (prop.type == 1) {
				prop.valueI = in.readInt();
				
			} else if (prop.type == 2) {
				prop.valueB = in.readByte() == 0 ? false : true;
				
			} else if (prop.type == 3) {
				prop.valuesF = new float[in.readShort()];
				in.readFloats(prop.valuesF);
				
			} else if (prop.type == 4) {
				prop.valuesI = new int[in.readShort()];
				in.readInts(prop.valuesI);
				
			} else if (prop.type == 5) {
				prop.valuesB = new boolean[in.readShort()];
				for (int f = 0; f < prop.valuesB.length; f++) {
					prop.valuesB[f] = in.readByte() == 0 ? false : true;
				}
				
			} else if (prop.type == 6) {
				prop.valueRes = new ResourceID();
				prop.valueRes.read(in);
			}
			properties.add(prop);
		}
	}

	@Override public void write(StreamWriter out) throws IOException {
		resourceID.write(out);
		shaderID.write(out);
		
		out.writeInt(properties.size());
		for (MaterialProperty prop : properties) {
			prop.name.write(out);
			out.writeByte(prop.type);
			
			if (prop.type == 0) {
				out.writeFloat(prop.valueF);
			} else if (prop.type == 1) {
				out.writeInt(prop.valueI);
			} else if (prop.type == 2) {
				out.writeBoolean(prop.valueB);
			} else if (prop.type == 3) {
				out.writeShort(prop.valuesF.length);
				out.writeFloats(prop.valuesF);
			} else if (prop.type == 4) {
				out.writeShort(prop.valuesI.length);
				out.writeInts(prop.valuesI);
			} else if (prop.type == 5) {
				out.writeShort(prop.valuesB.length);
				out.writeBooleans(prop.valuesB);
			} else if (prop.type == 6) {
				prop.valueRes.write(out);
			}
		}
	}
	
	
	protected static class Parser extends ArgScriptBlock<EffectUnit> {
		protected MaterialResource resource;
		protected String name;
		
		@Override
		public void parse(ArgScriptLine line) {
			resource = new MaterialResource(data.getEffectDirectory(), FACTORY.getMaxVersion());
			
			ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 1)) {
				name = args.get(0);
				resource.resourceID.parse(args, 0);
			}
			
			data.setPosition(resource, stream.getLinePositions().get(stream.getCurrentLine()));
			
			stream.startBlock(this);
		}
		
		@Override
		public void onBlockEnd() {
			data.addResource(name, resource);
		}
		
		@Override
		public void setData(ArgScriptStream<EffectUnit> stream, EffectUnit data) {
			super.setData(stream, data);
			
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("shader", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					resource.shaderID.parse(args, 0);
				}
			}));
			
			this.addParser("float", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				MaterialProperty property = new MaterialProperty();
				
				if (line.getArguments(args, 2) && property.parseName(stream, args, 0) &&
						(value = stream.parseFloat(args, 1)) != null) {
					
					property.type = 0;
					property.valueF = value.floatValue();
					resource.properties.add(property);
				}
			}));
			
			this.addParser("int", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				MaterialProperty property = new MaterialProperty();
				
				if (line.getArguments(args, 2) && property.parseName(stream, args, 0) &&
						(value = stream.parseInt(args, 1)) != null) {
					
					property.type = 1;
					property.valueI = value.intValue();
					resource.properties.add(property);
				}
			}));
			
			this.addParser("boolean", ArgScriptParser.create((parser, line) -> {
				Boolean value = null;
				MaterialProperty property = new MaterialProperty();
				
				if (line.getArguments(args, 2) && property.parseName(stream, args, 0) &&
						(value = stream.parseBoolean(args, 1)) != null) {
					
					property.type = 2;
					property.valueB = value;
					resource.properties.add(property);
				}
			}));
			
			this.addParser("floats", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				MaterialProperty property = new MaterialProperty();
				
				if (line.getArguments(args, 2, Integer.MAX_VALUE) && property.parseName(stream, args, 0)) {
					
					property.type = 3;
					property.valuesF = new float[args.size() - 1];
					for (int i = 0; i < property.valuesF.length; i++) {
						if ((value = stream.parseFloat(args, 1 + i)) == null) break;
						property.valuesF[i] = value.floatValue();
					}
					resource.properties.add(property);
				}
			}));
			
			this.addParser("ints", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				MaterialProperty property = new MaterialProperty();
				
				if (line.getArguments(args, 2, Integer.MAX_VALUE) && property.parseName(stream, args, 0)) {
					
					property.type = 4;
					property.valuesI = new int[args.size() - 1];
					for (int i = 0; i < property.valuesI.length; i++) {
						if ((value = stream.parseInt(args, 1 + i)) == null) break;
						property.valuesI[i] = value.intValue();
					}
					resource.properties.add(property);
				}
			}));
			
			this.addParser("booleans", ArgScriptParser.create((parser, line) -> {
				Boolean value = null;
				MaterialProperty property = new MaterialProperty();
				
				if (line.getArguments(args, 2, Integer.MAX_VALUE) && property.parseName(stream, args, 0)) {
					
					property.type = 5;
					property.valuesB = new boolean[args.size() - 1];
					for (int i = 0; i < property.valuesB.length; i++) {
						if ((value = stream.parseBoolean(args, 1 + i)) == null) break;
						property.valuesB[i] = value;
					}
					resource.properties.add(property);
				}
			}));
			
			this.addParser("texture", ArgScriptParser.create((parser, line) -> {
				MaterialProperty property = new MaterialProperty();
				
				if (line.getArguments(args, 2)) {
					
					property.type = 6;
					property.valueRes = new ResourceID(0, 0);
					
					String[] words = new String[2];
					property.valueRes.parse(args, 1, words);
					line.addHyperlinkForArgument(PfxEditor.HYPERLINK_TEXTURE, words, 1);
					
					property.name.setGroupID(0);
					if (args.get(0).equals("diffuse")) {
						property.name.setInstanceID(0);
					}
					// I just invented this one
					else if (args.get(0).equals("normal")) {
						property.name.setInstanceID(1);
					}
					else if (args.get(0).startsWith("sampler")) {
						try {
							//property.name.setInstanceID(Integer.parseInt(args.get(0).substring(7)) - 2);
							property.name.setInstanceID(Integer.parseInt(args.get(0).substring(7)) - 0);
						}
						catch (Exception e) {
							stream.addError(new DocumentError(e.getLocalizedMessage(), args.getRealPosition(args.getPosition(1) + 7), args.getEndPosition(1)));
							return;
						}
					}
					else {
						stream.addError(line.createErrorForArgument("Unknown texture sampler: expected 'diffuse', 'normal' or 'sampler0', 'sampler1', etc.", 1));
						return;
					}
					
					resource.properties.add(property);
				}
			}));
		}
	}


	public static class Factory implements EffectResourceFactory {
		
		@Override
		public int getTypeCode() {
			return TYPE_CODE;
		}

		@Override
		public void addParser(ArgScriptStream<EffectUnit> stream) {
			stream.addParser(KEYWORD, new Parser());
		}

		@Override
		public int getMinVersion() {
			return 0;
		}

		@Override
		public int getMaxVersion() {
			return 0;
		}

		@Override
		public EffectResource create(EffectDirectory effectDirectory, int version) {
			return new MaterialResource(effectDirectory, version);
		}
		
		@Override public String getKeyword() {
			return KEYWORD;
		}
	}


	@Override
	public EffectResourceFactory getFactory() {
		return FACTORY;
	}

	@Override
	public void toArgScript(ArgScriptWriter writer) {
		writer.command(KEYWORD).arguments(resourceID).startBlock();
		
		writer.command("shader").arguments(shaderID);
		
		if (!properties.isEmpty()) {
			writer.blankLine();
			for (MaterialProperty prop : properties) {
				prop.toArgScript(writer);
			}
		}
		
		writer.endBlock().commandEND();
	}
}
