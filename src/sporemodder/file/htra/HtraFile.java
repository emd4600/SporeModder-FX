package sporemodder.file.htra;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.HashManager;
import sporemodder.MainApp;
import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.Stream.StringEncoding;
import sporemodder.file.filestructures.StreamReader;

public class HtraFile {
	public static final int OP_POW = 4;  // stack(-1) ^ stack(-2)
	public static final int OP_PUSH = 5;  // pushes value from this definition
	public static final int OP_PUSH_VARIABLE = 6;  // pushes from code set variables
	public static final int OP_POP_INTO_VARIABLE = 7;  // assigns last pushed value into variable, pops
	public static final int OP_POP_INTO_VARIABLE2 = 8;
	
	public static final int OP_GREATER_EQUAL = 13;  // pushes 1 or 0, stack(-2) >= stack(-1)
	
	public static class VariableDefinition
	{
		public int field_0;
		public float field_4;
		public int field_8;  // index to string
		public int opCode;
	}
	public final List<VariableDefinition> definitions = new ArrayList<>();
	public final List<String> strings = new ArrayList<>();
	
	public void read(StreamReader stream) throws IOException
	{
		int numDwords = stream.readInt();
		if (stream.length() - stream.getFilePointer() != numDwords * 4)
			throw new IOException("Error: Inconsistent number of dwords in HTRA file");
		
		int magicWord = stream.readInt();
		if (magicWord != 0x48545241)
			throw new IOException("Error: Wrong magic word in HTRA file");
		
		int version = stream.readLEInt();
		if (version != 1)
			throw new IOException("Error: Wrong version in HTRA file");
		
		stream.readInt();
		stream.readInt();
		
		System.out.println("·· VARIABLES ··");
		
		int count = stream.readLEInt();
		for (int i = 0; i < count; i++) {
			VariableDefinition definition = new VariableDefinition();
			definition.opCode = stream.readLEInt();
			definition.field_4 = stream.readLEFloat();
			definition.field_8 = stream.readLEInt();
			definition.field_0 = stream.readLEInt();
			definitions.add(definition);
			
			System.out.println("\t" + definition.opCode + " " + definition.field_4 + " " + definition.field_8 + " " + definition.field_0);
		}
		System.out.println(stream.getFilePointer());
		
		System.out.println("·· UNK ··");
		
		int count2 = stream.readLEInt();
		for (int i = 0; i < count2; i++) {
			int value1 = stream.readLEInt();
			int value2 = stream.readLEInt();
			System.out.println("\t" + value1 + " " + value2);
		}
		
		System.out.println("·· STRINGS ··");
		
		int stringsCount = stream.readLEInt();
		if (stream.readLEInt() != stringsCount)
			throw new IOException("Error: Inconsistent strings count");
		
		for (int i = 0; i < stringsCount; i++) {
			strings.add(stream.readCString(StringEncoding.ASCII));
			System.out.println("\t" + strings.get(strings.size() - 1));
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		MainApp.testInit();
		String path = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\CellStuff\\scripts1~\\CityGame_CreaturePopulation.htra";
		
		try (StreamReader stream = new FileStream(path, "r")) {
			HtraFile htra = new HtraFile();
			htra.read(stream);
		}
	}
	
//	public static void main(String[] args) throws Exception
//	{
//		MainApp.testInit();
//		String path = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\CellStuff\\scripts~\\cell_ground_L1.structure";
//		
//		try (StreamReader stream = new FileStream(path, "r")) {
//			HtraFile htra = new HtraFile
//			while (stream.getFilePointer() < stream.length()) {
//				long address = stream.getFilePointer();
//				int value = stream.readLEInt();
//				stream.seek(address);
//				float valuefloat = stream.readLEFloat();
//				System.out.println(address + ":\t" + HashManager.get().hexToString(value) + "\t" + valuefloat + "\t" + HashManager.get().getFileName(value));
//			}
//		}
//	}
}
