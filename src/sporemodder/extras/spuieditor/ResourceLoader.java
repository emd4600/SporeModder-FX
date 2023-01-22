package sporemodder.extras.spuieditor;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import sporemodder.MainApp;
import sporemodder.ProjectManager;
import sporemodder.extras.spuieditor.components.ButtonDrawableRadio;
import sporemodder.extras.spuieditor.components.ButtonDrawableStandard;
import sporemodder.extras.spuieditor.components.CascadeEffect;
import sporemodder.extras.spuieditor.components.ComboBoxDrawable;
import sporemodder.extras.spuieditor.components.ConsoleWindow;
import sporemodder.extras.spuieditor.components.DialogDrawable;
import sporemodder.extras.spuieditor.components.ExplicitNavigator;
import sporemodder.extras.spuieditor.components.FadeEffect;
import sporemodder.extras.spuieditor.components.FrameDrawable;
import sporemodder.extras.spuieditor.components.GlideEffect;
import sporemodder.extras.spuieditor.components.Image;
import sporemodder.extras.spuieditor.components.ImageCursorProvider;
import sporemodder.extras.spuieditor.components.ImageDrawable;
import sporemodder.extras.spuieditor.components.InflateEffect;
import sporemodder.extras.spuieditor.components.ModulateEffect;
import sporemodder.extras.spuieditor.components.PerspectiveEffect;
import sporemodder.extras.spuieditor.components.ProportionalLayout;
import sporemodder.extras.spuieditor.components.RelativeNavigator;
import sporemodder.extras.spuieditor.components.RotateEffect;
import sporemodder.extras.spuieditor.components.SPUIComponent;
import sporemodder.extras.spuieditor.components.ScrollbarDrawable;
import sporemodder.extras.spuieditor.components.SimpleLayout;
import sporemodder.extras.spuieditor.components.SliderDrawable;
import sporemodder.extras.spuieditor.components.SpinnerDrawable;
import sporemodder.extras.spuieditor.components.StdDrawable;
import sporemodder.extras.spuieditor.components.TreeExpanderDrawable;
import sporemodder.extras.spuieditor.components.TreeNode;
import sporemodder.extras.spuieditor.components.WatchGraph;
import sporemodder.extras.spuieditor.components.WatchWindow;
import sporemodder.extras.spuieditor.components.WinButton;
import sporemodder.extras.spuieditor.components.WinComboBox;
import sporemodder.extras.spuieditor.components.WinDialog;
import sporemodder.extras.spuieditor.components.WinGrid;
import sporemodder.extras.spuieditor.components.WinMessageBox;
import sporemodder.extras.spuieditor.components.WinScrollbar;
import sporemodder.extras.spuieditor.components.WinSlider;
import sporemodder.extras.spuieditor.components.WinSpinner;
import sporemodder.extras.spuieditor.components.WinText;
import sporemodder.extras.spuieditor.components.WinTextEdit;
import sporemodder.extras.spuieditor.components.WinTreeView;
import sporemodder.extras.spuieditor.components.WinXHTML;
import sporemodder.extras.spuieditor.components.Window;
import sporemodder.extras.spuieditor.components.cSPUIAnimatedIconWin;
import sporemodder.extras.spuieditor.components.cSPUIBehaviorActionWinInterpolator;
import sporemodder.extras.spuieditor.components.cSPUIBehaviorActionWinState;
import sporemodder.extras.spuieditor.components.cSPUIBehaviorEventBase;
import sporemodder.extras.spuieditor.components.cSPUIBehaviorPredicateWinState;
import sporemodder.extras.spuieditor.components.cSPUIBehaviorTimeFunctionDampedPeriodic;
import sporemodder.extras.spuieditor.components.cSPUIBehaviorTimeFunctionRamp;
import sporemodder.extras.spuieditor.components.cSPUIBehaviorTimeFunctionSmoothRamp;
import sporemodder.extras.spuieditor.components.cSPUIBehaviorWinBoolStateEvent;
import sporemodder.extras.spuieditor.components.cSPUIBehaviorWinEventBase;
import sporemodder.extras.spuieditor.components.cSPUIBehaviorWinInterpolatorScale;
import sporemodder.extras.spuieditor.components.cSPUIBehaviorWinInterpolatorShadeColor;
import sporemodder.extras.spuieditor.components.cSPUIFrameSequencer;
import sporemodder.extras.spuieditor.components.cSPUILaunchScreenWinProc;
import sporemodder.extras.spuieditor.components.cSPUILayerIdWinProc;
import sporemodder.extras.spuieditor.components.cSPUILayoutZoom;
import sporemodder.extras.spuieditor.components.cSPUIMaterialEffect;
import sporemodder.extras.spuieditor.components.cSPUIMaterialWinProc;
import sporemodder.extras.spuieditor.components.cSPUIPopupMenuItemWin;
import sporemodder.extras.spuieditor.components.cSPUIPopupMenuWin;
import sporemodder.extras.spuieditor.components.cSPUIProgressBarGradientWin;
import sporemodder.extras.spuieditor.components.cSPUIProgressBarWin;
import sporemodder.extras.spuieditor.components.cSPUIRotateEffect;
import sporemodder.extras.spuieditor.components.cSPUIStdDrawable;
import sporemodder.extras.spuieditor.components.cSPUIStdDrawableImageInfo;
import sporemodder.extras.spuieditor.components.cSPUISwarmEffect;
import sporemodder.extras.spuieditor.components.cSPUITextWin;
import sporemodder.extras.spuieditor.components.cSPUITooltipWinProc;
import sporemodder.extras.spuieditor.components.cSPUIVariableWidthDrawable;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.LocaleFile;
import sporemodder.files.formats.ResourceKey;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIFileResource;
import sporemodder.files.formats.spui.SPUIMain;
import sporemodder.files.formats.spui.SPUIObject;
import sporemodder.files.formats.spui.SPUIStructResource;
import sporemodder.util.ProjectItem;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.Project;

