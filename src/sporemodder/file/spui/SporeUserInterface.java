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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.StreamReader;
import javafx.scene.image.Image;
import sporemodder.HashManager;
import sporemodder.MainApp;
import sporemodder.ProjectManager;
import sporemodder.file.ResourceKey;
import sporemodder.file.spui.components.DirectImage;
import sporemodder.file.spui.components.ISporeImage;
import sporemodder.file.spui.components.IWindow;
import sporemodder.file.spui.uidesigner.DesignerClass;
import sporemodder.file.spui.uidesigner.SpuiDesigner;
import sporemodder.view.editors.SpuiEditor;

public class SporeUserInterface {
	
	public static final int X = 0;
	public static final int Y = 1;
	
	public static final int HORIZONTAL = 1;
	public static final int VERTICAL = 2;  // it also works for 0 (and probably any other value)
	
	public static final int ALIGN_LEFT = 1;
	public static final int ALIGN_RIGHT = 2;
	public static final int ALIGN_CENTER = 3;
	
	public static final int ALIGN_TOP = 1;
	public static final int ALIGN_BOTTOM = 2;
	public static final int ALIGN_MIDDLE = 3;
	
	public static final int LEFT = 0;
	public static final int TOP = 1;
	public static final int RIGHT = 2;
	public static final int BOTTOM = 3;
	
	public final static int INDEX_IDLE = 0;
	public final static int INDEX_DISABLED = 1;
	public final static int INDEX_HOVER = 2;
	public final static int INDEX_CLICKED = 3;
	public final static int INDEX_SELECTED = 4;
	public final static int INDEX_SELECTED_DISABLED = 5;
	public final static int INDEX_SELECTED_HOVER = 6;
	public final static int INDEX_SELECTED_CLICK = 7;
	
	public static final int STATE_INDEX_DISABLED = 0;
	public static final int STATE_INDEX_IDLE = 1;
	public static final int STATE_INDEX_HOVER = 2;
	public static final int STATE_INDEX_ONCLICK = 3;

	public static final int MAGIC = 0xE3FE3FB8;  // LE
	public static final int MAGIC_END = 0x1C01C047;  // LE
	public static final int LAST_SUPPORTED_VERSION = 3;
	
	public static final int ROOT_FLAG = 0x8000;
	
	private static final SpuiDesigner designer = new SpuiDesigner();
	
	private int version = LAST_SUPPORTED_VERSION;
	private final List<ResourceKey> imageResources = new ArrayList<ResourceKey>();
	private final List<ResourceKey> atlasResources = new ArrayList<ResourceKey>();
	private final List<RLEHitMask> hitMasks = new ArrayList<RLEHitMask>();
	private final List<SpuiElement> elements = new ArrayList<SpuiElement>();
	private final List<IWindow> rootWindows = new ArrayList<IWindow>();
	
	private final Map<ResourceKey, Image> loadedImages = new HashMap<>();
	private final List<DirectImage> directImages = new ArrayList<>();
	
	private File projectFolder;
	
	/** List of relative paths of images that could not be loaded. */
	private final Set<String> unloadedFiles = new HashSet<>();
	
	private static void ensureDesigner() {
		if (!designer.isLoaded()) {
			designer.load();
		}
	}
	
	public static SpuiDesigner getDesigner() {
		ensureDesigner();
		return designer;
	}
	
	/**
	 * Returns a list of the relative paths of images that could not be loaded.
	 * @return
	 */
	public Collection<String> getUnloadedFiles() {
		return unloadedFiles;
	}
	
	/**
	 * Returns the folder manually specified that will be used to load images. If no project folder is
	 * specified, returns null and the active project is used instead. 
	 * @return
	 */
	public File getProjectFolder() {
		return projectFolder;
	}

	/**
	 * Specifies manually the folder that will be used to load images. If no project folder is
	 * specified, returns null and the active project is used instead. 
	 * @param projectFolder
	 */
	public void setProjectFolder(File projectFolder) {
		this.projectFolder = projectFolder;
	}

