package sporemodder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.StreamReader;

public class Test2 {
	
	int[] ids;
	int[] sizes;
	
	public void read(StreamReader stream) throws IOException {
		int classID = stream.readInt();
		
		// size
		stream.readInt();
		
		int count = stream.readInt();
		ids = new int[count];
		sizes = new int[count];
		
		for (int i = 0; i < count; i++) {
			ids[i] = stream.readInt();
			sizes[i] = stream.readInt();
			
			System.out.println(HashManager.get().hexToString(ids[i]) + ": " + sizes[i]);
		}
	}
	
	/*public static void main(String[] args) throws IOException {
		MainApp.testInit();
		
		String inputPath = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Gadea\\0x01897C18\\0x01897C18.0x01897C18";
		
		try (FileStream stream = new FileStream(inputPath, "r")) {
			Test2 test = new Test2();
			test.read(stream);
			
			stream.skip(Arrays.stream(test.sizes).sum());
			
			System.out.println(stream.getFilePointer());
			System.out.println();
			
			test.read(stream);
			
			for (int i = 0; i < test.sizes.length; ++i) {
				if (test.ids[i] == 0x056e3635) break;
				stream.skip(test.sizes[i]);
			}
			System.out.println(stream.getFilePointer());
		}
	}*/
	
	public static void main(String[] args) {
		File folder = new File("E:\\Eric\\Eclipse Projects\\SporeModder FX\\Styles\\Dark");
		for (File file : folder.listFiles()) {
			System.out.print("\"" + file.getName() + "\", ");
		}
	}
}
