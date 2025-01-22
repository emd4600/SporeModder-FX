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
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import sporemodder.UIManager;
import sporemodder.file.spui.SPUIRectangle;
import sporemodder.file.spui.SpuiElement;
import sporemodder.file.spui.SpuiWriter;
import sporemodder.file.spui.uidesigner.DesignerProperty;
import sporemodder.view.editors.SpuiEditor;
import sporemodder.view.editors.spui.SpuiPropertyAction;
import sporemodder.view.editors.spui.SpuiUVEditor;
import sporemodder.view.editors.spui.SpuiUndoableAction;
import sporemodder.view.inspector.InspectorReferenceLink;
import sporemodder.view.inspector.InspectorValue;
import sporemodder.view.inspector.PropertyPane;

public class AtlasImage extends SpuiElement implements ISporeImage {
	
	private static ScrollPane scrollPane;
	private static Canvas canvas;
	private static InspectorReferenceLink referenceLink;

	private DirectImage atlas;
	private final SPUIRectangle uvCoordinates = new SPUIRectangle(0, 0, 1, 1);
	private final int[] dimensions = new int[2];
	
	@Override
	public float getWidth() {
		return dimensions[0];
	}

	@Override
	public float getHeight() {
		return dimensions[1];
	}
	
	public DirectImage getAtlas() {
		return atlas;
	}

	public static AtlasImage create(DirectImage atlas, int[] dimensions, SPUIRectangle uvCoordinates) {
		AtlasImage atlasImage = new AtlasImage();
		atlasImage.atlas = atlas;
		atlasImage.dimensions[0] = dimensions[0];
		atlasImage.dimensions[1] = dimensions[1];
		atlasImage.uvCoordinates.copy(uvCoordinates);
		return atlasImage;
	}

	@Override
	public void drawImage(GraphicsContext graphics, double sx, double sy, double sw, double sh, double dx, double dy,
			double dw, double dh, Color shadeColor) {
		
		if (atlas == null) {
			graphics.setFill(Color.WHITE);
			graphics.fillRect(dx, dy, dw, dh);
		}
		else {
			double w = getWidth();
			double h = getHeight();
			double aw = atlas.getWidth();
			double ah = atlas.getHeight();
			
			// Converts width and height to [0, 1] range
			double relativeWidth = sw / w;
			double relativeHeight = sh / h;
			// Applies the UV coordinates, still in [0, 1] range but now relative to whole atlas
			relativeWidth *= uvCoordinates.getWidth();
			relativeHeight *= uvCoordinates.getHeight();
			// Multiply by atlas size to get in atlas coordinates
			sw = relativeWidth * aw;
			sh = relativeHeight * ah;
			
			// X, Y in range [0, 1] in fragment space
			double relativeX = sx / w;
			double relativeY = sy / h;
			// Now convert X, Y into atlas space, relative to image start
			relativeX = relativeX * (w / aw);
			relativeY = relativeY * (h / ah);
			// Add uvCoordinates and multiply by atlas size to convert into atlas coordinates
			sx = atlas.getWidth() * (uvCoordinates.x1 + relativeX);
			sy = atlas.getHeight() * (uvCoordinates.y1 + relativeY);
			
			atlas.drawImage(graphics, sx, sy, sw, sh, dx, dy, dw, dh, shadeColor);
		}
	}

	@Override public void addComponents(SpuiWriter writer) {
		writer.addAtlasImage(this);
		writer.addElement(this);
	}
	