public class ResourceLoader {
	
	private static final HashMap<ResourceKey, BufferedImage> loadedImages = new HashMap<ResourceKey, BufferedImage>();
	private static final HashMap<SPUIObject, SPUIComponent> loadedBlocks = new HashMap<SPUIObject, SPUIComponent>();
	
	private static final HashMap<Integer, LocaleFile> loadedLocales = new HashMap<Integer, LocaleFile>();
	
	private static final int LOCALE_FOLDER = 0x02FABF01;
	private static final int LOCALE_TYPE = 0x02FAC0B6;
	
	public static void clearResources() {
		loadedImages.clear();
		loadedBlocks.clear();
		loadedLocales.clear();
	}
 
	public static SPUIComponent getComponent(SPUIObject object) throws InvalidBlockException, IOException {
		SPUIComponent result = loadedBlocks.get(object);
		if (result != null) {
			return result;
		}
		
		switch (object.getObjectType()) {
		/* SimpleLayout */			case SimpleLayout.TYPE: result = new SimpleLayout((SPUIBlock) object); break;
		/* ProportionalLayout */	case ProportionalLayout.TYPE: result = new ProportionalLayout((SPUIBlock) object); break;
		
		/* FadeEffect */			case FadeEffect.TYPE: result = new FadeEffect((SPUIBlock) object); break;
		/* CascadeEffect */			case CascadeEffect.TYPE: result = new CascadeEffect((SPUIBlock) object); break;
		/* GlideEffect */			case GlideEffect.TYPE: result = new GlideEffect((SPUIBlock) object); break;
		/* InflateEffect */			case InflateEffect.TYPE: result = new InflateEffect((SPUIBlock) object); break;
		/* ModulateEffect */		case ModulateEffect.TYPE: result = new ModulateEffect((SPUIBlock) object); break;
		/* PerspectiveEffect */		case PerspectiveEffect.TYPE: result = new PerspectiveEffect((SPUIBlock) object); break;
		/* RotateEffect */			case RotateEffect.TYPE: result = new RotateEffect((SPUIBlock) object); break;
		/* cSPUIRotateEffect */		case cSPUIRotateEffect.TYPE: result = new cSPUIRotateEffect((SPUIBlock) object); break;
		/* cSPUISwarmEffect */		case cSPUISwarmEffect.TYPE: result = new cSPUISwarmEffect((SPUIBlock) object); break;
		/* cSPUIMaterialEffect */	case cSPUIMaterialEffect.TYPE: result = new cSPUIMaterialEffect((SPUIBlock) object); break;
		/* cSPUILayerIdWinProc */	case cSPUILayerIdWinProc.TYPE: result = new cSPUILayerIdWinProc((SPUIBlock) object); break;
		/* cSPUIMaterialWinProc */	case cSPUIMaterialWinProc.TYPE: result = new cSPUIMaterialWinProc((SPUIBlock) object); break;
		/* cSPUITooltipWinProc */	case cSPUITooltipWinProc.TYPE: result = new cSPUITooltipWinProc((SPUIBlock) object); break;
		
		/* ImageDrawable */			case ImageDrawable.TYPE: result = new ImageDrawable((SPUIBlock) object); break;
		/* StdDrawable */			case StdDrawable.TYPE: result = new StdDrawable((SPUIBlock) object); break;
		/* cSPUIStdDrawableImageInfo */		case cSPUIStdDrawableImageInfo.TYPE: result = new cSPUIStdDrawableImageInfo((SPUIBlock) object); break;
		/* cSPUIStdDrawable */		case cSPUIStdDrawable.TYPE: result = new cSPUIStdDrawable((SPUIBlock) object); break;
		/* ButtonDrawableStd */		case ButtonDrawableStandard.TYPE: result = new ButtonDrawableStandard((SPUIBlock) object); break;
		/* ButtonDrawableRadio */	case ButtonDrawableRadio.TYPE: result = new ButtonDrawableRadio((SPUIBlock) object); break;
		/* SpinnerDrawable */		case SpinnerDrawable.TYPE: result = new SpinnerDrawable((SPUIBlock) object); break;
		/* SliderDrawable */		case SliderDrawable.TYPE: result = new SliderDrawable((SPUIBlock) object); break;
		/* FrameDrawable */			case FrameDrawable.TYPE: result = new FrameDrawable((SPUIBlock) object); break;
		/* ScrollbarDrawable */		case ScrollbarDrawable.TYPE: result = new ScrollbarDrawable((SPUIBlock) object); break;
		/* ComboBoxDrawable */		case ComboBoxDrawable.TYPE: result = new ComboBoxDrawable((SPUIBlock) object); break;
		/* cSPUIVariableWidthDrawable */		case cSPUIVariableWidthDrawable.TYPE: result = new cSPUIVariableWidthDrawable((SPUIBlock) object); break;
		/* DialogDrawable */		case DialogDrawable.TYPE: result = new DialogDrawable((SPUIBlock) object); break;
		/* Image */					case Image.TYPE: result = new Image(object); break;
		/* Image (direct) */		case SPUIFileResource.TYPE: result = new Image(object); break;
		
		// Win components: TODO
		/* Window */				case Window.TYPE: result = new Window((SPUIBlock) object); break;
		/* WinButton */				case WinButton.TYPE: result = new WinButton((SPUIBlock) object); break;
		/* WinComboBox */			case WinComboBox.TYPE: result = new WinComboBox((SPUIBlock) object); break;
		/* WinDialog */				case WinDialog.TYPE: result = new WinDialog((SPUIBlock) object); break;
		/* WinGrid */				case WinGrid.TYPE: result = new WinGrid((SPUIBlock) object); break;
		/* WinSlider */				case WinSlider.TYPE: result = new WinSlider((SPUIBlock) object); break;
		/* WinScrollbar */			case WinScrollbar.TYPE: result = new WinScrollbar((SPUIBlock) object); break;
		/* WinSpinner */			case WinSpinner.TYPE: result = new WinSpinner((SPUIBlock) object); break;
		/* WinText */				case WinText.TYPE: result = new WinText((SPUIBlock) object); break;
		/* WinTextEdit */			case WinTextEdit.TYPE: result = new WinTextEdit((SPUIBlock) object); break;
		/* WinTreeView */			case WinTreeView.TYPE: result = new WinTreeView((SPUIBlock) object); break;
		
		/* cSPUIPopupMenuWin */		case cSPUIPopupMenuWin.TYPE: result = new cSPUIPopupMenuWin((SPUIBlock) object); break;
		/* cSPUIPopupMenuItemWin */	case cSPUIPopupMenuItemWin.TYPE: result = new cSPUIPopupMenuItemWin((SPUIBlock) object); break;
		
		/* cSPUITextWin */			case cSPUITextWin.TYPE: result = new cSPUITextWin((SPUIBlock) object); break;
		/* cSPUITextWin */			case cSPUIProgressBarWin.TYPE: result = new cSPUIProgressBarWin((SPUIBlock) object); break;
		/* cSPUIAnimatedIconWin */	case cSPUIAnimatedIconWin.TYPE: result = new cSPUIAnimatedIconWin((SPUIBlock) object); break;
		
		case cSPUIBehaviorWinInterpolatorScale.TYPE: result = new cSPUIBehaviorWinInterpolatorScale((SPUIBlock) object); break;
		case cSPUIBehaviorWinInterpolatorShadeColor.TYPE: result = new cSPUIBehaviorWinInterpolatorShadeColor((SPUIBlock) object); break;
		case cSPUIBehaviorTimeFunctionRamp.TYPE: result = new cSPUIBehaviorTimeFunctionRamp((SPUIBlock) object); break;
		case cSPUIBehaviorTimeFunctionSmoothRamp.TYPE: result = new cSPUIBehaviorTimeFunctionSmoothRamp((SPUIBlock) object); break;
		case cSPUIBehaviorTimeFunctionDampedPeriodic.TYPE: result = new cSPUIBehaviorTimeFunctionDampedPeriodic((SPUIBlock) object); break;
		case cSPUIBehaviorEventBase.TYPE: result = new cSPUIBehaviorEventBase((SPUIBlock) object); break;
		case cSPUIBehaviorWinEventBase.TYPE: result = new cSPUIBehaviorWinEventBase((SPUIBlock) object); break;
		case cSPUIBehaviorWinBoolStateEvent.TYPE: result = new cSPUIBehaviorWinBoolStateEvent((SPUIBlock) object); break;
		case cSPUIBehaviorPredicateWinState.TYPE: result = new cSPUIBehaviorPredicateWinState((SPUIBlock) object); break;
		case cSPUIBehaviorActionWinInterpolator.TYPE: result = new cSPUIBehaviorActionWinInterpolator((SPUIBlock) object); break;
		case cSPUIBehaviorActionWinState.TYPE: result = new cSPUIBehaviorActionWinState((SPUIBlock) object); break;
		
		// don't know what these are for or never found any example
		case cSPUIFrameSequencer.TYPE: result = new cSPUIFrameSequencer((SPUIBlock) object); break;
		case cSPUILayoutZoom.TYPE: result = new cSPUILayoutZoom((SPUIBlock) object); break;
		case cSPUILaunchScreenWinProc.TYPE: result = new cSPUILaunchScreenWinProc((SPUIBlock) object); break;
		case RelativeNavigator.TYPE: result = new RelativeNavigator((SPUIBlock) object); break;
		case ExplicitNavigator.TYPE: result = new ExplicitNavigator((SPUIBlock) object); break;
		case WatchGraph.TYPE: result = new WatchGraph((SPUIBlock) object); break;
		case WatchWindow.TYPE: result = new WatchWindow((SPUIBlock) object); break;
		case ImageCursorProvider.TYPE: result = new ImageCursorProvider((SPUIBlock) object); break;
		case ConsoleWindow.TYPE: result = new ConsoleWindow((SPUIBlock) object); break;
		case WinMessageBox.TYPE: result = new WinMessageBox((SPUIBlock) object); break;
		case cSPUIProgressBarGradientWin.TYPE: result = new cSPUIProgressBarGradientWin((SPUIBlock) object); break;
		case WinXHTML.TYPE: result = new WinXHTML((SPUIBlock) object); break;
		
		case TreeNode.TYPE: result = new TreeNode((SPUIBlock) object); break;
		case TreeExpanderDrawable.TYPE: result = new TreeExpanderDrawable((SPUIBlock) object); break;
		
		}
		
		loadedBlocks.put(object, result);
		return result;
	}
	
