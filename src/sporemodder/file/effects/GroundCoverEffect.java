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

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.metadata.StructureMetadata;
import sporemodder.file.argscript.ArgScriptBlock;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

@Structure(StructureEndian.BIG_ENDIAN)
public class GroundCoverEffect extends ParticleEffect {

	/**
	 * The structure metadata used for reading/writing this class.
	 */
	public static final StructureMetadata<GroundCoverEffect> STRUCTURE_METADATA = StructureMetadata.generate(GroundCoverEffect.class);
	
	public static final String KEYWORD = "groundCover";
	public static final int TYPE_CODE = 0x002C;
	
	public static final EffectComponentFactory FACTORY = new Factory();
	
	public GroundCoverEffect(EffectDirectory effectDirectory, int version) {
		super(effectDirectory, version);
	}
	
	public static class Factory implements EffectComponentFactory {
		@Override public Class<? extends EffectComponent> getComponentClass() {
			return GroundCoverEffect.class;
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
			return 1;
		}
		
		@Override
		public void addEffectParser(ArgScriptStream<EffectUnit> stream) {
			stream.addParser(KEYWORD, new Parser() {
				@Override
				protected GroundCoverEffect createEffect(EffectDirectory effectDirectory) {
					return new GroundCoverEffect(effectDirectory, FACTORY.getMaxVersion());
				}
			});
		}
		
		@Override
		public void addGroupEffectParser(ArgScriptBlock<EffectUnit> effectBlock) {
			effectBlock.addParser(KEYWORD, VisualEffectBlock.createGroupParser(TYPE_CODE));
		}
		
		@Override
		public EffectComponent create(EffectDirectory effectDirectory, int version) {
			return new GroundCoverEffect(effectDirectory, version);
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
		super.toArgScript(writer, KEYWORD);
	}
}