	public void read(StreamReader stream) throws IOException {
		ensureDesigner();
		
		if (stream.readLEInt() != MAGIC) {
			throw new IOException("Unsupported header magic, is the file a .SPUI?");
		}
		version = stream.readLEShort();
		if (version > LAST_SUPPORTED_VERSION) {
			throw new IOException("Version " + version + " is not supported, maximum supported version is " + LAST_SUPPORTED_VERSION + '.');
		}
		
		int[] counts = new int[4];
		stream.readLEUShorts(counts);
		
		if (counts[0] + counts[1] + counts[2] + counts[3] > 6000) {
			throw new IOException("Too many resources in SPUI. Max 6000 supported.");
		}
		
		for (int i = 0; i < counts[0]; ++i) {
			ResourceKey key = new ResourceKey();
			key.readLE(stream);
			imageResources.add(key);
		}
		
		for (int i = 0; i < counts[1]; ++i) {
			ResourceKey key = new ResourceKey();
			key.readLE(stream);
			atlasResources.add(key);
		}
		
		for (int i = 0; i < counts[2]; ++i) {
			RLEHitMask object = new RLEHitMask();
			object.read(stream, version);
			hitMasks.add(object);
		}
		
		int[] proxyIDs = new int[counts[3]];
		stream.readLEInts(proxyIDs);
		
		// First create all classes, so we can use it
		DesignerClass[] classes = new DesignerClass[proxyIDs.length];
		for (int i = 0; i < classes.length; ++i) {
			classes[i] = designer.getClass(proxyIDs[i]);
			if (classes[i] == null) {
				throw new IOException("Undefined designer class with proxyID=" + HashManager.get().hexToString(proxyIDs[i]));
			}
			elements.add(classes[i].createInstance());
			classes[i].fillDefaults(null, elements.get(elements.size() - 1));
		}
		
		int otherResourcesCount = imageResources.size() + atlasResources.size() + hitMasks.size();
		
		int magic = stream.readLEUShort();
		int proxyIndex = -1;
		while ((proxyIndex = stream.readLEShort()) != -1) {
			if (magic != 0x5FF5) {
				throw new IOException("Unsupported class magic, file might be corrupted or contain unsupported information.");
			}
			
			proxyIndex -= otherResourcesCount;
			
			int propertiesCount = stream.readLEUShort();
			
			SpuiElement element = elements.get(proxyIndex);
			classes[proxyIndex].read(this, stream, element, propertiesCount & ~ROOT_FLAG);
			
//			if (element.getEditorTag() == null) {
//				element.setEditorTag(Integer.toString(proxyIndex));
//			}
			
			if ((propertiesCount & ROOT_FLAG) != 0) {
				SpuiElement elm = elements.get(proxyIndex);
				if (IWindow.class.isAssignableFrom(elm.getClass()))
					rootWindows.add((IWindow)elm); //TODO: Don't lose root IWinProcs, if any
			}
			
			magic = stream.readLEUShort();
		}
		
		if (stream.readLEInt() != MAGIC_END) {
			throw new IOException("Unsupported end magic, file might be corrupted or contain unsupported information.");
		}
	}
	
	public Object getObject(int index) {
		if (index == -1) return null;
		
		int count = imageResources.size();
		
		if (index < count) {
			return imageResources.get(index);
		}
		index -= count;
		count = atlasResources.size();
		if (index < count) {
			return atlasResources.get(index);
		}
		index -= count;
		count = hitMasks.size();
		if (index < count) {
			return hitMasks.get(index);
		}
		index -= count;
		if (index >= elements.size()) {
			throw new IllegalArgumentException("Invalid resource index " + index);
		}
		return elements.get(index);
	}
	
	public ISporeImage getSporeImage(int index) {
		if (index == -1) return null;
		
		int count = imageResources.size();
		
		if (index < count) {
			ResourceKey res = imageResources.get(index);
			Image img = loadedImages.get(res);
			if (img == null) {
				img = loadImage(res);
				loadedImages.put(res, img);
			}
			DirectImage directImage = new DirectImage(img, res);
			directImages.add(directImage);
			return directImage;
		}
		index -= count;
		count = atlasResources.size();
		if (index < count) {
			// We want to know what resource key it is. AtlasImage will use this DirectImage
			ResourceKey res = atlasResources.get(index);
			Image img = loadedImages.get(res);
			if (img == null) {
				img = loadImage(res);
				loadedImages.put(res, img);
			}
			DirectImage directImage = new DirectImage(img, res);
			return directImage;
		}
		index -= count;
		count = hitMasks.size();
		if (index < count) {
			throw new IllegalArgumentException("Invalid resource index " + index);
		}
		index -= count;
		return (ISporeImage) elements.get(index);
	}
	
