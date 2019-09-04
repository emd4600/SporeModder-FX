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
import java.util.Arrays;
import java.util.List;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import emord.filestructures.Structure;
import emord.filestructures.StructureEndian;
import emord.filestructures.StructureLength;
import emord.filestructures.metadata.StructureMetadata;
import sporemodder.file.DocumentError;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.util.ColorRGB;

@Structure(StructureEndian.BIG_ENDIAN)
public class SkinpaintFloodEffect extends EffectComponent {

	/** The structure metadata used for reading/writing this class. */
	public static final StructureMetadata<SkinpaintFloodEffect> STRUCTURE_METADATA = StructureMetadata.generate(SkinpaintFloodEffect.class);

	public static final int TYPE_CODE = 0x0027;
	public static final String KEYWORD = "SPSkinPaintFlood";
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	
	public static final List<String> LIST_MODIFIER_TYPES = Arrays.asList("age", "random", "worldAngle", "worldPos", "worldDist",
			"boneAngle", "bonePos", "boneOffset", "torsoPos", "limbPos", "limbType", "region", "paintMask");
	
	public static final List<String> LIST_MODIFIER_CHANNELS = Arrays.asList("size", "aspect", "rotation", "spacing", "killswitch",
			"hairRadius", "alpha", "diffuseHue", "diffuseSat", "diffuseVal", "diffuseAlpha", "bumpScale", "bumpAlpha",
			"specularScale", "specularAlpha", "userVar1", "userVar2", "userVar3", "userVar4");
	
	public static final List<String> OPERATION_TYPES = Arrays.asList("set", "add", "mult", "div");
	public static final List<String> MODIFIER_FLAG = Arrays.asList(null, "open", "clamp", "wrap", "mirror", "clamp2", "wrap2", "mirror2");
	
	public static final int COMPARE_NORMAL_MODIFIER = -3;
	public static final int COMPARE_POSITION_MODIFIER = -2;
	
	public static final ArgScriptEnum ENUM_BLEND = new ArgScriptEnum();
	static {
		ENUM_BLEND.add(1, "alpha");
		ENUM_BLEND.add(2, "add");
		ENUM_BLEND.add(3, "subtract");
		ENUM_BLEND.add(4, "multiply");
		ENUM_BLEND.add(5, "screen");
		ENUM_BLEND.add(0, "inherit");
		ENUM_BLEND.add(1, "default");
	}

	
	@Structure(StructureEndian.BIG_ENDIAN)
	public static class VarModifier {
		/** The structure metadata used for reading/writing this class. */
		public static final StructureMetadata<VarModifier> STRUCTURE_METADATA = StructureMetadata.generate(VarModifier.class);
		
		public float value_0;
		public float value_1;
		public float value_2;
		public byte modifierType;
		public byte valueCount;
		public short valueIndex;
		
		public VarModifier() {};
		public VarModifier(VarModifier other) {
			value_0 = other.value_0;
			value_1 = other.value_1;
			value_2 = other.value_2;
			modifierType = other.modifierType;
			valueCount = other.valueCount;
			valueIndex = other.valueIndex;
		}
	}
	
	@Structure(StructureEndian.BIG_ENDIAN)
	public static class Variable {
		/** The structure metadata used for reading/writing this class. */
		public static final StructureMetadata<Variable> STRUCTURE_METADATA = StructureMetadata.generate(Variable.class);
		
		public final float[] range = new float[2];  // if varModifierIndex == -1 -> 0, 1
		public final float[] values = new float[2];  // if varModifierIndex == -1 -> value, 1.0f
		public byte channel;
		public byte varModifierIndex;  // index to Variable
		public byte operationType;  // 2 by default for normal variables; 1 for 'add' ? // not really?
		public byte flag;
		
		public Variable() {};
		public Variable(Variable other) {
			range[0] = other.range[0];
			range[1] = other.range[1];
			values[0] = other.values[0];
			values[1] = other.values[1];
			channel = other.channel;
			varModifierIndex = other.varModifierIndex;
			operationType = other.operationType;
			flag = other.flag;
		}
	}
	
