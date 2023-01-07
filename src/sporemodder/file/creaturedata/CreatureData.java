package sporemodder.file.creaturedata;

import java.io.IOException;
import java.util.Arrays;

import sporemodder.HashManager;
import sporemodder.MainApp;
import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.StreamReader;

public class CreatureData {
	private void readBuffer(StreamReader stream, byte[] dst) throws IOException {
		int length = dst.length;
		int dstIndex = 0;
		byte lastByte = 0;
		int indexInBuffer = 0;
		byte[] currentBuffer = null;
		while (dstIndex < length) {
			if (currentBuffer == null || indexInBuffer >= currentBuffer.length) {
				int size = stream.readLEUShort();
				if (size < 1 || size > 0x800)
					throw new IOException("Found size too big (" + Integer.toString(size) + " in compressed buffer");
				
				indexInBuffer = 0;
				currentBuffer = new byte[size];
				stream.read(currentBuffer);
			}
			
			if (Byte.toUnsignedInt(currentBuffer[indexInBuffer]) != 254) {
				lastByte = currentBuffer[0];
				dst[dstIndex] = lastByte;
				dstIndex++;
			}
			else {
				indexInBuffer++;
				if (indexInBuffer < currentBuffer.length) {
					int count = Byte.toUnsignedInt(currentBuffer[indexInBuffer]);
					if (count == 0) {
						lastByte = (byte)0xFE;
						dst[dstIndex] = lastByte;
						dstIndex++;
					}
					else {
						for (int i = 0; i < count; ++i) {
							dst[dstIndex + i] = lastByte;
						}
						dstIndex += count;
					}
				}
			}
			indexInBuffer++;
		}
	}
	
	public void read(StreamReader stream) throws IOException {
		int header = stream.readLEInt();
		int version = stream.readLEInt();
		
		if (header != 0xABB455B7) 
			throw new IOException("Unexpected header 0x" + Integer.toHexString(header));
		
		if (version < 9 || version > 10)
			throw new IOException("Unsupported version " + Integer.toString(version));
		
		int[] counts = new int[version < 10 ? 3 : 4];
		stream.readLEInts(counts); 
		
		System.out.println("Counts: " + Arrays.toString(counts));
		
		byte[] uncompressedBuffer = new byte[version >= 8 ? (0x80 - 0x3C) : 0x80];
		readBuffer(stream, uncompressedBuffer);
		
		System.out.println("Data 0 offset: " + stream.getFilePointer());
		for (int i = 0; i < counts[0]; i++) {
			System.out.println(stream.getFilePointer());
			stream.skip(0x8C);
		}
		
		char[][] capabilities = new char[counts[1]][4];
		int[] capabilityNumbers = new int[counts[1]];
		
		// PCTP identifiers
		System.out.println("Data 1 offset 0: " + stream.getFilePointer());
		for (int i = 0; i < counts[1]; i++) {
			capabilities[i] = new char[4];
			for (int j = 0; j < 4; j++) {
				capabilities[i][j] = (char) stream.readUByte();
			}
		}
		System.out.println("Data 1 offset 1: " + stream.getFilePointer());
		for (int i = 0; i < counts[1]; i++) {
			capabilityNumbers[i] = stream.readUByte();
		}
		
		int[] morphIDs = new int[counts[2]];
		float[] morphValues1 = new float[counts[2]];
		float[] morphValues2 = new float[counts[2]];
		
		System.out.println("Data 2 offset 0: " + stream.getFilePointer());
		stream.readLEInts(morphIDs);
		System.out.println("Data 2 offset 1: " + stream.getFilePointer());
		stream.readLEFloats(morphValues1);
		System.out.println("Data 2 offset 2: " + stream.getFilePointer());
		stream.readLEFloats(morphValues2);
		
		int field_110 = stream.readLEInt();
		System.out.println("field_110: " + HashManager.get().getFileName(field_110));
		System.out.println();
		System.out.println("CAPABILTIES:");
		for (int i = 0; i < capabilities.length; i++) {
			System.out.println("\t" + new String(capabilities[i]) + ": " + capabilityNumbers[i]);
		}
		System.out.println();
		System.out.println("MORPHS:");
		for (int i = 0; i < morphIDs.length; i++) {
			System.out.println("\t" + HashManager.get().getFileName(morphIDs[i]) + ": " + morphValues1[i] + "  " + morphValues2[i]);
		}
		
		if (version >= 10) {
			System.out.println("Data 3 offset: " + stream.getFilePointer());
			for (int i = 0; i < counts[3]; i++) {
				stream.skip(0x38);
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		MainApp.testInit();
		
		String path = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Player Creations\\creature_editorModel~\\0x1D79B75D.creaturedata";
		
		try (FileStream stream = new FileStream(path, "r")) {
			CreatureData creatureData = new CreatureData();
			creatureData.read(stream);
		}
	}
}
