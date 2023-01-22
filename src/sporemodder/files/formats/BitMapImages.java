package sporemodder.files.formats;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;

public class BitMapImages {
	
	//TODO not bit_48, but cube_8 !!! (and also cube_1)
	public enum BitMapType {BIT_1, BIT_8, BIT_32, BIT_48};

	public static BufferedImage read(InputStreamAccessor in, BitMapType type) throws IOException {
		int magic = in.readInt();
		int width = in.readInt();
		int height = in.readInt();
		int unk = in.readInt();  // bytes per pixel? channel count ?
		int bufferSize = in.readInt();
		
		if (magic != 0) {
			throw new IOException("Unkown BitMap image header.");
		}
		
		int imageType = BufferedImage.TYPE_CUSTOM;
		switch(type) {
		case BIT_1:
			imageType = BufferedImage.TYPE_BYTE_BINARY;
			break;
		case BIT_32:
			imageType = BufferedImage.TYPE_INT_ARGB;
			break;
		case BIT_48:
			imageType = BufferedImage.TYPE_3BYTE_BGR;
			break;
		case BIT_8:
			imageType = BufferedImage.TYPE_BYTE_GRAY;
			break;
		default:
			break;
		
		}
		BufferedImage image = new BufferedImage(width, height, imageType);
		
//		if (type != BitMapType.BIT_1) {
//			for (int x = 0; x < width; x++) {
//				for (int y = 0; y < height; y++) {
//					switch(type) {
//					case BIT_32:
//						image.setRGB(x, y, in.readInt());
//						break;
//					case BIT_48:
//						int rgb = 0;
//						rgb |= in.readShort();
//						rgb |= in.readShort() << 8;
//						rgb |= in.readShort() << 16;
//						image.setRGB(x, y, rgb);
//						break;
//					case BIT_8:
//						image.setRGB(x, y, in.readUByte());
//						break;
//					default:
//						break;
//					}
//				}
//			}
//		} else {
//			
//		}
		
		byte[] imageData = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		if (imageData.length != bufferSize) {
			throw new IOException("BitImage: Invalid buffer size.");
		}
		in.read(imageData);
		
		return image;
	}
	
	public static BufferedImage read(File input, BitMapType type) throws IOException {
		try (FileStreamAccessor in = new FileStreamAccessor(input, "r")) {
			return read(in, type);
		}
	}
	
	public static void write(OutputStreamAccessor out, BufferedImage image, BitMapType type) throws IOException {
		int width = image.getWidth();
		int height = image.getHeight();
		out.writeInt(0);
		out.writeInt(width);
		out.writeInt(height);
		int bufferSize = 0;
		if (type == BitMapType.BIT_48) {
			out.writeInt(3);
			bufferSize = width * height * 6;
		} else if (type == BitMapType.BIT_1) {
			out.writeInt(0);
			bufferSize = width * height / 8;
		} else if (type == BitMapType.BIT_8) {
			out.writeInt(1);
			bufferSize = width * height;
		} else if (type == BitMapType.BIT_32) {
			out.writeInt(4);  // ?
			bufferSize = width * height * 4;
		}
		
		out.writeInt(bufferSize);
		byte[] imageData = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		
		if (bufferSize == imageData.length) {
			out.write(imageData);
		} else {
//			System.out.println("imageData: " + imageData.length);
//			System.out.println("bufferSize: " + bufferSize);
			// the format is not exactly correct, we have to fix it
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
//					int rgb = img.getRGB(x, y);
//					int r = (rgb >> 16) & 0xFF;
//					int g = (rgb >> 8) & 0xFF;
//					int b = (rgb & 0xFF);
//					System.out.println(Integer.toHexString(image.getRGB(x, y) & 0xFF));
					out.writeUByte(image.getRGB(x, y) & 0xFF);
				}
			}
		}
	}
	
	public static void write(File output, BufferedImage image, BitMapType type) throws IOException {
		try (FileStreamAccessor out = new FileStreamAccessor(output, "rw", true)) {
			write(out, image, type);
		}
	}
	
	public static void main(String[] args) throws IOException {
		String input = "C:\\Users\\Eric\\Desktop\\#361D1E3F.#03E421EC.png";
		String output = "E:\\Eric\\SporeModder\\Projects\\SRNS_Effects\\animations~\\#361D1E3F.#03E421EC";
		
		BufferedImage image = ImageIO.read(new File(input));
		System.out.println(image.getType());
		write(new File(output), image, BitMapType.BIT_8);
		
		ImageIO.write(image, "PNG", new File(input + "_output.png"));
	}
}
