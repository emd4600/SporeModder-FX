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
package sporemodder.file.spui.components;

import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import sporemodder.HashManager;
import sporemodder.file.ResourceKey;
import sporemodder.file.spui.InspectableObject;
import sporemodder.file.spui.SpuiWriter;
import sporemodder.view.editors.SpuiEditor;
import sporemodder.view.editors.spui.SpuiPropertyAction;
import sporemodder.view.inspector.InspectorReferenceLink;
import sporemodder.view.inspector.PropertyPane;

public class DirectImage extends InspectableObject implements ISporeImage {
	
	private static InspectorReferenceLink referenceLink;
	private ImageView imageView;
	private ScrollPane scrollPane;
	
	private final ResourceKey key;
	private Image image;
	
	public DirectImage(Image image, ResourceKey key) {
		this.image = image;
		this.key = key;
	}
	
	public DirectImage(DirectImage other) {
		key = new ResourceKey();
		copy(other);
	}
	
	public void copy(DirectImage other) {
		this.image = other.image;
		this.key.copy(other.key);
	}
	
	@Override public String toString() {
		//return "Image: " + key.toString();
		return "Image: " + getLinkString();
	}
	
	public ResourceKey getKey() {
		return key;
	}
	
	public void setKey(ResourceKey key) {
		this.key.copy(key);
	}
	
	public Image getImage() {
		return image;
	}
	
	public void setImage(Image image) {
		this.image = image;
	}

	@Override
	public float getWidth() {
		return image == null ? 1 : (float) image.getWidth();
	}

	@Override
	public float getHeight() {
		return image == null ? 1 : (float) image.getHeight();
	}

	@Override
	public void drawImage(GraphicsContext graphics, double sx, double sy, double sw, double sh, double dx, double dy,
			double dw, double dh, Color shadeColor) {

		/*ColorAdjust tint = new ColorAdjust();
		double hueVal = shadeColor.getHue();
		while (hueVal > 360)
			hueVal -= 360;
		while (hueVal < 0)
			hueVal += 360;
		
		hueVal += 180;
		
		tint.setHue(map(hueVal, 0, 360, -1, 1));
		tint.setSaturation(shadeColor.getSaturation());
		tint.setBrightness(map(shadeColor.getBrightness(), 0, 1, -1, 0));*/
		
		if (image == null) {
			graphics.setFill(shadeColor);
			graphics.fillRect(dx, dy, dw, dh);
		} else if ((shadeColor.getRed() >= 1)
				&& (shadeColor.getGreen() >= 1)
				&& (shadeColor.getBlue() >= 1)
				&& (shadeColor.getOpacity() >= 1)
				) {
			//System.out.println("DRAWING WITHOUT SHADECOLOR");
			graphics.drawImage(image, sx, sy, sw, sh, dx, dy, dw, dh);
		} else {
			//System.out.println("DRAWING WITH SHADECOLOR");
			int imgWidth = (int)image.getWidth();
			int imgHeight = (int)image.getHeight();
			//System.out.println("IMAGE DIMENSIONS: " + imgWidth + ", " + imgHeight);
			/*int targetWidth = (int)dw;
			int destHeight = (int)dh;*/
			int sourceX = (int)sx;
			int sourceY = (int)sy;
			int sourceRight = (int)sw + sourceX;
			int sourceBottom = (int)sh + sourceY;
			
			PixelReader reader = image.getPixelReader();
			WritableImage wrImage = new WritableImage(imgWidth, imgHeight);
			PixelWriter writer = wrImage.getPixelWriter();
			for (int x = 0; x < sourceRight; x++) {
				for (int y = 0; y < sourceBottom; y++) {
					//Begin awkward nesting (looks dumb but helps performance)
					int safeSourceX = sourceX + x;
					if ((safeSourceX >= 0) && (safeSourceX < imgWidth)) {
						int safeSourceY = sourceY + y;
						if ((safeSourceY >= 0) && (safeSourceY < imgHeight)) {
							int safeDestX = x;
							if (safeDestX >= 0 && (safeDestX < imgWidth)) {
								int safeDestY = y;
								if ((safeDestY >= 0) && (safeDestY < imgHeight)) {
									//End awkward nesting
									Color initialColor = reader.getColor(safeSourceX, safeSourceY); //(int)(sx + x), (int)(sx + y));
									double red = shadeColor.getRed();
									double green = shadeColor.getGreen();
									double blue = shadeColor.getBlue();
									double alpha = shadeColor.getOpacity();
									writer.setColor(safeSourceX, safeSourceY, new Color(initialColor.getRed() * red,
											initialColor.getGreen() * green,
											initialColor.getBlue() * blue,
											initialColor.getOpacity() * alpha));
								}
							}
						}
					}
				}
			}
			graphics.drawImage(wrImage, sx, sy, sw, sh, dx, dy, dw, dh);
		}
	}
	
	private void setImageView(ImageView imageView, ScrollPane scrollPane) {
		imageView.setImage(image);
		if (image != null) {
			imageView.translateXProperty().bind(scrollPane.widthProperty().subtract(image.getWidth()).divide(2.0));
		}
	}

	@Override
	public Node generateUI(SpuiEditor editor) {
		PropertyPane pane = new PropertyPane();
		
		imageView = new ImageView();
		scrollPane = new ScrollPane(imageView);
		scrollPane.setPrefWidth(Double.MAX_VALUE);
		scrollPane.setMaxHeight(280);
		
		setImageView(imageView, scrollPane);
		
		pane.add(PropertyPane.createTitled("Preview", null, scrollPane));
		
		
		referenceLink = new InspectorReferenceLink();
		referenceLink.setValue(getLinkString());
		
		referenceLink.setOnButtonAction(event -> {
			DirectImage newValue = editor.showImageFileChooser(key);
			if (newValue != null) {
				DirectImage oldValue = new DirectImage(this);
				setValue(newValue);
				editor.repaint();
				editor.refreshTree();
				
				editor.addEditAction(new SpuiPropertyAction<DirectImage>(oldValue, newValue, 
					v ->  {
						setValue(v);
						editor.repaint();
						editor.refreshTree();
					}, 
					"Image: File"));
			}
		});
		
		pane.add("File", null, referenceLink.getNode());
		pane.add("Width", null, new Label(getWidth() + " px"));
		pane.add("Height", null, new Label(getHeight() + " px"));
		
		return pane.getNode();
	}
	
	private void setValue(DirectImage value) {
		copy(value);
		if (imageView != null) setImageView(imageView, scrollPane);
		if (referenceLink != null) referenceLink.setValue(getLinkString());
	}

	@Override
	public void addComponents(SpuiWriter writer) {
		writer.addImage(this);
	}

	// Showing the whole key takes too much space, so omit the folder
	public String getLinkString() {
		return HashManager.get().getFileName(key.getInstanceID()) + '.' + HashManager.get().getTypeName(key.getTypeID());
	}
}
