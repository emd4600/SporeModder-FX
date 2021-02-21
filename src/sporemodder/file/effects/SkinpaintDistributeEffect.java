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
import java.util.List;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.StructureFieldEndian;
import sporemodder.file.filestructures.StructureFieldMethod;
import sporemodder.file.filestructures.metadata.StructureMetadata;
import sporemodder.file.DocumentError;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptSpecialBlock;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.view.editors.PfxEditor;

@Structure(StructureEndian.BIG_ENDIAN)
public class SkinpaintDistributeEffect extends EffectComponent {

	public static final class ParticleSelectPair {
		public EffectComponent component;
		public float prob;
		
		public ParticleSelectPair(EffectComponent component, float prob) {
			this.component = component;
			this.prob = prob;
		}
	}
	
	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<SkinpaintDistributeEffect> STRUCTURE_METADATA = StructureMetadata.generate(SkinpaintDistributeEffect.class);
	
	public static final int TYPE_CODE = 0x0023;
	public static final String KEYWORD = "SPSkinPaintDistribute";

	public static final EffectComponentFactory FACTORY = new Factory();
	
	
	public static final int REGION_TORSO = 0x01;
	public static final int REGION_LIMBS = 0x02;
	public static final int REGION_PARTS = 0x04;
	public static final int REGION_JOINTS = 0x20;
	public static final int REGION_BACK = 0x08;  // this has a float
	public static final int REGION_BELLY = 0x10;  // this has a float
	
	
	public String field_8 = "";
	@StructureFieldMethod(read="readParticle", write="writeParticle")
	public EffectComponent particle;
	public float spacing = 0.2f;
	public int distributeLimit = -1;
	public int region = -1;
	public float regionBack;
	public float regionBelly;
	@StructureFieldEndian(StructureEndian.LITTLE_ENDIAN) public final float[] regionBodyRange = { 0.0f, 1.0f };
	public boolean regionInverse;
	public boolean regionCenterOnly;
	public boolean cover;
	public boolean ordered;
	@StructureFieldMethod(read="readSelect", write="writeSelect")
	public final List<ParticleSelectPair> particleSelect = new ArrayList<ParticleSelectPair>();
	public boolean selectAll;
	
	public SkinpaintDistributeEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	@Override public void copy(EffectComponent _effect) {
		SkinpaintDistributeEffect effect = (SkinpaintDistributeEffect) _effect;
		
		field_8 = new String(effect.field_8);
		particle = effect.particle;
		spacing = effect.spacing;
		distributeLimit = effect.distributeLimit;
		region = effect.region;
		regionBack = effect.regionBack;
		regionBelly = effect.regionBelly;
		regionBodyRange[0] = effect.regionBodyRange[0];
		regionBodyRange[1] = effect.regionBodyRange[1];
		regionInverse = effect.regionInverse;
		regionCenterOnly = effect.regionCenterOnly;
		cover = effect.cover;
		ordered = effect.ordered;
		for (ParticleSelectPair pair : effect.particleSelect) {
			particleSelect.add(new ParticleSelectPair(pair.component, pair.prob));
		}
		selectAll = effect.selectAll;
	}

	void readParticle(String fieldName, StreamReader in) throws IOException {
		int index = in.readInt();
		
		if (index == -1) {
			particle = null;
		} else {
			particle = effectDirectory.getEffect(SkinpaintParticleEffect.TYPE_CODE, index);
		}
	}
	
	void writeParticle(String fieldName, StreamWriter out, Object value) throws IOException {
		if (particle == null) {
			out.writeInt(-1);
		} else {
			out.writeInt(effectDirectory.getIndex(SkinpaintParticleEffect.TYPE_CODE, particle));
		}
	}
	
	void readSelect(String fieldName, StreamReader in) throws IOException {
		int count = in.readInt();
		for (int i = 0; i < count; i++) {
			int index = in.readInt();
			EffectComponent component = null;
			
			if (index == -1) {
				component = null;
			} else {
				component = effectDirectory.getEffect(SkinpaintParticleEffect.TYPE_CODE, index);
			}
			
			particleSelect.add(new ParticleSelectPair(component, in.readFloat()));
		}
	}
	
