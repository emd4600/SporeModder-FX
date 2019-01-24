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

import javafx.scene.canvas.GraphicsContext;
import sporemodder.file.spui.SPUIRectangle;
import sporemodder.file.spui.SporeUserInterface;

/**
 * Corresponds to the designer 'image' type, an object that holds an image. This can either be a portion of an atlas image,
 * or a full image file.
 */
public interface ISporeImage {
	
	public abstract float getWidth();
	public abstract float getHeight();
	
	/**
	 * Draws the specified source rectangle of the given image to the given destination rectangle. 
	 * A null image value or an image still in progress will be ignored.
	 * @param graphics 
	 * @param sx The source rectangle's X coordinate position.
	 * @param sy The source rectangle's Y coordinate position.
	 * @param sw The source rectangle's width.
	 * @param sh The source rectangle's height.
	 * @param dx The destination rectangle's X coordinate position.
	 * @param dy The destination rectangle's Y coordinate position.
	 * @param dw The destination rectangle's width.
	 * @param dh The destination rectangle's height.
	 */
	public abstract void drawImage(GraphicsContext graphics, 
			double sx, double sy, double sw, double sh, 
			double dx, double dy, double dw, double dh);
	
	public static void drawImage(GraphicsContext graphics, ISporeImage image, SPUIRectangle destArea) {
		image.drawImage(graphics, 
				0, 0, image.getWidth(), image.getHeight(),
				destArea.x1, destArea.y1, destArea.getWidth(), destArea.getHeight());
	}
	
	/**
	 * Draws the given image, repeating it horizontally/vertically until it fits all the required space.
	 * @param graphics
	 * @param image
	 * @param bounds
	 */
	public static void drawImageTiled(GraphicsContext graphics, ISporeImage image, SPUIRectangle bounds) {
		drawImageTiled(graphics, image, new SPUIRectangle(0, 0, image.getWidth(), image.getHeight()), bounds);
	}
	
	//TODO use alignment
	public static void drawImageTiled(GraphicsContext graphics, ISporeImage image, SPUIRectangle sourceBounds, SPUIRectangle bounds) {
		double width = sourceBounds.getWidth();
		double height = sourceBounds.getHeight();
		double currentY = bounds.y1;
		
		while (currentY < bounds.y2) {
			// Crop the image if it falls out of the bounds
			double tileHeight = Math.min(bounds.y2 - currentY, height);
			double currentX = bounds.x1;
			
			while (currentX < bounds.x2) {
				// Crop the image if it falls out of the bounds
				double tileWidth = Math.min(bounds.x2 - currentX, width);
				
				image.drawImage(graphics, sourceBounds.x1, sourceBounds.y1, tileWidth, tileHeight, currentX, currentY, tileWidth, tileHeight);
				
				currentX += tileWidth;
			}
			
			currentY += tileHeight;
		}
	}
	
	public static void drawImageSliced(GraphicsContext graphics, ISporeImage image, boolean isTiled, 
			SPUIRectangle area, Borders scaleArea) {
		drawImageSliced(graphics, image, isTiled, new SPUIRectangle(0, 0, image.getWidth(), image.getHeight()),
				area, scaleArea);
	}
	
	public static void drawImageSliced(GraphicsContext graphics, ISporeImage image, boolean isTiled, 
			SPUIRectangle sourceArea, SPUIRectangle area, Borders scaleArea) {
		
		SPUIRectangle center = drawSlicedEdges(graphics, image, isTiled, sourceArea, area, scaleArea);
		
		float w = sourceArea.getWidth();
		float h = sourceArea.getHeight();

		SPUIRectangle sourceBounds = new SPUIRectangle();
		sourceBounds.x1 = sourceArea.x1 + scaleArea.left*w;
		sourceBounds.y1 = sourceArea.y1 + scaleArea.top*h;
		sourceBounds.x2 = sourceArea.x1 + w - scaleArea.right*w;
		sourceBounds.y2 = sourceArea.y1 + h - scaleArea.bottom*h;
		
		if (isTiled) {
			ISporeImage.drawImageTiled(graphics, image, sourceBounds, center);
		}
		else {
			// Just stretch the remaining image through all the remaining area
			image.drawImage(graphics, 
					sourceBounds.x1, sourceBounds.y1, sourceBounds.getWidth(), sourceBounds.getHeight(),
					center.x1, center.y1, center.getWidth(), center.getHeight());
		}
	}
	
