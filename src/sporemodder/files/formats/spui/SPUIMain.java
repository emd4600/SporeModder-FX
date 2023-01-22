package sporemodder.files.formats.spui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileFormatStructure;
import sporemodder.files.formats.FileStructure;
import sporemodder.files.formats.FileStructureError;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScript.ArgScriptArgumentLimiter;
import sporemodder.files.formats.argscript.ArgScript.ArgScriptType;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.spui.SPUIResource.RESOURCE_TYPE;

public class SPUIMain extends FileStructure implements FileFormatStructure {
	
	public static final String NAME_GROUP_STRUCT = "SPUI_STRUCT";
	public static final String NAME_GROUP_CHANNEL = "SPUI_CHANNEL";
	
	private static final int MAGIC = 0xE3FE3FB8; // LE
	private static final int LAST_SUPPORTED_VERSION = 3;
	
	private int version = LAST_SUPPORTED_VERSION;
//	private int resCount1; // short, SPUIFileResource
//	private int resCount2; // short, SPUIFileResource atlas
//	private int resCount3; // short, SPUIResourceType3
//	private int resCount4; // short, SPUIStructResource
	
	//private List<SPUIResource> resources;
	
	private final SPUIResourceList resources = new SPUIResourceList();
	private final List<SPUIBlock> blocks = new ArrayList<SPUIBlock>();
	
	public SPUIObject get(short index) {
		int count = resources.getValidResourcesCount();
		if (index >= count) {
			return blocks.get(index - count);
		} else {
			return resources.get(index);
		}
	}
	
	public void readResources(InputStreamAccessor in) throws IOException {
		int magic = in.readLEInt();
		expect(magic, MAGIC, "SPUI-H001", in.getFilePointer());
		
		version = in.readLEShort();
		expect(version, LAST_SUPPORTED_VERSION, CompareFunc.LESS_EQUAL, "SPUI-H002", in.getFilePointer());

		resources.read(in, version, this);
	}
	
	public boolean read(InputStreamAccessor in) throws IOException {
		if (in.length() == 0) {
			return false;
		}
		readResources(in);
		int resourcesCount = resources.getResourceCount();
		
		int var = in.readLEShort();
		while(true)
		{
			expect(var, 0x5FF5, "SPUI-B001", in.getFilePointer());
			// this can't be bigger than resourcesCount
			int resourceIndex = in.readLEUShort();
			if (resourceIndex == 0xFFFF) {
				expect(in.readLEInt(), 0x1C01C047, "SPUI-B002", in.getFilePointer());
				break;
			}
			expect(resourceIndex, resourcesCount, CompareFunc.LESS, "SPUI-B003", in.getFilePointer());
			
			SPUIBlock block = new SPUIBlock(this);
			block.read(in, resourceIndex);
			
			blocks.add(block);
			
//			System.out.println();
//			System.out.println("-- Block " + secNum++ + "\tpos: " + blockPos);
//			block.print();
			
			var = in.readLEShort();
		}
		
		return true;
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeLEInt(MAGIC);
		out.writeLEShort(version);
		resources.write(out, version);
		for (SPUIBlock block : blocks) {
			block.write(out);
		}
		// file end
		out.writeLEShort(0x5FF5);
		out.writeLEShort(0xFFFF);
		out.writeLEInt(0x1C01C047);
	}
	
	public void parse(BufferedReader in) throws IOException, ArgScriptException {
		ArgScript parser = new ArgScript(in);
		parser.setParser(new SPUIParser());
		parser.setArgumentLimiter(new ArgScriptArgumentLimiter() {
			@Override
			public int getArgumentLimit(String keyword, String line, int level,
					ArgScriptType type) {
				// if it's a section, must have only 2 arguments (sectionType and value)
				if (level == 1) {
					return 2;
				}
				return -1;
			}
		});
		parser.parse();
		
		Collection<ArgScriptCommand> commands = parser.getAllCommands();
		for (ArgScriptCommand c : commands) {
			String keyword = c.getKeyword();
			
			if (keyword.equals("FileResource")) {
				SPUIFileResource res = new SPUIFileResource();
				res.setParent(this);
				res.parse(c);
				resources.add(res);
			}
			else if (keyword.equals("ResourceType3")) {
				SPUIHitMaskResource res = new SPUIHitMaskResource();
				res.setParent(this);
				res.parse(c);
				resources.add(res);
			}
		}
		
		Collection<ArgScriptBlock> blocks = parser.getAllBlocks();
		for (ArgScriptBlock b : blocks) {
			SPUIBlock block = new SPUIBlock(this);
			block.parse(b);
			block.setParent(this);
			this.blocks.add(block);
		}
	}
	
