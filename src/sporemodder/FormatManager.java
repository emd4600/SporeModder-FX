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
import sporemodder.file.anim.AnimConverter;
import sporemodder.file.bitmaps.BitmapConverter;
import sporemodder.file.cnv.CnvConverter;
import sporemodder.file.dbpf.DBPFConverter;
import sporemodder.file.effects.EffectsConverter;
import sporemodder.file.pctp.PCTPConverter;
import sporemodder.file.prop.PropConverter;
import sporemodder.file.raster.RasterConverter;
import sporemodder.file.rw4.RenderWareConverter;
import sporemodder.file.shaders.SmtConverter;
import sporemodder.file.tlsa.TLSAConverter;

/**
 * A class that manages file format conversions. Some basic concepts managed by this class:
 * <li><b>Decoding:</b> Transforming a file from a Spore format into a format that can be used by the user. </li>
 * <li><b>Encoding:</b> Inverse to encoding, transforming a file that can be used by the user into a format Spore understands.</li>
 * <li>{@link Converter}: A class that manages encoding/decoding for a specific format.
 * 
 * This class contains a collections of converters that developers can modify, and that the user will see when packing/unpacking mods and
 * right clicking files in the Project view.
 */
public class FormatManager extends AbstractManager {
	
	private final List<Converter> converters = new ArrayList<Converter>();
	
	@Override
	public void initialize(Properties properties) {
		converters.add(new BitmapConverter());
		converters.add(new DBPFConverter());
		converters.add(new AnimConverter());
		converters.add(new SmtConverter());
		converters.add(new RasterConverter());
		converters.add(new CnvConverter());
		converters.add(new PCTPConverter());
		converters.add(new TLSAConverter());
		converters.add(new EffectsConverter());
		converters.add(new RenderWareConverter());
		converters.add(new PropConverter());
	}
	
	/**
	 * Adds the given Converter into the list of supported converters.
	 * @param converter
	 */
	public void addConverter(Converter converter) {
		converters.add(converter);
	}
	
	/**
	 * Removes the given Converter from the list of supported converters.
	 * @param converter
	 */
	public void removeConverter(Converter converter) {
		converters.remove(converter);
	}
	
	/**
	 * Same as {@link #getDecoder(List,ResourceKey)}, but checks all converters supported by the program.
	 * @param key
	 * @return
	 */
	public Converter getDecoder(ResourceKey key) {
		for (Converter converter : converters) {
			if (converter.isDecoder(key)) {
				return converter;
			}
		}
		return null;
	}
	
	/**
	 * Returns the first Converter of the list that is capable of decoding (converting to a readable format) a file with
	 * the given resource key.
	 * @param converters The list of possible converters.
	 * @param key The file name, as a resource key.
	 * @return
	 */
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
	
	/**
	 * Same as {@link #getEncoder(List,File)}, but checks all converters supported by the program.
	 * @param file
	 * @return
	 */
	public Converter getEncoder(File file) {
		return getEncoder(converters, file);
	}
	
	/**
	 * Returns the first Converter of the list that is capable of encoding (converting into Spore format) the given file.
	 * @param converters The list of possible converters.
	 * @param file The file that would need to be encoded.
	 * @return
	 */
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
	
	/**
	 * Returns a list of all the converters supported by the program.
	 * @return
	 */
	public List<Converter> getConverters() {
		return converters;
	}

	/**
	 * Returns the current instance of the FormatManager class.
	 */
	public static FormatManager get() {
		return MainApp.get().getFormatManager();
	}
	
}
