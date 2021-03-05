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
package sporemodder.file.spui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import sporemodder.FileManager;
import sporemodder.UIManager;
import sporemodder.file.ResourceKey;
import sporemodder.file.spui.components.DirectImage;
import sporemodder.view.editors.SpuiEditor;
import sporemodder.view.editors.spui.SpuiPropertyAction;
import sporemodder.view.inspector.InspectorReferenceLink;
import sporemodder.view.inspector.PropertyPane;

public class RLEHitMask extends InspectableObject {
	
	private static ScrollPane scrollPane;
	private static Canvas previewCanvas;
	private static Image fileImage;
	private static InspectorReferenceLink referenceLink;
	
	private int width;
	private int height;
	private int[] rleData;
	// Always in version < 3, optional in other versions
	private ResourceKey fileKey;
	private boolean useRLEHitMask = true;
	
	@Override public String toString() {
		return "HitMask";
	}
	
	public void read(StreamReader in, int version) throws IOException {
		useRLEHitMask = false;
		if (version >= 3) {
			useRLEHitMask = in.readBoolean();
			if (useRLEHitMask) {
				width = in.readLEInt();
				height = in.readLEInt();
				rleData = new int[in.readLEInt()];
				in.readLEUShorts(rleData);
			}
		}
		// if version < 3 or not useRLEHitMask
		if (!useRLEHitMask) {
			fileKey = new ResourceKey();
			fileKey.readLE(in);
		}
	}
	
	public void write(StreamWriter out, int version) throws IOException {
		if (version >= 3) {
			out.writeBoolean(useRLEHitMask);
			if (useRLEHitMask) {
				out.writeLEInt(width);
				out.writeLEInt(height);
				out.writeLEInt(rleData.length);
				out.writeLEUShorts(rleData);
			}
			else {
				fileKey.writeLE(out);
			}
		}
		else {
			if (fileKey == null) {
				fileKey = new ResourceKey();
			}
			fileKey.writeLE(out);
		}
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int[] getRleData() {
		return rleData;
	}

	public void setRleData(int[] rleData) {
		this.rleData = rleData;
	}

	public ResourceKey getFileKey() {
		return fileKey;
	}

	public void setFileKey(ResourceKey fileKey) {
		this.fileKey = fileKey;
	}

	@Override public void addComponents(SpuiWriter writer) {
	}
	
	private boolean isOpaque(PixelReader pixels, int x, int y) {
		return (((pixels.getArgb(x, y) & 0xFF000000) >> 24) & 0xFF) >= 128;
	}
	
	private void importFromImage(Image image) {
		width = (int) image.getWidth();
		height = (int) image.getHeight();
		PixelReader pixels = image.getPixelReader();
		List<Integer> rle = new ArrayList<>();
		
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (isOpaque(pixels, j, i)) {
					
					rle.add(i * width + j);
					
					while (j+1 < width && isOpaque(pixels, j, i)) {
						j++;
					}
					
					rle.add(i * width + j);
				}
			}
		}
		
		rleData = new int[rle.size()];
		
		for (int i = 0; i < rleData.length; i++) {
			rleData[i] = rle.get(i);
		}
		