	public Image getImage(int index) {
		if (index == -1) return null;
		
		int count = imageResources.size();
		
		if (index < count) {
			ResourceKey res = imageResources.get(index);
			Image img = loadedImages.get(res);
			if (img == null) {
				img = loadImage(res);
				loadedImages.put(res, img);
			}
			return img;
		}
		index -= count;
		count = atlasResources.size();
		if (index < count) {
			ResourceKey res = atlasResources.get(index);
			Image img = loadedImages.get(res);
			if (img == null) {
				img = loadImage(res);
				loadedImages.put(res, img);
			}
			return img;
		}
	
		throw new IllegalArgumentException("Invalid resource index " + index);
	}
	
	private Image loadImage(ResourceKey key) {
		HashManager hasher = HashManager.get();
		String fileName = hasher.getFileName(key.getInstanceID()) + '.' + hasher.getTypeName(key.getTypeID());
		
		String path = hasher.getFileName(key.getGroupID()) + File.separatorChar + fileName;
		
		String uri = null;
		if (projectFolder != null) {
			File file = new File(projectFolder, path);
			//uri = file.toURI().toString();
			if (file.isDirectory()) {
				uri = new File(file, fileName).toURI().toString();
			}
			else {
				uri = file.toURI().toString();
			}
		} else {
      File file = null;
      if (ProjectManager.get().getActive() != null) {
        file = ProjectManager.get().getFile(path);
      }
      if (file != null) {
        if (file.isDirectory()) {
				  file = new File(file, fileName);
			  }
      }
      else {
        unloadedFiles.add(path);
				return null;
      }
			
			uri = file.toURI().toString();
		}
		
		try {
			return new Image(uri);
		}
		catch (Exception e) {
			unloadedFiles.add(path);
			return null;
		}
	}

	public List<IWindow> getRootWindows() {
		return rootWindows;
	}
	
	
	public static int getImageIndex(IWindow window) {
		
		int flags = window.getFlags();
		int state = window.getState();
		
		if ((state & IWindow.STATE_FLAG_SELECTED) == IWindow.STATE_FLAG_SELECTED) {
			
			if ((flags & IWindow.FLAG_ENABLED) == 0) return INDEX_SELECTED_DISABLED;
			else if ((state & IWindow.STATE_FLAG_HOVER) != 0) return INDEX_SELECTED_HOVER;
			else if ((state & IWindow.STATE_FLAG_CLICKED) != 0) return INDEX_SELECTED_CLICK;
			else return INDEX_SELECTED;
		}
		else {
			if ((flags & IWindow.FLAG_ENABLED) == 0) return INDEX_DISABLED;
			else if ((state & IWindow.STATE_FLAG_HOVER) != 0) return INDEX_HOVER;
			else if ((state & IWindow.STATE_FLAG_CLICKED) != 0) return INDEX_CLICKED;
			else return INDEX_IDLE;
		}
	}
	
	public static int getTileIndex(IWindow window) {
		int flags = window.getFlags();
		
		if ((flags & IWindow.FLAG_ENABLED) == IWindow.FLAG_ENABLED) {
			int state = window.getState();
			
			if ((state & IWindow.STATE_FLAG_CLICKED) == IWindow.STATE_FLAG_CLICKED
					|| (state & IWindow.STATE_FLAG_SELECTED) == IWindow.STATE_FLAG_SELECTED) {
				return STATE_INDEX_ONCLICK;
			}
			else if ((state & IWindow.STATE_FLAG_HOVER) == IWindow.STATE_FLAG_HOVER) {
				return STATE_INDEX_HOVER;
			}
			else return STATE_INDEX_IDLE;
		}
		else return STATE_INDEX_DISABLED;
	}

	public List<SpuiElement> getElements() {
		return elements;
	}
	
	public List<DirectImage> getDirectImages() {
		return directImages;
	}
	
	public static SpuiElement createElement(SpuiEditor editor, int proxyID) {
		DesignerClass clazz = designer.getClass(proxyID);
		if (clazz == null) throw new IllegalArgumentException("No designer class with proxyID=0x" + Integer.toHexString(proxyID));
		
		SpuiElement element = clazz.createInstance();
		clazz.fillDefaults(editor, element);
		return element;
	}
	
	public static void main(String[] args) throws IOException
	{
		MainApp.testInit();
		
		String path = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Spore (Game & Graphics)\\layouts_atlas~";
		
		for (File file : new File(path).listFiles()) {
			if (file.getName().endsWith(".spui"))
			{
				try (FileStream stream = new FileStream(file, "r"))
				{
					SporeUserInterface spui = new SporeUserInterface();
					spui.read(stream);
					
					for (SpuiElement element : spui.getElements()) {
						if (element.getDesignerClass().getProxyID() == 0x0f0b8b73) {
							System.out.println(file.getName());
							break;
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
