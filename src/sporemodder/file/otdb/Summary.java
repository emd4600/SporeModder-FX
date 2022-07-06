package sporemodder.file.otdb;

import java.io.IOException;

import sporemodder.file.filestructures.FileStream;
import sporemodder.HashManager;
import sporemodder.MainApp;

public class Summary {
	public static void main(String[] args) throws IOException
	{
		MainApp.testInit();
		
		String path = "E:\\Eric\\Eclipse Projects\\SporeModder FX\\Projects\\Player Creations\\vehiclesModelsVCL~\\0x0541DD83.summary";
		
		try (FileStream stream = new FileStream(path, "r"))
		{
			stream.readInt(); // version
			int count = stream.readInt();
			
			for (int i = 0; i < count; ++i)
			{
				int id = stream.readInt();
				int type = stream.readInt();
				
				if (type == 0x2E1A75D)
				{
					System.out.println(HashManager.get().getFileName(id) + ": " + HashManager.get().getFileName(stream.readInt()));
				}
				else if (type == 0x2E1A7FF)
				{
					System.out.println(HashManager.get().getFileName(id) + ": " + stream.readFloat());
				}
				else {
					throw new IOException("Unknown param type " + HashManager.get().getFileName(type));
				}
			}
		}
	}
}
