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
