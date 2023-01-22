package sporemodder.utilities;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class SearchSpec {
	
//	static {
//		System.load(System.getProperty("user.dir") + "\\SporeModder JNI.dll");
//	}

	private String str;
	private String lowercaseStr;
	private byte[] chars;
	private byte[] upperCaseChars;
	
	// String must be lowercase
	public SearchSpec(String str) {
		this.str = str;
		lowercaseStr = str.toLowerCase();
		try {
			chars = lowercaseStr.getBytes("US-ASCII");
			upperCaseChars = str.toUpperCase().getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getString() {
		return str;
	}
	
	public String getLowercaseString() {
		return lowercaseStr;
	}

	public byte[] getChars() {
		return chars;
	}
	
	public boolean searchFast(byte[] data) {
		if (chars.length == 0) return false;
		//TODO use lowercaseChars instead???
		loop: for (int i = 0; i < data.length; i++) {
			if (i + chars.length > data.length) return false;
			if (data[i] == chars[0] || data[i] == upperCaseChars[0]) {
				int j = 1;
				while (j < chars.length) {
					if (data[i + j] != chars[j] && data[i + j] != upperCaseChars[j]) {
						continue loop;
					}
					j++;
				}
				return true;
			}
		}
		
		return false;
	}
	
	public static native int searchInFile(String path, byte[] lowercaseChars, byte[] uppercaseChars);
	//public static native boolean searchInFile();
	
	public static List<SearchSpec> generateSearchSpecs(List<String> strings) {
		List<SearchSpec> list = new ArrayList<SearchSpec>();
		if (strings != null) {
			for (String s : strings) {
				list.add(new SearchSpec(s));
			}
		}
		return list;
	}
	
	//TODO searching non-digit characters doesn't work!
	
//	public static void main(String[] args) {
//		SearchSpec spec = new SearchSpec(" 12");
//		try {
//			boolean result = spec.searchFast(Files.readAllBytes(new File("C:\\Users\\Eric\\Desktop\\test text.txt").toPath()));
//			System.out.println(result);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	
	
	public static void main(String[] args) {
		//searchInFile("C:\\Users\\Eric\\Desktop\\test\\properties0.trigger", new byte[1][0], new byte[2][3]);
		
		List<Long> times1 = new ArrayList<Long>();
		List<Long> times2 = new ArrayList<Long>();
		
		Timer timer = new Timer.NanoTimer();
		SearchSpec spec = new SearchSpec("Properties");
		for (int i = 0; i < 5; i++) {
			boolean result = false;
			
			try {
				timer.start();
				byte[] arr = Files.readAllBytes(new File("C:\\Users\\Eric\\Desktop\\test\\properties" + i + ".trigger").toPath());
				
				result = spec.searchFast(arr);
				timer.stop();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//System.out.println("load: " + times[0] + "\t" + "check: " + times[1]);
			times1.add(timer.getTime());
			System.out.println("total: " + (timer.getTime()) + "\tresult: " + result);
		}
		
		System.out.println();
		
		for (int i = 0; i < 5; i++) {
			timer.start();
			int result = searchInFile("C:\\Users\\Eric\\Desktop\\test\\properties" + i + ".trigger", spec.chars, spec.upperCaseChars);
			timer.stop();
			
			times2.add(timer.getTime());
			System.out.println("fast: " + timer.getTime() + "\tresult: " + result);
		}
		
		
	}
}