	public static BufferedImage loadImage(String path) throws IOException {
		return ImageIO.read(new File(path));
	}
	
	public static BufferedImage loadImage(SPUIFileResource fileResource) throws IOException {
		return loadImage(fileResource, false);
	}
	
	public static BufferedImage loadImage(SPUIFileResource fileResource, boolean forceLoad) throws IOException {
		
		BufferedImage image = null;
		
		if (!forceLoad) {
			image = loadedImages.get(fileResource.getResourceKey());
			if (image != null) {
				return image;
			}
		}
		
		String folderName = null;
		String fileName = null;
		
		String realPath = fileResource.getRealPath();
		if (realPath == null) {
			folderName = Hasher.getFileName(fileResource.getGroupID());
			fileName = Hasher.getFileName(fileResource.getInstanceID()) + "." + Hasher.getTypeName(fileResource.getTypeID());
		}
		else {
			String[] splits = realPath.split("!");
			folderName = splits[0];
			fileName = splits[1];
		}

		File file = ProjectManager.get().getFile(folderName + "/" + fileName);
		
//		if (file == null) {
//			throw new FileNotFoundException("Image " + fileResource.getStringSimple() + " not found.");
//		}
		
		
		if (file == null) {
			image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
			image.setRGB(0, 0, -1);
			
			SPUIEditor.MissingImages.add(fileResource.getStringSimple());
		}
		else {
			image = ImageIO.read(file);
		}
		
		loadedImages.put(fileResource.getResourceKey(), image);
		return image;
	}
	
