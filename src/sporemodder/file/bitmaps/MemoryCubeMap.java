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
import java.io.IOException;

import javax.imageio.ImageIO;

import emord.filestructures.FileStream;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MemoryCubeMap extends Application {
	
	private static final int SIZE = 512;
	private static final int COUNT = 6;
	private static final int MAX_INDEX = COUNT - 1;
	private static final double DIV = Math.pow(2, 16) - 1;
	
	private double getData(int[] data, int face, int x, int y) {
		int index = (y + SIZE*face) * SIZE + x;
		return data[index] / DIV;
	}
	
	private Image[] readImage(File file) throws IOException {
		try (FileStream stream = new FileStream(file, "r")) {
			
			WritableImage[] images = new WritableImage[COUNT];
			PixelWriter[] dst = new PixelWriter[COUNT];
			for (int i = 0; i < COUNT; ++i) {
				images[i] = new WritableImage(SIZE, SIZE);
				dst[i] = images[i].getPixelWriter();
			}
			
			int[] data = new int[SIZE*SIZE*COUNT];
			stream.readLEUShorts(data);
			
			for (int k = 0; k < COUNT; ++k) {
				for (int i = 0; i < SIZE; ++i) {
					for (int j = 0; j < SIZE; ++j) {
						dst[k].setColor(i, j, Color.gray(getData(data, k, i, j)));
					}
				}
			}
			
			return images;
		}
	}
	
	private int index;

	@Override
	public void start(Stage primaryStage) throws Exception {
		File file = new File("C:\\Users\\Eric\\Desktop\\terrain_heightMap.raw");
		
		index = 0;
		Image[] images = readImage(file);
		ImageView imageView = new ImageView(images[index]);
		
		imageView.setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.SECONDARY) {
				if (index < MAX_INDEX) ++index;
			} else {
				if (index > 0) --index;
			}
			
			primaryStage.setTitle("Tile: " + index);
			imageView.setImage(images[index]);
		});
		
		Scene scene = new Scene(new Pane(imageView), SIZE, SIZE);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Tile: " + index);
		primaryStage.show();
		
		File dstFolder = new File("C:\\Users\\Eric\\Desktop\\TerrainMaps\\");
		for (int i = 0; i < images.length; ++i) {
			File dst = new File(dstFolder, "HeightMap-" + i + ".png");
			ImageIO.write(SwingFXUtils.fromFXImage(images[i], null), "PNG", dst);
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