	@StructureLength.Value(32) public final List<VarModifier> varModifiers = new ArrayList<VarModifier>();
	@StructureLength.Value(32) public final List<Variable> variables = new ArrayList<Variable>();
	@StructureLength.Value(32) public final List<Float> variableValues = new ArrayList<Float>();
	public boolean hairFaceCamera;
	public final float[] diffuseColor = {0, 0, 1};  // BE
	public int diffuseColorIndex;
	public byte diffuseBlend;
	public byte specularBlend;
	public byte bumpBlend;
	
	public SkinpaintFloodEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		SkinpaintFloodEffect effect = (SkinpaintFloodEffect) _effect;
		
		for (VarModifier varMod : effect.varModifiers) {
			varModifiers.add(new VarModifier(varMod));
		}
		for (Variable var : effect.variables) {
			variables.add(new Variable(var));
		}
		for (float value : effect.variableValues) {
			variableValues.add(value);
		}
		hairFaceCamera = effect.hairFaceCamera;
		EffectDirectory.copyArray(diffuseColor, effect.diffuseColor);
		diffuseColorIndex = effect.diffuseColorIndex;
		diffuseBlend = effect.diffuseBlend;
		specularBlend = effect.specularBlend;
		bumpBlend = effect.bumpBlend;
	}
	
	protected static class Parser extends EffectBlockParser<SkinpaintFloodEffect> {
		protected byte modifierIndex;
		
		@Override
		protected SkinpaintFloodEffect createEffect(EffectDirectory effectDirectory) {
			SkinpaintFloodEffect effect = new SkinpaintFloodEffect(effectDirectory, FACTORY.getMaxVersion());
			
			// Add two default modifiers
			for (int i = 0; i < 2; i++) {
				VarModifier varMod = new VarModifier();
				varMod.modifierType = -1;
				effect.varModifiers.add(varMod);
			}
			
			modifierIndex = -1;
			
			return effect;
		}

		@Override
		public void addParsers() {
			
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("hairFaceCamera", ArgScriptParser.create((parser, line) -> {
				Boolean value = null;
				if (line.getArguments(args, 1) && (value = stream.parseBoolean(args, 0)) != null) {
					effect.hairFaceCamera = value;
				}
			}));
			
			this.addParser("diffuseColor", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					
					String arg = args.get(0);
					
					if (arg.startsWith("color")) {
						try {
							effect.diffuseColorIndex = Byte.parseByte(arg.substring("color".length())) + 1;
							effect.diffuseColor[0] = 0.0f;
							effect.diffuseColor[1] = 0.0f;
							effect.diffuseColor[2] = 1.0f;
						}
						catch (Exception e) {
							DocumentError error = new DocumentError(e.getLocalizedMessage(), 
									args.getRealPosition(args.getPosition(0) + "color".length()),
									args.getEndPosition(0));
							
							args.getStream().addError(error);
							return;
						}
					}
					else if (arg.equals("identity")) {
						effect.diffuseColorIndex = -2;
						effect.diffuseColor[0] = 0.0f;
						effect.diffuseColor[1] = 0.0f;
						effect.diffuseColor[2] = 1.0f;
					}
					else {
						//TODO Spore does something with that vector!
						ColorRGB color = new ColorRGB();
						stream.parseColorRGB(args, 0, color);
						effect.diffuseColor[0] = color.getR();
						effect.diffuseColor[1] = color.getG();
						effect.diffuseColor[2] = color.getB();
					}
				}
			}));
			
			this.addParser("diffuseBlend", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.diffuseBlend = (byte) ENUM_BLEND.get(args, 0);
				}
			}));
			
			this.addParser("specularBlend", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.specularBlend = (byte) ENUM_BLEND.get(args, 0);
				}
			}));
			
			this.addParser("bumpBlend", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.bumpBlend = (byte) ENUM_BLEND.get(args, 0);
				}
			}));
			
			for (byte i = 0; i < LIST_MODIFIER_CHANNELS.size(); i++) {
				this.addParser(LIST_MODIFIER_CHANNELS.get(i), new VariableParser(i));
			}
			
			this.addParser("rotate", new VariableParser((byte) 2));
			this.addParser("varyHue", new VariableParser((byte) 7));
			this.addParser("varySat", new VariableParser((byte) 8));
			this.addParser("varyVal", new VariableParser((byte) 9));
			
			this.addParser("modifier", new ArgScriptBlock<EffectUnit>() {

				@Override
				public void parse(ArgScriptLine line) {
					ArgScriptArguments args = new ArgScriptArguments();
					Number value = null;
					
					VarModifier modifier = new VarModifier();
					modifier.value_0 = 0.0f;
					modifier.value_1 = 0.0f;
					modifier.value_2 = 0.0f;
					modifier.modifierType = 14;
					modifier.valueCount = 19;
					modifier.valueIndex = 0;
					
					if (!line.getArguments(args, 1, 4)) return;
					
					String arg = args.get(0);
					for (byte i = 0; i < modifier.modifierType; i++) {
						
						if (i == 13) {
							
							for (byte j = 0; j < modifier.valueCount; j++) {
								if (arg.equals(LIST_MODIFIER_CHANNELS.get(j))) {
									// This will also stop the loop
									modifier.modifierType = i;
									modifier.valueCount = j;
								}
							}
						}
						else {
							if (arg.equals(LIST_MODIFIER_TYPES.get(i))) {
								// This will also stop the loop
								modifier.modifierType = i;
							}
						}
					}
					
					switch (modifier.modifierType) {
					case 0:
					case 13:
						if (line.getOptionArguments(args, "scale", 1) && (value = stream.parseFloat(args, 0)) != null) {
							modifier.value_1 = value.floatValue();
						} else {
							modifier.value_1 = 1.0f;
						}
						break;
						
					case 2:
					case 3:
					case 4:
					case 5:
						float[] arr = new float[3];
						if (stream.parseVector3(args, 1, arr)) {
							
							if (modifier.modifierType == 4) {
								float scalar = 1 / (arr[0]*arr[0] + arr[1]*arr[1] + arr[2]*arr[2] + 0.00000001f);
								arr[0] = arr[0] * scalar;
								arr[1] = arr[1] * scalar;
								arr[2] = arr[2] * scalar;
							}
							else if (modifier.modifierType == 3) {
								float scalar = 1 / (arr[0] + arr[1] + arr[2] + 0.00000001f);
								arr[0] = arr[0] * scalar;
								arr[1] = arr[1] * scalar;
								arr[2] = arr[2] * scalar;
							}
							
							modifier.value_0 = arr[0];
							modifier.value_1 = arr[1];
							modifier.value_2 = arr[2];
						}
					}
					
					modifierIndex = (byte) effect.varModifiers.size();
					effect.varModifiers.add(modifier);
					
					stream.startBlock(this);
				}
				
				@Override
				public void onBlockEnd() {
					modifierIndex = -1;
				}
			});
			
			this.addParser("compareNormal", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 3)) {
					float[] arr = new float[3];
					Number value1 = null;
					Number value2 = null;
					
					if (!stream.parseVector3(args, 0, arr)) return;
					if ((value1 = stream.parseFloat(args, 1)) == null) return;
					if ((value2 = stream.parseFloat(args, 2)) == null) return;
					
					VarModifier modifier = new VarModifier();
					modifier.value_0 = arr[0];
					modifier.value_1 = arr[1];
					modifier.value_2 = arr[2];
					modifier.modifierType = -3;
					modifier.valueCount = 2;
					modifier.valueIndex = (short) effect.variableValues.size();
					
					effect.varModifiers.set(0, modifier);
					
					effect.variableValues.add(value1.floatValue());
					effect.variableValues.add(value2.floatValue());
				}
			}));
			
			this.addParser("comparePosition", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 2)) {
					float[] arr = new float[3];
					float[] arr2 = new float[3];
					
					if (!stream.parseVector3(args, 0, arr)) return;
					if (!stream.parseVector3(args, 1, arr2)) return;
					
					VarModifier modifier = new VarModifier();
					modifier.value_0 = arr[0];
					modifier.value_1 = arr[1];
					modifier.value_2 = arr[2];
					modifier.modifierType = -2;
					modifier.valueCount = 3;
					modifier.valueIndex = (short) effect.variableValues.size();
					
					effect.varModifiers.set(1, modifier);
					
					effect.variableValues.add(arr2[0]);
					effect.variableValues.add(arr2[1]);
					effect.variableValues.add(arr2[2]);
				}
			}));
		}
		
		private class VariableParser extends ArgScriptParser<EffectUnit> {
			
			private byte channel;
			private VariableParser(byte channel) {
				super();
				this.channel = channel;
			}
			
			private void parseNoModifier(ArgScriptLine line, ArgScriptArguments args) {
				VarModifier modifier = new VarModifier();
				modifier.value_0 = 0;
				modifier.value_1 = 0;
				modifier.value_2 = 0;
				modifier.modifierType = -1;
				modifier.valueCount = (byte) args.size();
				modifier.valueIndex = (byte) effect.variableValues.size();
				
				if (!stream.parseFloats(args, effect.variableValues)) return;
				
				Number value = null;
				if (line.getOptionArguments(args, "vary", 1) && (value = stream.parseFloat(args, 0)) != null) {
					modifier.value_0 = value.floatValue();
				}
				
				if (line.getOptionArguments(args, "offset", 1, 2) && (value = stream.parseFloat(args, 0)) != null) {
					modifier.value_1 = modifier.value_2 = value.floatValue();
					
					if (args.size() == 2 && (value = stream.parseFloat(args, 1)) != null) {
						modifier.value_2 = value.floatValue();
					}
				}
				
				effect.varModifiers.add(modifier);
				
				
				Variable variable = new Variable();
				variable.range[0] = 0.0f;
				variable.range[1] = 1.0f;
				variable.values[0] = 0.0f;
				variable.values[1] = 1.0f;
				variable.channel = channel;
				variable.varModifierIndex = (byte) (effect.varModifiers.size() - 1);
				// The default operation is 'mult'
				variable.operationType = 2;
				variable.flag = 0;
				
				if (line.hasFlag("add")) variable.operationType = 1;
				
				if (line.hasFlag("scaleNorm")) {
					variable.range[0] = 1.0f;
				}
				if (line.hasFlag("scalePos")) {
					variable.range[1] = 1.0f;
				}
				
				effect.variables.add(variable);
			}

			@Override
			public void parse(ArgScriptLine line) {
				ArgScriptArguments args = new ArgScriptArguments();
				Number value = null;
				
				if (!line.getArguments(args, 0, 100)) return;
				
				if (modifierIndex == -1) {
					if (args.size() > 1 ||
							line.hasOption("offset") ||
							line.hasOption("vary") ||
							line.hasOption("scaleNorm") ||
							line.hasOption("scalePos")) {

						parseNoModifier(line, args);
						return;
					}
				}
				
				Variable variable = new Variable();
				variable.range[0] = 0.0f;
				variable.range[1] = 1.0f;
				variable.values[0] = 0.0f;
				variable.values[1] = 1.0f;
				variable.channel = channel;
				variable.varModifierIndex = modifierIndex;
				// The default operation is 'set'
				variable.operationType = 0;
				variable.flag = 0;
				
				// Maybe we should check for '-set' which is the default operation
				if (line.hasOption("add")) variable.operationType = 1;
				else if (line.hasOption("mult")) variable.operationType = 2;
				else if (line.hasOption("div")) variable.operationType = 3;
				
				if (modifierIndex == -1) {
					if (args.size() == 1 ||
							line.getOptionArguments(args, "set", 1) ||
							line.getOptionArguments(args, "add", 1) ||
							line.getOptionArguments(args, "mult", 1) ||
							line.getOptionArguments(args, "div", 1)) {

							if ((value = stream.parseFloat(args, 0)) != null) {
								variable.values[0] = value.floatValue();
							}
							
							// Now Spore tries to parse the same options but as flags (so expecting no arguments)
							// which is incoherent... ?
					}
				}
				else {
					if (line.getOptionArguments(args, "range", 2) && (value = stream.parseFloat(args, 0)) != null) {
						variable.range[0] = value.floatValue();
						if ((value = stream.parseFloat(args, 1)) != null) variable.range[1] = value.floatValue();
					}
					
					if (line.getOptionArguments(args, "set", 2) ||
						line.getOptionArguments(args, "add", 2) ||
						line.getOptionArguments(args, "mult", 2) ||
						line.getOptionArguments(args, "div", 2)) {
						
						if ((value = stream.parseFloat(args, 0)) != null) variable.range[0] = value.floatValue();
						if ((value = stream.parseFloat(args, 1)) != null) variable.range[1] = value.floatValue();
					}
					
					if (line.hasFlag("open")) variable.flag = 1;
					else if (line.hasFlag("clamp")) variable.flag = 2;
					else if (line.hasFlag("wrap")) variable.flag = 3;
					else if (line.hasFlag("mirror")) variable.flag = 4;
					else if (line.hasFlag("clamp2")) variable.flag = 5;
					else if (line.hasFlag("wrap2")) variable.flag = 6;
					else if (line.hasFlag("mirror2")) variable.flag = 7;
				}
				
				effect.variables.add(variable);
			}
		}
	}
	
	public static class Factory implements EffectComponentFactory {
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
			return 1;
		}
		
		@Override
		public void addEffectParser(ArgScriptStream<EffectUnit> stream) {
			stream.addParser(KEYWORD, new Parser());
		}
		
		@Override
		public void addGroupEffectParser(ArgScriptBlock<EffectUnit> effectBlock) {
			effectBlock.addParser(KEYWORD, VisualEffectBlock.createGroupParser(TYPE_CODE, SkinpaintFloodEffect.class));
		}

		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new SkinpaintFloodEffect(effectDirectory, version);
		}

		@Override
		public boolean onlySupportsInline() {
			return false;
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
		writer.command(KEYWORD).arguments(name).startBlock();
		
		writer.command("hairFaceCamera").arguments(hairFaceCamera);
		
		/* --- DIFFUSE --- */
		writer.blankLine();
		writer.command("diffuseColor");
		if (diffuseColorIndex == -2) writer.arguments("identity");
		else if (diffuseColorIndex > -1) writer.arguments("color" + Integer.toString(diffuseColorIndex + 1));
		else {
			//TODO Spore does something with that list
			writer.vector(diffuseColor);
		}
		
		// diffuseAlpha
		writeNormalVariableAS(writer, 10, false);
		if (diffuseBlend != 0) writer.command("diffuseBlend").arguments(ENUM_BLEND.get(diffuseBlend));
		
		
		/* --- SPECULAR --- */
		writer.blankLine();
		// specularAlpha
		writeNormalVariableAS(writer, 14, false);
		// specularScale
		writeNormalVariableAS(writer, 13, false);
		if (specularBlend != 0) writer.command("specularBlend").arguments(ENUM_BLEND.get(specularBlend));
		
		/* --- BUMP --- */
		writer.blankLine();
		// bumpAlpha
		writeNormalVariableAS(writer, 12, false);
		// bumpScale
		writeNormalVariableAS(writer, 11, false);
		if (bumpBlend != 0) writer.command("bumpBlend").arguments(ENUM_BLEND.get(bumpBlend));
		
		boolean blankLine = true;
		// "diffuseHue", "diffuseSat", "diffuseVal"
		for (int i = 7; i < 10; i++) {
			if (writeNormalVariableAS(writer, i, blankLine)) {
				blankLine = false;
			}
		}
		// rotate
		if (writeNormalVariableAS(writer, 2, blankLine)) {
			blankLine = false;
		}
		// "userVar1", "userVar2", "userVar3", "userVar4"
		for (int i = 15; i < 19; i++) {
			if (writeNormalVariableAS(writer, i, blankLine)) {
				blankLine = false;
			}
		}
		
		VarModifier compareNormal = null;
		VarModifier comparePosition = null;
		
		// process the modifiers
		for (int i = 0; i < varModifiers.size(); ++i) {
			VarModifier vm = varModifiers.get(i);
			if (vm.valueCount == 19) {
				writeModifierArgScript(writer, i);
				writer.blankLine();
			}
			else if (vm.modifierType == COMPARE_NORMAL_MODIFIER) {
				compareNormal = vm;
			}
			else if (vm.modifierType == COMPARE_POSITION_MODIFIER) {
				comparePosition = vm;
			}
		}
		
		if (compareNormal != null) {
			writer.command("compareNormal").vector(compareNormal.value_0, compareNormal.value_1, compareNormal.value_2);
			writer.floats(variableValues.get(compareNormal.valueIndex), variableValues.get(compareNormal.valueIndex + 1));
		}
		if (comparePosition != null) {
			writer.command("comparePosition").vector(compareNormal.value_0, compareNormal.value_1, compareNormal.value_2);
			writer.vector(variableValues.get(compareNormal.valueIndex), variableValues.get(compareNormal.valueIndex + 1), variableValues.get(compareNormal.valueIndex + 2));
		}
		
		writer.endBlock().commandEND();
	}
	
	private boolean writeNormalVariableAS(ArgScriptWriter writer, int channel, boolean addBlankLine) {
		for (Variable var : variables) {
			if (var.channel == channel) {
				if (var.varModifierIndex == -1 || varModifiers.get(var.varModifierIndex).valueCount < 19) {
					if (addBlankLine) writer.blankLine();
					writeVariableArgScript(writer, var);
					return true;
				}
			}
		}
		return false;
	}
	
	private void writeVariableArgScript(ArgScriptWriter writer, Variable var) {
		writer.command(LIST_MODIFIER_CHANNELS.get(var.channel));
		
		if (var.varModifierIndex == -1) {
			if (var.operationType != 0) writer.option(OPERATION_TYPES.get(var.operationType));
			writer.floats(var.values[0]);
		} else {
			VarModifier varModifier = varModifiers.get(var.varModifierIndex);
			if (varModifier.valueIndex + varModifier.valueCount <= variableValues.size()) {
				writer.floats(variableValues.subList(varModifier.valueIndex, varModifier.valueIndex + varModifier.valueCount));
				
				if (varModifier.value_0 != 0) writer.option("vary").floats(varModifier.value_0);
				if (varModifier.value_1 != 0 || varModifier.value_2 != 0) {
					writer.option("offset").floats(varModifier.value_1);
					if (varModifier.value_1 != varModifier.value_2) writer.floats(varModifier.value_2);
				}
				
				writer.flag("scaleNorm", var.range[0] == 1.0f);
				writer.flag("scalePos", var.range[1] == 1.0f);
			}
		}
	}
	
	private void writeModifierArgScript(ArgScriptWriter writer, int modifierIndex) {
		VarModifier modifier = varModifiers.get(modifierIndex);
		writer.command("modifier");
		
		writer.arguments(LIST_MODIFIER_TYPES.get(modifier.modifierType));
		
		switch (modifier.modifierType) {
		case 0:
		case 13:
			if (modifier.value_1 != 1.0f) writer.option("scale").floats(modifier.value_1);
			break;
		case 3:
			// 1 / (Vector(1, 1, 1) · vector)
			// float dotProduct = 1 * value_0 + 1 * value_1 + 1 * value_2  + 0.00000001; 
			// we can't calculate this, but since it's already "normalized" it will work anyways
			writer.vector(modifier.value_0, modifier.value_1, modifier.value_2);
			break;
		case 4:
			float squaredSum = modifier.value_0*modifier.value_0 + 
					modifier.value_1*modifier.value_1 + modifier.value_2*modifier.value_2 - 0.00000001f;
			writer.vector(modifier.value_0 / squaredSum, modifier.value_1 / squaredSum, modifier.value_2 / squaredSum);
			break;
		case 2:
		case 5:
			writer.vector(modifier.value_0, modifier.value_1, modifier.value_2);
			break;
		}
		
		writer.startBlock();
		
		for (Variable var : variables) {
			if (var.varModifierIndex == modifierIndex) {
				writer.command(LIST_MODIFIER_CHANNELS.get(var.channel));
				writer.option(OPERATION_TYPES.get(var.operationType)).floats(var.values);
				writer.option("range").floats(var.range);
				if (var.flag != 0) writer.option(MODIFIER_FLAG.get(var.flag));
			}
		}
		
		writer.endBlock().commandEND();
	}
}
