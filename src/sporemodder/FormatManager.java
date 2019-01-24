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
package sporemodder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import sporemodder.file.Converter;
import sporemodder.file.ResourceKey;
import sporemodder.file.effects.EffectsConverter;
import sporemodder.file.pctp.PCTPConverter;
import sporemodder.file.prop.PropConverter;
import sporemodder.file.raster.RasterConverter;
import sporemodder.file.rw4.RenderWareConverter;
import sporemodder.file.tlsa.TLSAConverter;

public class FormatManager extends AbstractManager {
	
	private final List<Converter> converters = new ArrayList<Converter>();
	
	@Override
	public void initialize(Properties properties) {
		converters.add(new RasterConverter());
		converters.add(new PCTPConverter());
		converters.add(new TLSAConverter());
		converters.add(new EffectsConverter());
		converters.add(new RenderWareConverter());
		converters.add(new PropConverter());
	}
	
	public Converter getDecoder(ResourceKey key) {
		for (Converter converter : converters) {
			if (converter.isDecoder(key)) {
				return converter;
			}
		}
		return null;
	}
	
	public Converter getDecoder(List<Converter> converters, ResourceKey key) {
		ListIterator<Converter> it = converters.listIterator(converters.size());
		while (it.hasPrevious()) {
			Converter converter = it.previous();
			if (converter.isDecoder(key)) {
				return converter;
			}
		}
		return null;
	}
	
	public Converter getEncoder(File file) {
		return getEncoder(converters, file);
	}
	
	public Converter getEncoder(List<Converter> converters, File file) {
		ListIterator<Converter> it = converters.listIterator(converters.size());
		while (it.hasPrevious()) {
			Converter converter = it.previous();
			if (converter.isEncoder(file)) {
				return converter;
			}
		}
		return null;
	}
	
	public List<Converter> getConverters() {
		return converters;
	}

	public static FormatManager get() {
		return MainApp.get().getFormatManager();
	}
	
	
}
