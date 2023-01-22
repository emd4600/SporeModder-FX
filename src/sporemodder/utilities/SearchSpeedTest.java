package sporemodder.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class SearchSpeedTest {
	private static boolean searchInFile(String text, File file) throws FileNotFoundException {
 		//TODO is there any fastest method?
 		
 		if (file.exists() && file.isFile()) {
 			try (Scanner scanner = new Scanner(file)) {
 				while (scanner.hasNextLine()) {
 	 				if (scanner.nextLine().contains(text)) {
 	 					return true;
 	 				}
 	 			}
 			}
 		}
 		
 		return false;
 	}
	
	private static boolean searchInFile(List<String> strings, File file) throws FileNotFoundException {
 		//TODO is there any fastest method?
 		
 		if (file.exists() && file.isFile()) {
 			boolean[] foundStrings = new boolean[strings.size()]; 
 			
 			try (Scanner scanner = new Scanner(file)) {
 				while (scanner.hasNextLine()) {
 					String line = scanner.nextLine().toLowerCase();
 					for (int i = 0; i < foundStrings.length; i++) {
 						if (!foundStrings[i]) {
 							if (line.contains(strings.get(i))) {
 	 							foundStrings[i] = true;

 	 							boolean result = true;
 	 							for (int j = 0; j < foundStrings.length; j++) result &= foundStrings[j];
 	 							if (result) {
 	 								return true;
 	 							}
 	 	 	 				}
 						}
 					}
 	 			}
 			}
 		}
 		
 		return false;
 	}
	
	private static boolean searchFast(String text, File file) throws IOException {
		return searchFast(text, Files.readAllBytes(file.toPath()));
	}
	
	private static boolean searchFast(String text, byte[] data) throws IOException {
		byte[] chars = text.getBytes("US-ASCII");
		if (chars.length == 0) return false;
		
		loop: for (int i = 0; i < data.length; i++) {
			if (i + chars.length > data.length) return false;
			if (data[i] == chars[0]) {
				int j = 1;
				while (j < chars.length) {
					if (data[i + j] != chars[j]) {
						continue loop;
					}
					j++;
				}
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean searchFast(List<String> text, File file) throws IOException {
		byte[][] chars = new byte[text.size()][];
		if (chars.length == 0) return false;
		for (int i = 0; i < chars.length; i++) {
			chars[i] = text.get(i).getBytes("US-ASCII");
		}
		boolean[] foundStrings = new boolean[chars.length]; 
		byte[] data = Files.readAllBytes(file.toPath());
		
		loop: for (int i = 0; i < data.length; i++) {
			if (i + chars.length > data.length) return false;
			for (int k = 0; k < chars.length; k++) {
				if (!foundStrings[k]) {
					if (data[i] == chars[k][0]) {
						int j = 1;
						while (j < chars[k].length) {
							if (data[i + j] != chars[k][j]) {
								continue loop;
							}
							j++;
						}
						foundStrings[k] = true;

						boolean result = true;
						for (int a = 0; a < foundStrings.length; a++) result &= foundStrings[a];
						if (result) {
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	
	private static final int TEST_COUNT = 5;
	private static final String[] SEARCH_WORDS = {"wallStyleDefensePadTexture", "0xbeac6fe2", "creature", "l", "asdasdasd"};
	private static final List<String> SEARCH_LIST = Arrays.asList(SEARCH_WORDS);
	private static final List<Long> TIMES_OLD = new ArrayList<Long>();
	private static final List<Long> TIMES_NEW = new ArrayList<Long>();
	
	public static void main(String[] args) throws IOException {
		File file = new File("E:\\Eric\\SporeMaster 2.0 beta\\SR_NS.package.unpacked\\animations~\\properties.trigger");
		
//		for (String s : SEARCH_WORDS) {
//			long time1 = System.nanoTime();
//			boolean result = searchInFile(s, file);
//			time1 = System.nanoTime() - time1;
//			
//			System.out.println(result + "\t" + time1);
//			TIMES_OLD.add(time1);
//		}
//		System.out.println();
//		for (String s : SEARCH_WORDS) {
//			long time1 = System.nanoTime();
//			boolean result = searchFast(s, file);
//			time1 = System.nanoTime() - time1;
//			
//			System.out.println(result + "\t" + time1);
//			TIMES_NEW.add(time1);
//		}
		
//		for (int i = 0; i < TEST_COUNT; i++) {
//			long time1 = System.nanoTime();
//			boolean result = searchInFile(SEARCH_LIST, file);
//			time1 = System.nanoTime() - time1;
//			
//			System.out.println(result + "\t" + time1);
//			TIMES_OLD.add(time1);
//		}
//		System.out.println();
//		for (int i = 0; i < TEST_COUNT; i++) {
//			long time1 = System.nanoTime();
//			boolean result = searchFast(SEARCH_LIST, file);
//			time1 = System.nanoTime() - time1;
//			
//			System.out.println(result + "\t" + time1);
//			TIMES_NEW.add(time1);
//		}
		
		for (int i = 0; i < TEST_COUNT; i++) {
			long time1 = System.nanoTime();
			boolean result = searchInFile(SEARCH_LIST, file);
			time1 = System.nanoTime() - time1;
			
			System.out.println(result + "\t" + time1);
			TIMES_OLD.add(time1);
		}
		System.out.println();
		for (int i = 0; i < TEST_COUNT; i++) {
			long time1 = System.nanoTime();
			byte[] data = Files.readAllBytes(file.toPath());
			boolean result = true;
			for (String s : SEARCH_WORDS) {
				result = searchFast(s, data);
				if (result == false) break;
			}
			time1 = System.nanoTime() - time1;
			
			System.out.println(result + "\t" + time1);
			TIMES_NEW.add(time1);
		}
		
		long average = 0;
		for (long l : TIMES_OLD) average += l;
		long oldAverage = average / TEST_COUNT;
		System.out.println("Average old: " + oldAverage);
		
		average = 0;
		for (long l : TIMES_NEW) average += l;
		long newAverage = average / TEST_COUNT;
		System.out.println("Average new: " + newAverage);
		
		System.out.println("New is " + oldAverage / (double) newAverage + " times faster.");
	}
}
