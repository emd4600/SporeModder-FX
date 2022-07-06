package sporemodder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import sporemodder.file.Converter;
import sporemodder.file.dbpf.DBPFConcurrentUnpacker;
import sporemodder.file.dbpf.DBPFUnpacker;

public class Test {
	
	private static boolean searchInData_old(File file, byte[] wordBytes, byte[] wordBytesUppercase) throws IOException {
		byte[] data = Files.readAllBytes(file.toPath());
		for (int i = 0; i < data.length; i++) {
			if (i + wordBytes.length > data.length) return false;
			if (data[i] == wordBytes[0] || data[i] == wordBytesUppercase[0]) {
				int j = 1;
				while (j < wordBytes.length && 
						(data[i+j] == wordBytes[j] || data[i+j] == wordBytesUppercase[j])) {
					++j;
				}
				if (j == wordBytes.length) return true;
			}
		}
		
		return false;
	}
	
	private static boolean searchInData_fast(File file, Pattern pattern) throws FileNotFoundException
	{
		try (Scanner scanner = new Scanner(file)) {
			return scanner.findWithinHorizon(pattern, 0) != null;
		}
	}

	public static void searchSpeedTest() throws IOException {
		
		String text = "0x034asd0";
		
		Pattern pattern = Pattern.compile(text, Pattern.CASE_INSENSITIVE);
		byte[] wordBytes = text.toLowerCase().getBytes();
		byte[] wordBytesUppercase = text.toLowerCase().getBytes();
		
		File file = new File("E:\\Eric\\SporeModder\\reg_file.txt");
		
		// Warm up
		for (int i = 0; i < 100; ++i) {
			searchInData_old(file, wordBytes, wordBytesUppercase);
		}
		
		for (int i = 0; i < 100; ++i) {
			searchInData_fast(file, pattern);
		}
		
		long time = System.nanoTime();
		for (int i = 0; i < 100; ++i) {
			searchInData_fast(file, pattern);
			//searchInData_old(file, wordBytes, wordBytesUppercase);
		}
		time = System.nanoTime() - time;
		
		System.out.println("Old: " + (time / 1000.0));
		
		time = System.nanoTime();
		for (int i = 0; i < 100; ++i) {
			searchInData_old(file, wordBytes, wordBytesUppercase);
			//searchInData_fast(file, pattern);
		}
		time = System.nanoTime() - time;
		
		System.out.println("New: " + (time / 1000.0));
	}

	final static int COUNT = 1;
	
	public static void main(String[] args) throws Exception {
		MainApp.testInit();
		
		List<Converter> converters = new ArrayList<>();
		for (Converter c : FormatManager.get().getConverters()) {
			if (c.isEnabledByDefault()) converters.add(c);
		}
		
//		String path = "C:\\Users\\Eric\\Downloads\\!!!!!!!!!!!!!!!!_DroneParts_2017_PublicRelease_1_2.package";
//		String projectPath = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\!!!!!!!!!!!!!!!!_DroneParts_2017_PublicRelease_1_2";
		
		String path = "C:\\Program Files (x86)\\Games\\Spore\\Data\\patchdata.package";
		String projectPath = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\patchdata";
		
		File projectFolder = new File(projectPath);
		
//		DBPFUnpacker oldTask = new DBPFUnpacker(new File(path), projectFolder, null, converters);
//		
//		// Heating up, let Java load everything
//		FileManager.get().deleteDirectory(projectFolder);
//		projectFolder.mkdir();
//		oldTask.call();
		
		long totalTime = 0;
//		for (int i = 0; i < COUNT; ++i) {
//			FileManager.get().deleteDirectory(projectFolder);
//			projectFolder.mkdir();
//			
//			long time = System.currentTimeMillis();
//			oldTask.call();
//			time = System.currentTimeMillis() - time;
//			
//			totalTime += time;
//		}
//		
//		System.out.println("old time:  " + (totalTime / COUNT));
		
		
		//FileManager.get().deleteDirectory(projectFolder);
		projectFolder.mkdir();
		DBPFConcurrentUnpacker newTask = new DBPFConcurrentUnpacker(new File(path), projectFolder, null, converters);
		
		totalTime = 0;
		for (int i = 0; i < COUNT; ++i) {
			FileManager.get().deleteDirectory(projectFolder);
			projectFolder.mkdir();
			
			long time = System.currentTimeMillis();
			Exception e = newTask.call();
			if (e != null) e.printStackTrace();
			time = System.currentTimeMillis() - time;
			
			totalTime += time;
		}
		
		System.out.println("new time:  " + (totalTime / COUNT));
		
		
//		oldTask = new DBPFUnpacker(new File(path), projectFolder, null, converters);
//		
//		// Heating up, let Java load everything
//		FileManager.get().deleteDirectory(projectFolder);
//		projectFolder.mkdir();
//		oldTask.call();
//		
//		totalTime = 0;
//		for (int i = 0; i < COUNT; ++i) {
//			FileManager.get().deleteDirectory(projectFolder);
//			projectFolder.mkdir();
//			
//			long time = System.currentTimeMillis();
//			oldTask.call();
//			time = System.currentTimeMillis() - time;
//			
//			totalTime += time;
//		}
//		
//		System.out.println("old time2: " + (totalTime / COUNT));
	}
}
