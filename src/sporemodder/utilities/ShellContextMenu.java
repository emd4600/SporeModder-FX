package sporemodder.utilities;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;

import sporemodder.MainApp;
import sporemodder.files.formats.dbpf.DBPFPacker;
import sporemodder.files.formats.dbpf.DBPFUnpacker;
import sporemodder.files.formats.effects.EffectPacker;
import sporemodder.files.formats.effects.EffectUnpacker;
import sporemodder.files.formats.pctp.PctpToTxt;
import sporemodder.files.formats.pctp.TxtToPctp;
import sporemodder.files.formats.prop.PropToXml;
import sporemodder.files.formats.prop.XmlToProp;
import sporemodder.files.formats.rast.DDStoRast;
import sporemodder.files.formats.rast.RastToDDS;
import sporemodder.files.formats.renderWare4.DDSToRw4;
import sporemodder.files.formats.renderWare4.Rw4ToDDS;
import sporemodder.files.formats.spui.SpuiToTxt;
import sporemodder.files.formats.spui.TxtToSpui;
import sporemodder.files.formats.tlsa.TlsaToTxt;
import sporemodder.files.formats.tlsa.TxtToTlsa;

public class ShellContextMenu {
	
	private static final String PATH = "Software\\Classes\\";
	private static final String SHELL_KEY = "Shell\\sporemodder";
	
	private static void addConverterToRegistry(String exePath, String extension, String text, String command, String other) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException {
		String path = PATH + extension + "\\"  + SHELL_KEY + other;
		String actualValue = WinRegistry.valueForKey(WinRegistry.HKEY_CURRENT_USER, PATH + extension, null);
		if (actualValue != null) {
			path = PATH + actualValue  + "\\"  + SHELL_KEY + other;
		}
		
		WinRegistry.createKey(WinRegistry.HKEY_CURRENT_USER, path);	
		WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, path, null, text);
		WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, path, "Icon", exePath);
		WinRegistry.createKey(WinRegistry.HKEY_CURRENT_USER, path + "\\command");	
		WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, path + "\\command", null, exePath + " -convert " + command + " -i \"%1\"");
	}
	
	private static void addConverterToRegistry(String exePath, String extension, String text, String command) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException {
		addConverterToRegistry(exePath, extension, text, command, "");
	}
	
	public static boolean addButtonsToRegistry(Component parentComponent) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException {
		
		String exePath = new File(MainApp.getProgramPath(), "SporeModder.exe").getAbsolutePath();
		if (!new File(exePath).exists()) {
			JOptionPane.showMessageDialog(parentComponent, "Can't add context menu buttons without SporeModder.exe. Please, be sure you have SporeModder.exe in SporeModder's path",
					"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		exePath = "\"" + exePath + "\"";
		
		addConverterToRegistry(exePath, ".prop", "Convert to .prop.xml", PropToXml.class.getSimpleName());
		addConverterToRegistry(exePath, ".xml", "Convert to .prop", XmlToProp.class.getSimpleName());
		addConverterToRegistry(exePath, ".tlsa", "Convert to .tlsa.tlsa_t", TlsaToTxt.class.getSimpleName());
		addConverterToRegistry(exePath, ".tlsa_t", "Convert to .tlsa", TxtToTlsa.class.getSimpleName());
		addConverterToRegistry(exePath, ".pctp", "Convert to .pctp.pctp_t", PctpToTxt.class.getSimpleName());
		addConverterToRegistry(exePath, ".pctp_t", "Convert to .pctp", TxtToPctp.class.getSimpleName());
		addConverterToRegistry(exePath, ".spui", "Convert to .spui.spui_t", SpuiToTxt.class.getSimpleName());
		addConverterToRegistry(exePath, ".spui_t", "Convert to .spui", TxtToSpui.class.getSimpleName());
		addConverterToRegistry(exePath, ".rw4", "Convert to .rw4.dds", Rw4ToDDS.class.getSimpleName());
		addConverterToRegistry(exePath, ".dds", "Convert to .rw4", DDSToRw4.class.getSimpleName());
		addConverterToRegistry(exePath, ".dds", "Convert to .raster", DDStoRast.class.getSimpleName());
		addConverterToRegistry(exePath, ".rast", "Convert to .rast.dds", RastToDDS.class.getSimpleName());
		addConverterToRegistry(exePath, ".raster", "Convert to .raster.dds", RastToDDS.class.getSimpleName());
		addConverterToRegistry(exePath, ".effdir", "Unpack effects", EffectUnpacker.class.getSimpleName());
		addConverterToRegistry(exePath, "Directory", "Pack effects to .effidr", EffectPacker.class.getSimpleName());
		addConverterToRegistry(exePath, ".package", "Unpack .package", DBPFUnpacker.class.getSimpleName() + " -convert_prop -convert_rw4 - convert_tlsa -convert_effects");
		addConverterToRegistry(exePath, "Directory", "Pack mod here", DBPFPacker.class.getSimpleName(), "-packModHere");
		addConverterToRegistry(exePath, "Directory", "Pack mod as...", DBPFPacker.class.getSimpleName(), "-packModAs");
		
		
		return true;
	}
	
	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException {
		MainApp.init();
		addButtonsToRegistry(null);
	}
}