	/**
	 * Attempts to get the file in the current project, in a folder with the same hash as the specified folderHash.
	 * If it's not available, it will be attempted to get the InputStream for the resource located in sporemodder.extras.spuieditor.resources with the specified name. 
	 * @param name
	 * @param folderHash
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static InputStream getResourceInputStream(String name, int folderHash) throws FileNotFoundException {
		
		
		if (true) {
			String relativePath = Hasher.getFileName(folderHash) + "/" + name;
			
			File file = ProjectManager.get().getFile(relativePath);
			if (file != null) {
				return new FileInputStream(file);
			}
		}
		
		InputStream result = ResourceLoader.class.getResourceAsStream("/sporemodder/extras/spuieditor/resources/" + name);
		if (result != null) {
			return result;
		}
		
		return null;
	}
	
	public static LocaleFile getLocaleFile(int tableID) throws NumberFormatException, IOException {
		LocaleFile result = loadedLocales.get(tableID);
		
		if (result == null) {
			
			String relativePath = Hasher.getFileName(LOCALE_FOLDER) + "/" + Hasher.getFileName(tableID) + "." + Hasher.getTypeName(LOCALE_TYPE);
			
			File file = ProjectManager.get().getFile(relativePath);
			if (file != null) {
				result = new LocaleFile(file);
				loadedLocales.put(tableID, result);
			}
		}
		
		return result;
	}
	
	public static SPUIMain loadSpui(File file) throws IOException {
		try (InputStreamAccessor in = new FileStreamAccessor(file, "r")) {
			SPUIMain spui = new SPUIMain();
			spui.read(in);
			return spui;
		}
	}
	
	public static SPUIMain loadSpuiText(File file) throws IOException, ArgScriptException {
		try (BufferedReader in = new BufferedReader(new FileReader(file))) {
			SPUIMain spui = new SPUIMain();
			spui.parse(in);
			return spui;
		}
	}
	
	public static void saveSpui(SPUIMain spui, File file) throws IOException {
		try (OutputStreamAccessor out = new FileStreamAccessor(file, "rw")) {
			spui.write(out);
		}
	}
	
	public static void saveSpuiText(SPUIMain spui, File file) throws IOException {
		try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
			spui.toArgScript().write(out);
		}
	}
	
	// This searches SPUIs that use certain component types
	public static void main(String[] args) throws FileNotFoundException, IOException {
//		String path = "E:\\Eric\\SporeMaster 2.0 beta\\spore.unpacked\\layouts_atlas~";
//		String path = "E:\\Eric\\SporeMaster 2.0 beta\\spore.unpacked\\layouts_atlas_2~";
//		String path = "E:\\Eric\\SporeMaster 2.0 beta\\spore.unpacked\\layouts_atlas_3~";
		
//		String path = "E:\\Eric\\SporeMaster AG\\spore.unpacked\\layouts_atlas~";
		
		String path = "E:\\Eric\\SporeMaster 2.0 beta\\UI.package.unpacked\\layouts_atlas~";
		
		int searchedType = cSPUIAnimatedIconWin.TYPE;
//		int searchedType = 0xAF552C4B;
		
		File folder = new File(path);
		File[] files = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".spui");
			}
		});
		
		for (File f : files) {
			
			try (FileStreamAccessor in = new FileStreamAccessor(f, "r")) {
				SPUIMain spui = new SPUIMain();
				spui.readResources(in);
				
				List<SPUIStructResource> structs = spui.getResources().getStructResources();
				for (SPUIStructResource struct : structs) {
					if (struct.getHash() == searchedType) {
						System.out.println(f);
						break;
					}
				}
			}
		}
	}
}