	public ArgScript toArgScript() {
		ArgScript as = new ArgScript();
		
		int index = 0;
		for (SPUIResource res : resources) {
			if (res.getType() != RESOURCE_TYPE.STRUCT) {
				ArgScriptCommand c = res.toCommand();
				c.setComment(Integer.toString(index++));
				as.putCommand(c);
			}
		}
		
		as.addBlankLine();
		
		for (SPUIBlock block : blocks) {
			if (index++ == 893) {
				System.out.println("BREAK");
			}
			as.putBlock(block.toBlock());
			as.addBlankLine();
		}
		
		return as;
	}
	
	public void printAllErrors() {
		printErrors();
		
	}
	
	public List<FileStructureError> getAllErrors() {
		List<FileStructureError> errors = new ArrayList<FileStructureError>(getErrors());
		
		return errors;
	}
	
	
	public int getVersion() {
		return version;
	}

	public SPUIResourceList getResources() {
		return resources;
	}

	public List<SPUIBlock> getBlocks() {
		return blocks;
	}

	public static SPUIMain spuiToTxt(String inPath, String outPath) throws FileNotFoundException, IOException {
		try (FileStreamAccessor in = new FileStreamAccessor(inPath, "r");
				BufferedWriter out = new BufferedWriter(new FileWriter(outPath))) {
			
			return spuiToTxt(in, out);
		}
	}
	
	public static SPUIMain spuiToTxt(InputStreamAccessor in, BufferedWriter out) throws IOException {
		SPUIMain spui = new SPUIMain();
		spui.read(in);
		//spui.writeTxt(out);
		spui.toArgScript().write(out);
		return spui;
	}
	
	
	public static SPUIMain spuiToTxt(File inFile, File outFile) throws FileNotFoundException, IOException {
		try (FileStreamAccessor in = new FileStreamAccessor(inFile, "r");
				BufferedWriter out = new BufferedWriter(new FileWriter(outFile))) {
			
			return spuiToTxt(in, out);
		}
	}
	
	
	public static SPUIMain txtToSpui(File inPath, File outPath) throws FileNotFoundException, IOException, ArgScriptException {
		try (BufferedReader in = new BufferedReader(new FileReader(inPath));
				FileStreamAccessor out = new FileStreamAccessor(outPath, "rw", true)) {
			
			return txtToSpui(in, out);
		}
	}
	
	public static SPUIMain txtToSpui(String inPath, String outPath) throws FileNotFoundException, IOException, ArgScriptException {
		return txtToSpui(new File(inPath), new File(outPath));
	}
	
	public static SPUIMain txtToSpui(BufferedReader in, OutputStreamAccessor out) throws FileNotFoundException, IOException, ArgScriptException {
		SPUIMain spui = new SPUIMain();
		spui.parse(in);
		spui.write(out);
		return spui;
	}
	
	
	public static void main(String[] args) throws IOException {
//		MainApp.initPaths();
//		NameRegistry_old.init();
		
//		String path = "E:\\Eric\\SporeMaster 2.0 beta\\spore.unpacked\\layouts_atlas~\\";
//
//		File[] files = new File(path).listFiles();
//		for (File f : files) {
//			if (f.getName().endsWith(".spui")) 
//			{
//				FileStreamAccessor in = new FileStreamAccessor(f, "r");
//				try {
//					SPUIMain spui = new SPUIMain();
//					spui.read(in);
//					if (spui.resCount1 > 0) {
//						System.out.println("resCount1: " + spui.resCount1 + "\t" + f.getName());
//					}
//				} finally {
//					in.close();
//				}
//			}
//		}
		
		String path = "C:\\Users\\Eric\\Desktop\\SPUI test\\";
//		path = "E:\\Eric\\SporeMaster AG\\spore.unpacked\\layouts_atlas~\\";
		String file = "editorPartsPalette";
//		String file = "GlobalUIGGE-9-EP1";
//		String file = "#89BB1330";
		
		path = "E:\\Eric\\Eclipse Projects\\SporeModder\\Projects\\SPUI Files\\layouts_atlas~\\";
		path = "C:\\Users\\Ferran\\Desktop\\SPUIs\\";
//		file = "AssetDiscovery";
//		file = "editorPartsPalettePage";
//		file = "tooltips";
		file = "ScrollFrameVerticalGeneric";
		
		try (FileStreamAccessor in = new FileStreamAccessor(path + file + ".spui", "r");
				BufferedWriter out = new BufferedWriter(new FileWriter(path + file + ".spui_t"));) {
			SPUIMain spui = new SPUIMain();
			spui.read(in);
			//spui.writeTxt(out, true);
			spui.printErrors();
		}
		
//		path = "E:\\Eric\\SporeMaster 2.0 beta\\SPUI_Testpackage.package.unpacked\\layouts_atlas~\\";
//		String file = "editorPartsPalette";
//		
//		try (BufferedReader in = new BufferedReader(new FileReader(path + file + ".spui_t"));
//				FileStreamAccessor out = new FileStreamAccessor(path + file + ".spui", "rw", true)) {
//			SPUIMain spui = new SPUIMain();
//			SPUIMain.initRegistry();
//			spui.readTxt(in);
//			spui.write(out);
//		}
	}

}