	private void showImage() {
		double w = getWidth();
		double h = getHeight();
		canvas.setWidth(w);
		canvas.setHeight(h);
		canvas.getGraphicsContext2D().clearRect(0, 0, w, h);
		drawImage(canvas.getGraphicsContext2D(), 0, 0, w, h, 0, 0, w, h, Color.WHITE);
		
		canvas.translateXProperty().bind(scrollPane.widthProperty().subtract(w).divide(2.0));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Node generateUI(SpuiEditor editor) {
		PropertyPane pane = new PropertyPane();
		
		canvas = new Canvas();
		scrollPane = new ScrollPane(canvas);
		scrollPane.setPrefWidth(Double.MAX_VALUE);
		scrollPane.setMaxHeight(280);
		
		showImage();
		
		referenceLink = new InspectorReferenceLink();
		referenceLink.setValue(atlas.getLinkString());
		
		referenceLink.setOnButtonAction(event -> {
			DirectImage newValue = editor.showImageFileChooser(atlas.getKey());
			if (newValue != null) {
				DirectImage oldValue = new DirectImage(atlas);
				setAtlasValue(newValue);
				editor.repaint();
				editor.refreshTree();
				
				editor.addEditAction(new SpuiPropertyAction<DirectImage>(oldValue, newValue, 
					v ->  {
						setAtlasValue(v);
						editor.repaint();
						editor.refreshTree();
					}, 
					"Atlas Image: File"));
			}
		});
		
		DesignerProperty uvProperty = getDesignerClass().getProperty(0x01BE0002);
		DesignerProperty dimProperty = getDesignerClass().getProperty(0x01BE0003);
		
		Button uvEditorButton = new Button("UV Editor");
		uvEditorButton.setOnAction(event -> {
			SpuiUVEditor uvEditor = new SpuiUVEditor(this);
			if (UIManager.get().showDialog(uvEditor).orElse(ButtonType.CANCEL) != ButtonType.CANCEL) {
				SPUIRectangle oldUV = new SPUIRectangle(this.uvCoordinates);
				SPUIRectangle newUV = new SPUIRectangle(uvEditor.getUVCoordinates());
				int[] oldDim = copyDim(this.dimensions);
				int[] newDim = copyDim(uvEditor.getDimensions());
				
				uvCoordinates.copy(newUV);
				copyDim(dimensions, newDim);
				
				((InspectorValue<SPUIRectangle>)uvProperty.getInspectorComponents().get(0)).setValue(uvCoordinates);
				((InspectorValue<int[]>)dimProperty.getInspectorComponents().get(0)).setValue(dimensions);
				
				editor.addEditAction(new SpuiUndoableAction() {

					@Override public void undo() {
						uvCoordinates.copy(oldUV);
						copyDim(dimensions, oldDim);
						
						((InspectorValue<SPUIRectangle>)uvProperty.getInspectorComponents().get(0)).setValue(uvCoordinates);
						((InspectorValue<int[]>)dimProperty.getInspectorComponents().get(0)).setValue(dimensions);
					}

					@Override public void redo() {
						uvCoordinates.copy(newUV);
						copyDim(dimensions, newDim);
						
						((InspectorValue<SPUIRectangle>)uvProperty.getInspectorComponents().get(0)).setValue(uvCoordinates);
						((InspectorValue<int[]>)dimProperty.getInspectorComponents().get(0)).setValue(dimensions);
					}

					@Override public String getText() {
						return "AtlasImage: UV Editor";
					}
				});
				
				showImage();
				editor.repaint();
			}
		});
		
		pane.add(PropertyPane.createTitled("Preview", null, scrollPane));
		pane.add("File", null, referenceLink.getNode());
		pane.add(uvEditorButton);
		
		editor.setOnInspectorUpdateRequest(() -> showImage());
		
		getDesignerClass().generateUI(editor, pane, this);
		return pane.getNode();
	}
	
	private int[] copyDim(int[] dim) {
		return new int[] { dim[0], dim[1] };
	}
	
	private void copyDim(int[] dest, int[] source) {
		dest[0] = source[0];
		dest[1] = source[1];
	}
	
	private void setAtlasValue(DirectImage value) {
		atlas.copy(value);
		showImage();
		if (referenceLink != null) referenceLink.setValue(value.getLinkString());
	}

	public void setAtlas(DirectImage atlas) {
		this.atlas = atlas;
	}

	public SPUIRectangle getUVCoordinates() {
		return uvCoordinates;
	}
	
	public int[] getDimensions() {
		return dimensions;
	}
}
