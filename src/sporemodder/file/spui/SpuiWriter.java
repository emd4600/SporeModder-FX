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
package sporemodder.file.spui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.ResourceKey;
import sporemodder.file.spui.components.AtlasImage;
import sporemodder.file.spui.components.DirectImage;
import sporemodder.file.spui.components.IWindow;

public class SpuiWriter {

	private int version = SporeUserInterface.LAST_SUPPORTED_VERSION;
	
	private final List<ResourceKey> imageResources = new ArrayList<ResourceKey>();
	private final List<ResourceKey> atlasResources = new ArrayList<ResourceKey>();
	private final List<RLEHitMask> hitMasks = new ArrayList<RLEHitMask>();
	private final List<SpuiElement> elements = new ArrayList<SpuiElement>();
	
	private final List<DirectImage> images = new ArrayList<>();
	private final List<AtlasImage> atlasImages = new ArrayList<>();
	
	private final List<Object> indexableElements = new ArrayList<>();
	
	private final List<IWindow> rootWindows;
	
	public SpuiWriter(List<IWindow> rootWindows) {
		this.rootWindows = rootWindows;
	}
	
	public void addImage(DirectImage image) {
		images.add(image);
		imageResources.add(image.getKey());
	}
	
	public void addAtlasImage(AtlasImage image) {
		if (image.getAtlas() == null) return;
		atlasImages.add(image);
		atlasResources.add(image.getAtlas().getKey());
	}
	
	public void addHitMask(RLEHitMask hitMask) {
		hitMasks.add(hitMask);
	}
	
	public void addElement(SpuiElement element) {
		elements.add(element);
	}
	
	public int getIndex(Object data) {
		if (data == null) return -1;
		return indexableElements.indexOf(data);
	}
	
	public void write(StreamWriter stream) throws IOException {
		
		for (IWindow window : rootWindows) {
			SpuiElement element = (SpuiElement) window;
			element.addComponents(this);
		}
		
		indexableElements.addAll(images);
		// do not add the atlas objects themselves, they are in elements!
		for (AtlasImage atlas : atlasImages) {
			indexableElements.add(atlas.getAtlas());
		}
		indexableElements.addAll(hitMasks);
		indexableElements.addAll(elements);
		
		stream.writeLEInt(SporeUserInterface.MAGIC);
		stream.writeLEUShort(version);
		
		stream.writeLEUShort(imageResources.size());
		stream.writeLEUShort(atlasResources.size());
		stream.writeLEUShort(hitMasks.size());
		stream.writeLEUShort(elements.size());
		
		for (ResourceKey key : imageResources) {
			key.writeLE(stream);
		}
		for (ResourceKey key : atlasResources) {
			key.writeLE(stream);
		}
		for (RLEHitMask object : hitMasks) {
			object.write(stream, version);
		}
		for (SpuiElement element : elements) {
			stream.writeLEInt(element.getDesignerClass().getProxyID());
		}
		
		int otherResourcesCount = imageResources.size() + atlasResources.size() + hitMasks.size();
		
		stream.writeLEUShort(0x5FF5);
		for (int i = 0; i < elements.size(); ++i) {
			SpuiElement element = elements.get(i);
			
			stream.writeLEUShort(otherResourcesCount + i);
			
			element.getDesignerClass().write(this, stream, element);
			
			stream.writeLEUShort(0x5FF5);
		}
		stream.writeLEUShort(0xFFFF);
		
		stream.writeLEInt(SporeUserInterface.MAGIC_END);
	}
}
