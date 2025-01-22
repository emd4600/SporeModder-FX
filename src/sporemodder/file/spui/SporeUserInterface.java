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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.scene.paint.Color;
import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.StreamReader;
import javafx.scene.image.Image;
import sporemodder.HashManager;
import sporemodder.MainApp;
import sporemodder.ProjectManager;
import sporemodder.file.ResourceKey;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.spui.components.*;
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

	public static List<Path> findSpuisWithControlId(File folder, int controlId, boolean print) throws IOException {
		try (Stream<Path> pathStream = Files.walk(folder.toPath())) {
			return pathStream.filter(path -> path.getFileName().toString().endsWith(".spui"))
				.filter(path -> {
					try (FileStream stream = new FileStream(path.toFile(), "r")) {
						SporeUserInterface spui = new SporeUserInterface();
						spui.read(stream);
						boolean anyMatch = spui.getElements().stream().anyMatch(element -> {
							if (element instanceof WindowBase) {
								String controlIdStr = ((WindowBase)element).getControlID();
								return controlIdStr != null && HashManager.get().getFileHash(controlIdStr) == controlId;
							} else {
								return false;
							}
						});
						if (print && anyMatch) {
							System.out.println(path);
						}
						return anyMatch;
					} catch (IOException e) {
						return false;
					}
				})
				.collect(Collectors.toList());
		}
	}



	public static void main(String[] args) throws IOException
	{
		MainApp.testInit();

		SpuiDesigner designer = getDesigner();

		WinButton button = designer.createElementWithDefaults(WinButton.class);
		button.setControlID("ShareablePlanets-ExportButton");

		AtlasImage backgroundImage = AtlasImage.create(
				new DirectImage(null, new ResourceKey(0x31A44893, 0x556F123D, 0x2F7D0004)),
				new int[] { 29, 29 },
				new SPUIRectangle(0.291016f, 0.4375f, 0.347656f, 0.550781f)
		);
		AtlasImage iconImage = AtlasImage.create(
				new DirectImage(null, new ResourceKey(0x31A44893, 0x556F123C, 0x2F7D0004)),
				new int[] { 29, 29 },
				new SPUIRectangle(0.835938f, 0.292969f, 1.0625f, 0.40625f)
		);

		cSPUIStdDrawable drawable = designer.createElementWithDefaults(cSPUIStdDrawable.class);
		drawable.setScaleType(cSPUIStdDrawable.TILE_CENTER);
		drawable.getScaleArea().set(0.333f, 0.333f, 0.333f, 0.333f);

		for (int i = 0; i < 8; i++) {
			cSPUIStdDrawableImageInfo drawableImageInfo = designer.createElementWithDefaults(cSPUIStdDrawableImageInfo.class);

			drawableImageInfo.setProperty(0xef3c000a, 4);  // scaleType = Tile Center
			drawableImageInfo.backgroundImage = backgroundImage;
			drawableImageInfo.backgroundColor = Color.WHITE;
			drawableImageInfo.iconImage = iconImage;
			drawableImageInfo.iconColor = Color.web("#a2e4f5");
			drawableImageInfo.iconDrawMode = 0;  // Image Size
			drawableImageInfo.iconScale.set(0.9f, 0.9f);

			if (i == 1) {
				drawableImageInfo.iconDrawMode = 0;  // Window Size
				drawableImageInfo.iconColor = Color.rgb(255, 255, 255, 0.37);
				drawableImageInfo.iconImage = null;
				drawableImageInfo.iconScale.set(1.0f, 1.0f);
			} else if (i == 2) {
				drawableImageInfo.iconDrawMode = 0;  // Window Size
				drawableImageInfo.iconColor = Color.WHITE;
				drawableImageInfo.iconImage = null;
				drawableImageInfo.iconScale.set(1.0f, 1.0f);
			} else if (i == 3) {
				drawableImageInfo.iconDrawMode = 0;  // Window Size
				drawableImageInfo.iconColor = Color.web("#01547c");
				drawableImageInfo.iconImage = null;
				drawableImageInfo.iconScale.set(1.0f, 1.0f);
			}


			drawable.setImage(i, drawableImageInfo);

//			writer.addElement((AtlasImage)drawableImageInfo.backgroundImage);
//			writer.addAtlasImage((AtlasImage)drawableImageInfo.backgroundImage);
//			writer.addElement((AtlasImage)drawableImageInfo.iconImage);
//			writer.addAtlasImage((AtlasImage)drawableImageInfo.iconImage);
		}

		button.setFillDrawable(drawable);
		button.getArea().copy(new SPUIRectangle(0f, 0f, 29f, 29f));

		SpuiElement stateEvent1 = designer.createElementWithDefaults("WindowStateEvent");
		stateEvent1.setProperty(0x0255eaf8, 0x025a5f8e);  // Message = Button Highlighted
		stateEvent1.setProperty(0x03339952, 1);  // Trigger = ON

		SpuiElement stateEvent2 = designer.createElementWithDefaults("WindowStateEvent");
		stateEvent2.setProperty(0x0255eaf8, 0x025a5f95);  // Message = Button Depressed
		stateEvent2.setProperty(0x03339952, 2);  // Trigger = OFF - ON

		button.getWinProcs().add(new SimpleLayout(SimpleLayout.FLAG_LEFT | SimpleLayout.FLAG_BOTTOM));
		button.getWinProcs().add((IWinProc)stateEvent1);
		button.getWinProcs().add((IWinProc)stateEvent2);

		try (StreamWriter stream = new FileStream("E:\\Eric\\SMFX Projects\\Spore-ShareablePlanets\\data\\Spore-ShareablePlanets\\layouts_atlas~\\ShareablePlanets-ExportButton.spui", "rw")) {
			SpuiWriter writer = new SpuiWriter(List.of(button));
			writer.write(stream);
		}
	}
}
