package sporemodder.utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import sporemodder.MainApp;
import sporemodder.files.ByteArrayStreamAccessor;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.FileStructureError;
import sporemodder.files.formats.dbpf.DBPFIndex;
import sporemodder.files.formats.dbpf.DBPFItem;
import sporemodder.files.formats.dbpf.DBPFMain;
import sporemodder.files.formats.dbpf.DBPFUnpackingTask;

public class UnpackSpeedTest {
	
	public static final List<Long> TIMES = new ArrayList<Long>();
	public static final List<Long> TIMES_FAST = new ArrayList<Long>();
	
//	private static final int TESTS_COUNT = 0;
//	private static final List<Long> TIMES_GETNAMES = new ArrayList<Long>();
//	private static final List<Long> TIMES_CHECKFOLDER = new ArrayList<Long>();
//	private static final List<Long> TIMES_PROCESSFILE = new ArrayList<Long>();
//	private static final List<Long> TIMES_WRITEFILE = new ArrayList<Long>();
//	
//	private static void unpack(String inputPath, String outputPath) throws Exception {
//		long initialTime = System.currentTimeMillis();
//		List<DBPFItem> errors = new ArrayList<DBPFItem>();
//		int errorCount = 0;
//		int fileCount = 0;
//		
//		long time0 = System.nanoTime();
//		try (DBPFMain dbpf = new DBPFMain(new FileStreamAccessor(inputPath, "r"))) 
//		{
//			//TODO This is too slow!!!!
////			DBPFMain dbpf = new DBPFMain(in);
//			
//			time0 = System.nanoTime() - time0;
//			System.out.println();
//			System.out.println("read: " + time0);
//			
//			DBPFIndex index = dbpf.getIndex();
//			fileCount = index.items.size();
//			System.out.println(fileCount + " items to unpack");
//			
//			int testCount = 0;
//			
//			for (DBPFItem item : index.items) 
//			{
//				if (testCount++ < TESTS_COUNT) {
//					long time1 = System.nanoTime();
//					String extension = Hasher.getTypeName(item.type);
//					String folderPath = outputPath + Hasher.getFileName(item.group) + "\\";
//					String path = folderPath + Hasher.getFileName(item.name) + 
//							"." + extension;
//					time1 = System.nanoTime() - time1;
//					TIMES_GETNAMES.add(time1);
//					
//					long time2 = System.nanoTime();
//					File folder = new File(folderPath);
//					if (!folder.exists()) folder.mkdir();
//					time2 = System.nanoTime() - time2;
//					TIMES_CHECKFOLDER.add(time2);
//					
//					long time3 = System.nanoTime();
//					try (ByteArrayStreamAccessor stream = item.processFile(dbpf.getSource())) 
//					{
//						time3 = System.nanoTime() - time3;
//						TIMES_PROCESSFILE.add(time3);
//						
//						long time4 = System.nanoTime();
//						stream.writeToFile(path);
//						time4 = System.nanoTime() - time4;
//						TIMES_WRITEFILE.add(time4);
//						
//						System.out.println();
//						System.out.println("getNames: " + time1);
//						System.out.println("checkFolder: " + time2);
//						System.out.println("processFile: " + time3);
//						System.out.println("writeFile: " + time4);
//					} catch (Exception e) {
//						errors.add(item);
//						e.printStackTrace();
//					}
//					
//				}
//				else {
//					String extension = Hasher.getTypeName(item.type);
//					String folderPath = outputPath + Hasher.getFileName(item.group) + "\\";
//					String path = folderPath + Hasher.getFileName(item.name) + 
//							"." + extension;
//					
//					File folder = new File(folderPath);
//					if (!folder.exists()) folder.mkdir();
//					
//					try (ByteArrayStreamAccessor stream = item.processFile(dbpf.getSource())) 
//					{
//						stream.writeToFile(path);
//					} catch (Exception e) {
//						errors.add(item);
//						e.printStackTrace();
//					}
//				}
//				
//			}
//			
//			System.out.println();
//			System.out.println(" -- AVERAGES -- ");
//			System.out.println();
//			double average = 0; 
//			for (int i = 0; i < TESTS_COUNT; i++) average += TIMES_GETNAMES.get(i);
//			System.out.println("Average getNames: " + (long)(average / (double)TESTS_COUNT));
//			
//			average = 0; 
//			for (int i = 0; i < TESTS_COUNT; i++) average += TIMES_CHECKFOLDER.get(i);
//			System.out.println("Average checkFolder: " + (long)(average / (double)TESTS_COUNT));
//			
//			average = 0; 
//			for (int i = 0; i < TESTS_COUNT; i++) average += TIMES_PROCESSFILE.get(i);
//			System.out.println("Average processFile: " + (long)(average / (double)TESTS_COUNT));
//			
//			average = 0; 
//			for (int i = 0; i < TESTS_COUNT; i++) average += TIMES_WRITEFILE.get(i);
//			System.out.println("Average writeFile: " + (long)(average / (double)TESTS_COUNT));
//			
//			average = 0;
//			for (int i = 0; i < TESTS_COUNT; i++) {
//				average += TIMES_GETNAMES.get(i) + TIMES_CHECKFOLDER.get(i) + TIMES_PROCESSFILE.get(i) + TIMES_WRITEFILE.get(i);
//			}
//			System.out.println();
//			System.out.println("Total average: " + (long)(average / TESTS_COUNT));
//			System.out.println("Estimated total time: " + ((average / (double)TESTS_COUNT) * fileCount) / 1000000 + " ms");
//		}
//		
//		long time = System.currentTimeMillis() - initialTime;
//		
//		if (errors.size() > 0) {
//			StringBuilder sb = new StringBuilder("Unpacked in " + (time / 1000.0f) + " seconds with " + errorCount + "errors.\n");
//			sb.append("The following files could not be converted: \n");
//			
//			for (int i = 0; i < errorCount; i++) 
//			{
//				DBPFItem item = errors.get(i);
//				sb.append(Hasher.getFileName(item.group) + "\\" + Hasher.getFileName(item.name) +
//						"." + Hasher.getTypeName(item.type) + "\n");
//			}
//			
//			JOptionPane.showMessageDialog(null, sb.toString(), "Error", JOptionPane.ERROR_MESSAGE);
//		}
//		else {
//			JOptionPane.showMessageDialog(null, "Successfully unpacked in " + (time / 1000.0f) + " seconds with no errors.", 
//					"Successfully unpacked", JOptionPane.INFORMATION_MESSAGE);
//		}
//		
//		System.out.println("# - Unpacked in " + (time / 1000.0f) + " seconds.");
//	}
	
