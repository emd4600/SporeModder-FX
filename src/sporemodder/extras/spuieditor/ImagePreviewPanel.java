package sporemodder.extras.spuieditor;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import sporemodder.extras.spuieditor.components.Image;

public class ImagePreviewPanel extends JPanel {
	
	private Image image;
	private float zoomLevel = 1.0f;

	public ImagePreviewPanel(Image image) {
		this.image = image;
	}

	@Override
	public Dimension getPreferredSize() {
		if (image == null || image.getResource() == null || image.getBufferedImage() == null) {
			return super.getPreferredSize();
		}
		Dimension dim = image.getDimensions();
		
		return new Dimension(Math.round(dim.width * zoomLevel), Math.round(dim.height * zoomLevel));
	}
	
	public void setZoomLevel(float zoomLevel) {
		this.zoomLevel = zoomLevel;
		
		revalidate();  // update parent scrollPane
		repaint();
	}

	public float getZoomLevel() {
		return zoomLevel;
	}
	
	public void setImage(Image image) {
		this.image = image;
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (!Image.isValid(image)) {
			return;
		}
		
		Dimension dimensions = image.getDimensions();
		float[] uvCoords = image.getUVCoords();
		
		Dimension panelSize = getSize();
 		int width = Math.round(dimensions.width * zoomLevel);
 		int height = Math.round(dimensions.height * zoomLevel);
 		int posX = (panelSize.width - width) / 2;
 		int posY = (panelSize.height - height) / 2;
 		
 		int imageWidth = image.getBufferedImage().getWidth();
 		int imageHeight = image.getBufferedImage().getHeight();
 		
 		Graphics2D g2 = (Graphics2D)g;
 		Image.drawImage(g2, image.getBufferedImage(), posX, posY, posX + width, posY + height, 
 				Math.round(uvCoords[0] * imageWidth), 
				Math.round(uvCoords[1] * imageHeight), 
				Math.round(uvCoords[2] * imageWidth), 
				Math.round(uvCoords[3] * imageHeight));
 		
	}
}
