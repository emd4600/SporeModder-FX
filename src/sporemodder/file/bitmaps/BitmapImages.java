/****************************************************************************
* Copyright (C) 2019 Eric Mor
*
* This file is part of SporeModder FX.
*
* SporeModder FX is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
****************************************************************************/
package sporemodder.file.bitmaps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import emord.filestructures.FileStream;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class BitmapImages extends Application {
	
	private Image readImage(File file) throws FileNotFoundException, IOException {
		try (FileStream stream = new FileStream(file, "r")) {
			// Magic, 0
			stream.readInt();
			int width = stream.readInt();
			int height = stream.readInt();
			int channelCount = stream.readInt();
			// bufferSize
			stream.readInt();
			
			WritableImage image = new WritableImage(width, height);
			PixelWriter dst = image.getPixelWriter();
			for (int j = 0; j < height; ++j) {
				for (int i = 0; i < width; ++i) {
					dst.setColor(i, j, Color.color(
							stream.readUShort() / 255.0,
							stream.readUShort() / 255.0,
							stream.readUShort() / 255.0));
				}
			}
			
			return image;
		}
	}
	
	@Override public void start(Stage primaryStage) throws Exception {
		File file = new File("C:\\Users\\Eric\\Desktop\\#7D328FDD.48bitImage");
		
		Image image = readImage(file);
		ImageView imageView = new ImageView(image);
		
		Scene scene = new Scene(new Pane(imageView), image.getWidth(), image.getHeight());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