	static SPUIRectangle drawSlicedEdges(GraphicsContext graphics, ISporeImage image, 
			boolean isTiled, SPUIRectangle sourceArea, SPUIRectangle area, Borders scaleArea) {
		
		float x = sourceArea.x1;
		float y = sourceArea.y1;
		float w = sourceArea.getWidth();
		float h = sourceArea.getHeight();
		float t = scaleArea.top;
		float l = scaleArea.left;
		float r = scaleArea.right;
		float b = scaleArea.bottom;
		
		SPUIRectangle center = new SPUIRectangle();
		center.x1 = area.x1 + l*w;
		center.y1 = area.y1 + t*h;
		center.x2 = area.x2 - r*w;
		center.y2 = area.y2 - b*h;
		
		/* --  Draw top-left corner -- */
		image.drawImage(graphics, 
				x, y, x + l*w, y + t*h, 
				area.x1, area.y1, center.x1-area.x1, center.y1-area.y1);
		
		/* --  Draw top-right corner -- */
		image.drawImage(graphics, 
				x + (1 - l)*w, y, x + r*w, y + t*h, 
				center.x2, area.y1, area.x2-center.x2, center.y1-area.y1);
		
		/* --  Draw bottom-left corner -- */
		image.drawImage(graphics, 
				x + 0, y + (1 - b)*h, x + l*w, y + b*h, 
				area.x1, center.y2, center.x1-area.x1, area.y2-center.y2);
		
		/* --  Draw bottom-right corner -- */
		image.drawImage(graphics, 
				x + (1 - l)*w, y + (1 - b)*h, x + r*w, y + b*h, 
				center.x2, center.y2, area.x2-center.x2, area.y2-center.y2);
		
		
		SPUIRectangle sourceBounds = new SPUIRectangle();
		SPUIRectangle destBounds = new SPUIRectangle();
		
		if (center.getWidth() > 0) {
			/* -- Draw top edge -- */
			sourceBounds.x1 = x + l*w;
			sourceBounds.y1 = y + 0;
			sourceBounds.x2 = x + w - r*w;
			sourceBounds.y2 = y + t*h;
			destBounds.x1 = center.x1;
			destBounds.y1 = area.y1;
			destBounds.x2 = center.x2;
			destBounds.y2 = center.y1;
			ISporeImage.drawImage(graphics, image, isTiled, sourceBounds, destBounds);
			
			/* -- Draw bottom edge -- */
			sourceBounds.x1 = x + l*w;
			sourceBounds.y1 = y + h - b*h;
			sourceBounds.x2 = x + w - r*w;
			sourceBounds.y2 = y + h;
			destBounds.x1 = center.x1;
			destBounds.y1 = center.y2;
			destBounds.x2 = center.x2;
			destBounds.y2 = area.y2;
			ISporeImage.drawImage(graphics, image, isTiled, sourceBounds, destBounds);
		}
		
		if (center.getHeight() > 0) {
			/* -- Draw left edge -- */
			sourceBounds.x1 = x + 0;
			sourceBounds.y1 = y + t*h;
			sourceBounds.x2 = x + l*w;
			sourceBounds.y2 = y + h - b*h;
			destBounds.x1 = area.x1;
			destBounds.y1 = center.y1;
			destBounds.x2 = center.x1;
			destBounds.y2 = center.y2;
			ISporeImage.drawImage(graphics, image, isTiled, sourceBounds, destBounds);
			
			/* -- Draw right edge -- */
			sourceBounds.x1 = x + w - r*w;
			sourceBounds.y1 = y + t*h;
			sourceBounds.x2 = x + w;
			sourceBounds.y2 = y + h - b*h;
			destBounds.x1 = center.x2;
			destBounds.y1 = center.y1;
			destBounds.x2 = area.x2;
			destBounds.y2 = center.y2;
			ISporeImage.drawImage(graphics, image, isTiled, sourceBounds, destBounds);
		}
		
		return center;
	}
	
	public static void drawImage(GraphicsContext graphics, ISporeImage image, boolean isTiled,
			SPUIRectangle sourceBounds, SPUIRectangle destBounds) {
		
		if (isTiled) {
			ISporeImage.drawImageTiled(graphics, image, sourceBounds, destBounds);
		} else {
			image.drawImage(graphics, 
					sourceBounds.x1, sourceBounds.y1, sourceBounds.getWidth(), sourceBounds.getHeight(), 
					destBounds.x1, destBounds.y1, destBounds.getWidth(), destBounds.getHeight());
		}
	}
	
	public static SPUIRectangle getTileArea(IWindow window, ISporeImage image, int tileCount) {
		int tileIndex = SporeUserInterface.getTileIndex(window);
		float imageWidth = image.getWidth();

		SPUIRectangle sourceBounds = new SPUIRectangle();
		sourceBounds.setWidth(imageWidth / tileCount);
		sourceBounds.setHeight(image.getHeight());
		
		sourceBounds.translateX(tileIndex * imageWidth/tileCount);
		
		return sourceBounds;
	}
}