		paintHitMask();
	}
	
	@Override
	public Node generateUI(SpuiEditor editor) {
		PropertyPane pane = new PropertyPane();
		
		Button buttonImport = new Button("Import");
		buttonImport.setOnAction(event -> {
			FileChooser chooser = new FileChooser();
			chooser.getExtensionFilters().addAll(FileManager.FILEFILTER_ALL, FileManager.FILEFILTER_PNG);
			File input = chooser.showOpenDialog(UIManager.get().getScene().getWindow());
			if (input != null) {
				importFromImage(new Image(input.toURI().toString()));
			}
		});
		
		Button buttonExport = new Button("Export");
		buttonExport.setOnAction(event -> {
			FileChooser chooser = new FileChooser();
			chooser.getExtensionFilters().addAll(FileManager.FILEFILTER_ALL, FileManager.FILEFILTER_PNG);
			File output = chooser.showSaveDialog(UIManager.get().getScene().getWindow());
			if (output != null) {
				try {
					ImageIO.write(SwingFXUtils.fromFXImage(previewCanvas.snapshot(null, null), null), "PNG", output);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		HBox buttonsPane = new HBox(5);
		buttonsPane.getChildren().addAll(buttonImport, buttonExport);
		
		referenceLink = new InspectorReferenceLink();
		referenceLink.setValue(fileKey);
		
		referenceLink.setOnButtonAction(event -> {
			DirectImage newValue = editor.showImageFileChooser(fileKey);
			if (newValue != null) {
				ResourceKey oldValue = new ResourceKey(fileKey);
				setFileValue(newValue.getKey());
				editor.repaint();
				editor.refreshTree();
				
				editor.addEditAction(new SpuiPropertyAction<ResourceKey>(oldValue, newValue.getKey(), 
					v ->  {
						setFileValue(v);
						editor.repaint();
						editor.refreshTree();
					}, 
					"HitMask: Image"));
			}
		});
		
		ComboBox<String> cbType = new ComboBox<>();
		cbType.getItems().addAll("RLE", "Image");
		cbType.getSelectionModel().select(useRLEHitMask ? "RLE" : "Image");
		cbType.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			useRLEHitMask = "RLE".equals(newValue);
			paintHitMask();
			
			referenceLink.getNode().setVisible(!useRLEHitMask);
			buttonsPane.setVisible(useRLEHitMask);
			
			editor.addEditAction(new SpuiPropertyAction<Boolean>(!useRLEHitMask, useRLEHitMask, 
					v ->  {
						useRLEHitMask = v;
						paintHitMask();
					}, 
					"HitMask: Type"));
		});
		
		referenceLink.getNode().setVisible(!useRLEHitMask);
		buttonsPane.setVisible(useRLEHitMask);
		
		previewCanvas = new Canvas();
		scrollPane = new ScrollPane(previewCanvas);
		scrollPane.setPrefWidth(Double.MAX_VALUE);
		scrollPane.setMaxHeight(280);
		
		
		pane.add("Type", null, cbType);
		pane.add(referenceLink.getNode());
		pane.add(buttonsPane);
		pane.add(PropertyPane.createTitled("Preview", null, scrollPane));
		
		paintHitMask();
		
		return pane.getNode();
	}
	
	private void setFileValue(ResourceKey value) {
		if (fileKey == null) {
			fileKey = new ResourceKey();
		}
		fileKey.copy(value);
		paintHitMask();
		if (referenceLink != null) referenceLink.setValue(fileKey);
	}
	
	private void paintHitMask() {
		double canvasWidth = previewCanvas.getWidth();
		
		if (useRLEHitMask) {
			canvasWidth = width;
			previewCanvas.setWidth(canvasWidth);
			previewCanvas.setHeight(height);
			
			GraphicsContext	graphics = previewCanvas.getGraphicsContext2D();
			graphics.setFill(Color.WHITE);
			graphics.fillRect(0, 0, width, height);
			
			graphics.setFill(Color.BLACK);
			if (rleData != null) {
				for (int i = 0; i < rleData.length; i+=2) {
					graphics.strokeLine(
							rleData[i] % canvasWidth, 
							rleData[i] / canvasWidth, 
							rleData[i + 1] % canvasWidth, 
							rleData[i] / canvasWidth);
				}
			}
		}
		else if (fileImage != null) {
			canvasWidth = fileImage.getWidth();
			double height = fileImage.getHeight();
			previewCanvas.setWidth(canvasWidth);
			previewCanvas.setHeight(height);
			
			previewCanvas.getGraphicsContext2D().drawImage(fileImage, 0, 0);
		}
		
		previewCanvas.translateXProperty().bind(scrollPane.widthProperty().subtract(canvasWidth).divide(2.0));
	}
}