	void writeSelect(String fieldName, StreamWriter out, Object value) throws IOException {
		out.writeInt(particleSelect.size());
		for (ParticleSelectPair pair : particleSelect) {
			
			if (pair.component == null) {
				out.writeInt(-1);
			} else {
				out.writeInt(effectDirectory.getIndex(SkinpaintParticleEffect.TYPE_CODE, pair.component));
			}
			
			out.writeFloat(pair.prob);
		}
	}
	
	protected static class Parser extends EffectBlockParser<SkinpaintDistributeEffect> {
		@Override
		protected SkinpaintDistributeEffect createEffect(EffectDirectory effectDirectory) {
			return new SkinpaintDistributeEffect(effectDirectory, FACTORY.getMaxVersion());
		}

		@Override
		public void addParsers() {
			
			final ArgScriptArguments args = new ArgScriptArguments();
			
			this.addParser("particle", ArgScriptParser.create((parser, line) -> {
				if (line.getArguments(args, 1)) {
					effect.particle = parser.getData().getComponent(args, 0, SkinpaintParticleEffect.class, SkinpaintParticleEffect.KEYWORD);
					if (effect.particle != null) args.addHyperlink(PfxEditor.getHyperlinkType(effect.particle), effect.particle, 0);
				}
			}));
			
			this.addParser("particleSelect", new ArgScriptSpecialBlock<EffectUnit>() {
				// An error saved temporarily, in case we need to display an error in the block
				DocumentError tempError;
				
				@Override
				public void parse(ArgScriptLine line) {
					effect.selectAll = line.hasFlag("all");
					effect.particleSelect.clear();
					
					stream.startSpecialBlock(this, "end");
					
					tempError = line.createError("");
					tempError.setLine(stream.getCurrentLine());
				}
				
				@Override
				public boolean processLine(String text) {
					ArgScriptLine line = preprocess(text);
					ArgScriptArguments args = new ArgScriptArguments();
					
					if (!line.isEmpty()) {
						float prob = effect.selectAll ? 1.0f : -1.0f;

						EffectComponent component = getData().getComponent(line.getKeyword(), SkinpaintParticleEffect.class, SkinpaintParticleEffect.KEYWORD);
						if (component != null) {
							// -1 cause it's the first split, 0 is the second split
							line.addHyperlinkForArgument(PfxEditor.getHyperlinkType(component), component, -1);
						}
						
						if (component == null) {
							stream.addError(line.createErrorForKeyword(getData().getLastError()));
							return true;
						}
						
						Number value = null;
						if (line.getOptionArguments(args, "prob", 1) && (value = stream.parseFloat(args, 0, 0.0f, 1.0f)) != null) {
							prob = value.floatValue();
						}
						
						effect.particleSelect.add(new ParticleSelectPair(component, prob));
					}
					
					// This adds option warnings, so it must go after being parsed
					stream.addSyntax(line, false);
					
					return true;
				}
				
				@Override
				public void onBlockEnd() {
					if (effect.particleSelect.isEmpty()) {
						tempError.setMessage("No particles specified.");
						stream.addError(tempError);
						return;
					}
					
					if (!effect.selectAll) {
						float totalProb = 0.0f;
						int negativeProbs = 0;
						
						for (ParticleSelectPair pair : effect.particleSelect) {
							if (totalProb < 0) {
								negativeProbs++;
							}
							else {
								totalProb += pair.prob;
							}
						}
						
						if (totalProb > 1.00010001659393310546875E0) {
							tempError.setMessage("The sum of probabilities cannot be greater than 1.0");
							stream.addError(tempError);
							stream.endSpecialBlock();
							return;
						}
						
						// This distributes the remaining probability to the ones using negative probs
						for (ParticleSelectPair pair : effect.particleSelect) {
							if (pair.prob < 0) {
								pair.prob = (1.0f - totalProb) / (float) negativeProbs;
							}
						}
						
						// In reality, it seems probabilities must get accumulated 
						float addedProb = 0;
						for (ParticleSelectPair pair : effect.particleSelect) {
							addedProb += pair.prob;
							pair.prob = addedProb;
						}
					}
					
					stream.endSpecialBlock();
				}
			});
			
			this.addParser("spacing", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				
				if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
					effect.spacing = value.floatValue();
				}
				
				effect.cover = line.hasFlag("cover");
				effect.ordered = line.hasFlag("ordered");
			}));
			
			this.addParser("limit", ArgScriptParser.create((parser, line) -> {
				Number value = null;
				
				if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
					effect.distributeLimit = value.intValue();
				}
			}));
			
			this.addParser("region", ArgScriptParser.create((parser, line) -> {

				effect.region = 0;
				if (line.hasFlag("torso")) effect.region |= REGION_TORSO;
				if (line.hasFlag("limbs")) effect.region |= REGION_LIMBS;
				if (line.hasFlag("parts")) effect.region |= REGION_PARTS;
				if (line.hasFlag("joints")) effect.region |= REGION_JOINTS;
				
				Number value = null;
				
				if (line.getOptionArguments(args, "back", 1) && (value = stream.parseFloat(args, 0, 0.0f, 2.0f)) != null) {
					effect.region |= REGION_BACK;
					effect.regionBack = (float) Math.cos(value.floatValue() * Math.PI / 2.0);
				}
				
				if (line.getOptionArguments(args, "belly", 1) && (value = stream.parseFloat(args, 0, 0.0f, 2.0f)) != null) {
					effect.region |= REGION_BELLY;
					effect.regionBelly = (float) -Math.cos(value.floatValue() * Math.PI / 2.0);
				}
				
				if (line.getOptionArguments(args, "bodyRange", 2) && (value = stream.parseFloat(args, 0)) != null) {
					
					effect.regionBodyRange[0] = value.floatValue();
					
					if ((value = stream.parseFloat(args, 1)) != null) {
						effect.regionBodyRange[1] = value.floatValue();
					}
				}
				else {
					effect.regionBodyRange[0] = 0.0f;
					effect.regionBodyRange[1] = 1.0f;
				}
				
				effect.regionInverse = line.hasFlag("inverse");
				effect.regionCenterOnly = line.hasFlag("centerOnly");
			}));
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
			return 2;
		}
		
		@Override
		public void addEffectParser(ArgScriptStream<EffectUnit> stream) {
			stream.addParser(KEYWORD, new Parser());
		}
		
		@Override
		public void addGroupEffectParser(ArgScriptBlock<EffectUnit> effectBlock) {
			effectBlock.addParser(KEYWORD, VisualEffectBlock.createGroupParser(TYPE_CODE, SkinpaintDistributeEffect.class));
		}

		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new SkinpaintDistributeEffect(effectDirectory, version);
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
		
		if (particle != null) writer.command("particle").arguments(particle.getName());
		if (!particleSelect.isEmpty()) {
			writer.command("particleSelect").flag("all", selectAll).startBlock();
			for (int i = 0; i < particleSelect.size(); ++i) {
				ParticleSelectPair select = particleSelect.get(i);
				writer.command(select.component.getName());
				if (selectAll) {
					if (select.prob != 1.0f) writer.option("prob").floats(select.prob);
				}
				else {
					writer.option("prob").floats(i == 0 ? select.prob : (select.prob - particleSelect.get(i-1).prob));
				}
			}
			writer.endBlock().commandEND();
		}
		
		writer.command("spacing").floats(spacing).flag("cover", cover).flag("ordered", ordered);
		
		if (distributeLimit != -1) writer.command("limit").ints(distributeLimit);
		if (region != -1) {
			writer.command("region");
			writer.flag("torso", (region & REGION_TORSO) == REGION_TORSO);
			writer.flag("limbs", (region & REGION_LIMBS) == REGION_LIMBS);
			writer.flag("parts", (region & REGION_PARTS) == REGION_PARTS);
			writer.flag("joints", (region & REGION_JOINTS) == REGION_JOINTS);
			if ((region & REGION_BACK) == REGION_BACK) {
				writer.option("back").floats((float) (Math.acos(regionBack) / (Math.PI / 2)));
			}
			if ((region & REGION_BELLY) == REGION_BELLY) {
				writer.option("belly").floats((float) (Math.acos(-regionBelly) / (Math.PI / 2)));
			}
			if (regionBodyRange[0] != 0 || regionBodyRange[1] != 1) {
				writer.option("bodyRange").floats(regionBodyRange);
			}
			
			writer.flag("inverse", regionInverse).flag("centerOnly", regionCenterOnly);
		}
		
		writer.endBlock().commandEND();
	}
	
	@Override public List<EffectFileElement> getUsedElements() {
		List<EffectFileElement> list = new ArrayList<EffectFileElement>();
		list.add(particle);
		for (ParticleSelectPair pair : particleSelect) {
			list.add(pair.component);
		}
		return list;
	}
}