	private static final int TESTS_COUNT = 15;
	private static final List<Long> TIMES_CHECKFOLDER = new ArrayList<Long>();
	private static final List<Long> TIMES_CHECKFOLDERFAST = new ArrayList<Long>();
	
	private static void unpack(String inputPath, String outputPath) throws Exception {
		long initialTime = System.currentTimeMillis();
		List<DBPFItem> errors = new ArrayList<DBPFItem>();
		int errorCount = 0;
		int fileCount = 0;
		HashMap<Integer, File> folders = new HashMap<Integer, File>();
		
		long time0 = System.nanoTime();
		try (DBPFMain dbpf = new DBPFMain(new FileStreamAccessor(inputPath, "r"))) 
		{
			//TODO This is too slow!!!!
	//		DBPFMain dbpf = new DBPFMain(in);
			
			time0 = System.nanoTime() - time0;
			System.out.println();
			System.out.println("read: " + time0);
			
			DBPFIndex index = dbpf.getIndex();
			fileCount = index.items.size();
			System.out.println(fileCount + " items to unpack");
			
			int testCount = 0;
			int testCount2 = 0;
			
			for (DBPFItem item : index.items) 
			{
				if (testCount++ < TESTS_COUNT) {
					long time2 = System.nanoTime();
					String extension = Hasher.getTypeName(item.type);
					String folderPath = outputPath + Hasher.getFileName(item.group) + "\\";
					String path = folderPath + Hasher.getFileName(item.name) + 
							"." + extension;
					
					File folder = new File(folderPath);
					if (!folder.exists()) folder.mkdir();
					time2 = System.nanoTime() - time2;
					TIMES_CHECKFOLDER.add(time2);
					
					System.out.println();
					System.out.println("path: " + path);
					System.out.println("checkFolder: " + time2);
				}
				else if (testCount2++ < TESTS_COUNT){
					long time1 = System.nanoTime();
					
					String extension = Hasher.getTypeName(item.type);
					String name = Hasher.getFileName(item.name) + 
							"." + extension;
					
					File folder = folders.get(item.group);
					if (folder == null) {
						folder = new File(outputPath + Hasher.getFileName(item.group));
						folder.mkdir();
						folders.put(item.group, folder);
					}
					time1 = System.nanoTime() - time1;
					TIMES_CHECKFOLDERFAST.add(time1);
					
					System.out.println();
					System.out.println("path: " + folder.getAbsolutePath() + "\\" + name);
					System.out.println("checkFolder fast: " + time1);
				}
			}
			
			System.out.println();
			System.out.println(" -- AVERAGES -- ");
			System.out.println();
			double average = 0; 
			
			average = 0; 
			for (int i = 0; i < TESTS_COUNT; i++) average += TIMES_CHECKFOLDER.get(i);
			System.out.println("Average checkFolder: " + (long)(average / (double)TESTS_COUNT));
			
			average = 0; 
			for (int i = 0; i < TESTS_COUNT; i++) average += TIMES_CHECKFOLDERFAST.get(i);
			System.out.println("Average checkFolder fast: " + (long)(average / (double)TESTS_COUNT));
			
			System.out.println();
			System.out.println("Total average: " + (long)(average / TESTS_COUNT));
			System.out.println("Estimated total time: " + ((average / (double)TESTS_COUNT) * fileCount) / 1000000 + " ms");
		}
	
		long time = System.currentTimeMillis() - initialTime;
		
		if (errors.size() > 0) {
			StringBuilder sb = new StringBuilder("Unpacked in " + (time / 1000.0f) + " seconds with " + errorCount + "errors.\n");
			sb.append("The following files could not be converted: \n");
			
			for (int i = 0; i < errorCount; i++) 
			{
				DBPFItem item = errors.get(i);
				sb.append(Hasher.getFileName(item.group) + "\\" + Hasher.getFileName(item.name) +
						"." + Hasher.getTypeName(item.type) + "\n");
			}
			
			JOptionPane.showMessageDialog(null, sb.toString(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		else {
			JOptionPane.showMessageDialog(null, "Successfully unpacked in " + (time / 1000.0f) + " seconds with no errors.", 
					"Successfully unpacked", JOptionPane.INFORMATION_MESSAGE);
		}
		
		System.out.println("# - Unpacked in " + (time / 1000.0f) + " seconds.");
	}


	public static void main(String[] args) throws Exception {
		MainApp.init();
//		String inputPath = "E:\\Eric\\SporeModder\\UnpackTest.package";
		String inputPath = "C:\\Program Files (x86)\\Juegos\\Spore\\Data\\Spore_Game.package";
		String outputPath = "E:\\Eric\\SporeModder\\Projects\\UnpackingTest";
		
		unpack(inputPath, outputPath + "\\");
		
//		unpack(inputPath, outputPath + "_fast\\");
//		
//		// print results:
//		for (long time : TIMES) {
//			System.out.println("decompress time: " + time);
//		}
//		System.out.println();
//		System.out.println(" -- FAST -- ");
//		System.out.println();
//		for (long time : TIMES_FAST) {
//			System.out.println("decompress time fast: " + time);
//		}
//		
//		double average = 0;
//		System.out.println();
//		System.out.println(" -- RESULTS -- ");
//		System.out.println("decompress(byte[] in, byte[] out) is X times faster:");
//		System.out.println();
//		for (int i = 0; i < TIMES.size(); i++) {
//			double time = TIMES.get(i) / (double) TIMES_FAST.get(i);
//			average += time;
//			System.out.println(time);
//		}
//		
//		average /= TIMES.size();
//		
//		System.out.println();
//		System.out.println(" -- AVERAGE -- ");
//		System.out.println("decompress(byte[] in, byte[] out) is " + average + " times faster:");
		
		
//		unpack("C:\\Program Files (x86)\\Juegos\\Spore\\Data\\Spore_Game.package", outputPath + "\\");
	}
}
