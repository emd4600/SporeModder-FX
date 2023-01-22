package sporemodder.files.formats.spui;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import javax.swing.JPanel;

import sporemodder.files.ActionCommand;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.FileFormatStructure;
import sporemodder.files.formats.ResourceKey;
import sporemodder.userinterface.dialogs.UIErrorsDialog;
import sporemodder.utilities.InputOutputPaths.InputOutputPair;

public class TxtToSpui implements ConvertAction {

	@Override
	public FileFormatStructure convert(File input, File output)
			throws Exception {
		
		return SPUIMain.txtToSpui(input, output);
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			OutputStreamAccessor output) throws Exception {

		try (BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(input.toByteArray())))) {
			
			return SPUIMain.txtToSpui(in, output);
		}
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			String outputPath) throws Exception {

		try (BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(input.toByteArray())));
				FileStreamAccessor out = new FileStreamAccessor(outputPath, "rw", true)) {
			
			return SPUIMain.txtToSpui(in, out);
		}
	}
	
	@Override
	public FileFormatStructure convert(String inputPath,
			OutputStreamAccessor output) throws Exception {
		
		try (BufferedReader in = new BufferedReader(new FileReader(inputPath))) {
			return SPUIMain.txtToSpui(in, output);
		}
	}

	@Override
	public boolean isValid(ResourceKey key) {
		return key.getTypeID() == 0x250FE9A2;
	}

	@Override
	public boolean isValid(String extension) {
		return extension.equals("spui_t");
	}

	@Override
	public String getOutputExtension(String extension) {
		return "spui";
	}

	@Override
	public boolean isValid(File file) {
		return file.isFile() && file.getName().endsWith(".spui.spui_t");
	}

	@Override
	public File getOutputFile(File file) {
		return ActionCommand.replaceFileExtension(file, ".spui");
	}

	@Override
	public int getOutputExtensionID(String extension) {
		return 0x250FE9A2;
	}

	@Override
	public SPUIMain process(File input) throws Exception {
		try (BufferedReader in = new BufferedReader(new FileReader(input))) {
			SPUIMain main = new SPUIMain();
			main.parse(in);
			return main;
		}
	}
	
	@Override
	public JPanel createOptionsPanel() {
		return null;
	}
	
	public static boolean processCommand(String[] args) {
		List<InputOutputPair> pairs = ActionCommand.parseDefaultArguments(args, "spui_t", "spui", true);
		
		if (pairs == null) {
			return false;
		}
		
		HashMap<File, Exception> exceptions = new HashMap<File, Exception>();
		for (InputOutputPair pair : pairs) {
			try {
				SPUIMain.txtToSpui(pair.input, pair.output);
			} catch (Exception e) {
				exceptions.put(pair.input,  e);
			}
			
		}
		
		if (exceptions.size() > 0) {
			new UIErrorsDialog(exceptions);
			return false;
		}


		return true;
	}
}
