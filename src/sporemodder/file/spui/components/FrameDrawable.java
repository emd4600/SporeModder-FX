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
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import sporemodder.file.spui.SPUIRectangle;
import sporemodder.file.spui.SporeUserInterface;
import sporemodder.file.spui.SpuiViewer;

public class FrameDrawable extends IDrawable {
	
	private final FrameStyle[] style = new FrameStyle[8];
	private final Borders borderWidth = new Borders();

	@Override public void paint(IWindow window, SpuiViewer viewer) {
		FrameStyle border = style[SporeUserInterface.getImageIndex(window)];
		if (border == null) border = style[0];
		
		if (border == null) {
			return;
		}
		
		Borders finalSizes = new Borders();
		finalSizes.left = borderWidth.left;
		finalSizes.top = borderWidth.top;
		finalSizes.right = borderWidth.right;
		finalSizes.bottom = borderWidth.bottom;
		
		SPUIRectangle bounds = window.getRealArea();
		SPUIRectangle drawBounds = new SPUIRectangle(bounds);

		if (finalSizes.left + finalSizes.right > bounds.getWidth()) {
			int newSize = (int) (bounds.getWidth() * 0.5f);
			
			finalSizes.left = finalSizes.left > newSize ? newSize : (int) finalSizes.left;
			finalSizes.right = bounds.getWidth() - finalSizes.left;
		}
		
		if (finalSizes.top + finalSizes.bottom > bounds.getHeight()) {
			int newSize = (int) (bounds.getHeight() * 0.5f);
			
			finalSizes.top = finalSizes.top > newSize ? newSize : (int) finalSizes.top;
			finalSizes.bottom = bounds.getHeight() - finalSizes.top;
		}
		
		if (border.borderType == FrameStyle.BORDER_INSET_LINE) {
			drawBounds.x1 += 1;
			drawBounds.y1 += 1;
			drawBounds.x2 -= 1;
			drawBounds.y2 -= 1;
		}
		
		Color color = border.color;
		GraphicsContext graphics = viewer.getGraphicsContext2D();
		
		switch(border.borderType) {
		case FrameStyle.BORDER_DEFAULT:
		case FrameStyle.BORDER_SOLID:
			drawSolidBorder(graphics, finalSizes, drawBounds, color);
			break;
			
		case FrameStyle.BORDER_DOTTED:
			drawDashedBorder(graphics, finalSizes, drawBounds, color, 1.0f);
			break;
			
		case FrameStyle.BORDER_DASHED:
			drawDashedBorder(graphics, finalSizes, drawBounds, color, 3.0f);
			break;
			
		case FrameStyle.BORDER_GROOVE:
			drawBevelBorder(graphics, finalSizes, drawBounds, brighten(color, 0.6f), darken(color, 0.2f));
			break;
			
		case FrameStyle.BORDER_RIDGE:
			drawBevelBorder(graphics, finalSizes, drawBounds, darken(color, 0.2f), brighten(color, 0.6f));
			break;
			
		case FrameStyle.BORDER_INSET:
			drawGradientBorder(graphics, finalSizes, drawBounds, color, darken(color, 0.6f), brighten(color, 0.6f), color);
			break;
			
		case FrameStyle.BORDER_OUTSET:
			drawGradientBorder(graphics, finalSizes, drawBounds, color, brighten(color, 0.6f), darken(color, 0.6f), color);
			break;
			
		case FrameStyle.BORDER_INSET_LINE:
			drawGradientBorder(graphics, finalSizes, drawBounds, color, darken(color, 0.6f), brighten(color, 0.6f), color);
			graphics.setFill(Color.BLACK);
			graphics.setLineCap(StrokeLineCap.SQUARE);
			graphics.setLineJoin(StrokeLineJoin.MITER);
			graphics.setLineWidth(1.0);
			graphics.strokeRect(
					drawBounds.x1 + finalSizes.left, drawBounds.y1 + finalSizes.top, 
					drawBounds.getWidth() - finalSizes.left - finalSizes.right, drawBounds.getHeight() - finalSizes.top - finalSizes.bottom);
			break;
			
		case FrameStyle.BORDER_OUTSET_LINE:
			drawGradientBorder(graphics, finalSizes, drawBounds, color, brighten(color, 0.6f), darken(color, 0.6f), color);
			graphics.setFill(Color.BLACK);
			graphics.setLineCap(StrokeLineCap.SQUARE);
			graphics.setLineJoin(StrokeLineJoin.MITER);
			graphics.setLineWidth(1.0);
			graphics.strokeRect(drawBounds.x1, drawBounds.y1, drawBounds.getWidth(), drawBounds.getHeight());
			break;
		}
		
		// Reset these attributes
		graphics.setLineCap(StrokeLineCap.SQUARE);
		graphics.setLineJoin(StrokeLineJoin.MITER);
		graphics.setLineWidth(1.0);
	}
	
	private static void drawGradientBorder(GraphicsContext graphics, Borders borderSizes, SPUIRectangle drawBounds, 
			Color firstGradient1, Color firstGradient2, Color secondGradient1, Color secondGradient2) {
		
		int leftWidth = Math.round(borderSizes.left);
		int topWidth = Math.round(borderSizes.top);
		int bottomWidth = Math.round(borderSizes.bottom);
		int rightWidth = Math.round(borderSizes.right);
		
		double x = drawBounds.x1;
		double y = drawBounds.y1;
		double width = drawBounds.getWidth();
		double height = drawBounds.getHeight();
		
		Stop[] firstStops = new Stop[] { new Stop(0, firstGradient1), new Stop(1, firstGradient2)};
		Stop[] secondStops = new Stop[] { new Stop(0, secondGradient1), new Stop(1, secondGradient2)};
		
		// Top
		graphics.setFill(new LinearGradient(
				x + width/2, y, x + width/2, y + topWidth, false, CycleMethod.NO_CYCLE, firstStops));
		
		graphics.fillPolygon(
				new double[] {x, x + leftWidth, x + width, x + width}, 
				new double[] {y, y + topWidth, y + topWidth, y}, 4);
		
		// Left
		graphics.setFill(new LinearGradient(
				x, y + height/2, x + leftWidth, y + height/2, false, CycleMethod.NO_CYCLE, firstStops));
		
		graphics.fillPolygon(
				new double[] {x, x + leftWidth, x + leftWidth, x}, 
				new double[] {y, y + topWidth, y + height - bottomWidth, y + height}, 4);
		
		// Bottom
		graphics.setFill(new LinearGradient(
				x + width/2, y + height, x + width/2, y + height - bottomWidth, false, CycleMethod.NO_CYCLE, secondStops));
		
		graphics.fillPolygon(
				new double[] {x, x + leftWidth, x + width - rightWidth, x + width}, 
				new double[] {y+height, y+height-bottomWidth, y+height-bottomWidth, y+height}, 4);
		
		// Right
		graphics.setFill(new LinearGradient(
				x+width, y+height/2, x+width-rightWidth, y+height/2, false, CycleMethod.NO_CYCLE, secondStops));
		
		graphics.fillPolygon(
				new double[] {x+width, x+width-rightWidth, x+width-rightWidth, x+width}, 
				new double[] {y, y+topWidth, y+height-bottomWidth, y+height}, 4);
	}
	
	private static void drawBevelBorder(GraphicsContext graphics, Borders borderSizes, SPUIRectangle drawBounds, Color firstColor, Color secondColor) {
		int left = Math.round(borderSizes.left);
		int top = Math.round(borderSizes.top);
		int bottom = Math.round(borderSizes.bottom);
		int right = Math.round(borderSizes.right);
		
		int leftHalf = (int) (borderSizes.left / 2.0f);
		int leftHalf2 = left - leftHalf;
		int rightHalf = (int) (borderSizes.right / 2.0f);
		int rightHalf2 = right - rightHalf;
		int topHalf = (int) (borderSizes.top / 2.0f);
		int bottomHalf = (int) (borderSizes.bottom / 2.0f);
		int bottomHalf2 = bottom - bottomHalf;
		
		double x = drawBounds.x1;
		double y = drawBounds.y1;
		double width = drawBounds.getWidth();
		double height = drawBounds.getHeight();
		
		graphics.setFill(firstColor);
		
		// Bottom bottom
		graphics.fillPolygon(
				new double[] {x, x+leftHalf, x+width-leftHalf2, x+width}, 
				new double[] {y+height, y+height-bottomHalf2, y+height-bottomHalf2, y+height}, 4);
		
		// Right right
		graphics.fillPolygon(
				new double[] {x+width, x+width-rightHalf2, x+width-rightHalf2, x+width}, 
				new double[] {y+height, y+height-bottomHalf2, y+topHalf, y}, 4);
		
		// Top bottom 
		graphics.fillPolygon(
				new double[] {x+leftHalf, x+left, x+width-right, x+width-rightHalf2}, 
				new double[] {y+topHalf, y+top, y+top, y+topHalf}, 4);
		
		// Left right
		graphics.fillPolygon(
				new double[] {x+leftHalf, x+left, x+left, x+leftHalf}, 
				new double[] {y+topHalf, y+top, y+height-bottom, y+height-bottomHalf2}, 4);
		
		
		graphics.setFill(secondColor);
		
		// Bottom top
		graphics.fillPolygon(
				new double[] {x+leftHalf, x+left, x+width-right, x+width-rightHalf2}, 
				new double[] {y+height-bottomHalf2, y+height-bottom, y+height-bottom, y+height-bottomHalf2}, 4);
		
		// Right left
		graphics.fillPolygon(
				new double[] {x+width-rightHalf2, x+width-right, x+width-right, x+width-rightHalf2}, 
				new double[] {y+height-bottomHalf2, y+height-bottom, y+top, y+topHalf}, 4);
		
		// Left left
		graphics.fillPolygon(
				new double[] {x, x+leftHalf, x+leftHalf, x}, 
				new double[] {y, y+topHalf, y+height-bottomHalf2, y+height}, 4);
		
		// Top top
		graphics.fillPolygon(
				new double[] {x, x+leftHalf, x+width-rightHalf2, x+width}, 
				new double[] {y, y+topHalf, y+topHalf, y}, 4);
	}
	
	private static void drawSolidBorder(GraphicsContext graphics, Borders finalSizes, SPUIRectangle drawBounds, Color color) {
		graphics.setFill(color);
		graphics.setLineCap(StrokeLineCap.BUTT);
		graphics.setLineJoin(StrokeLineJoin.BEVEL);
		
		if (finalSizes.top != 0) {
			graphics.setLineWidth(finalSizes.top);
			graphics.strokeLine(
					drawBounds.x1, drawBounds.y1 + (finalSizes.top/2), 
					drawBounds.x1 + drawBounds.getWidth(), drawBounds.y1 + (finalSizes.top/2));
		}
		
		if (finalSizes.left != 0) {
			graphics.setLineWidth(finalSizes.left);
			graphics.strokeLine(
					drawBounds.x1 + (int)(finalSizes.left/2), drawBounds.y1, 
					drawBounds.x1 + (int)(finalSizes.left/2), drawBounds.y1 + drawBounds.getHeight());
		}
		
		if (finalSizes.right != 0) {
			graphics.setLineWidth(finalSizes.right);
			graphics.strokeLine(
					drawBounds.x1 + drawBounds.getWidth() - (int)(finalSizes.right/2), drawBounds.y1, 
					drawBounds.x1 + drawBounds.getWidth() - (int)(finalSizes.right/2), drawBounds.y1 + drawBounds.getHeight());
		}
		
		if (finalSizes.bottom != 0) {
			graphics.setLineWidth(finalSizes.bottom);
			graphics.strokeLine(
					drawBounds.x1, drawBounds.y1 + drawBounds.getHeight() - (finalSizes.bottom/2), 
					drawBounds.x1 + drawBounds.getWidth(), drawBounds.y1 + drawBounds.getHeight() - (finalSizes.bottom/2));
		}
	}
	
	private static void drawDashedBorder(GraphicsContext graphics, Borders finalSizes, SPUIRectangle drawBounds, Color color, float dashFactor) {
		graphics.setLineDashes(dashFactor, 0);
		drawSolidBorder(graphics, finalSizes, drawBounds, color);
	}
	
	private static Color brighten(Color color, float factor) {
		return Color.rgb(
				(int) Math.min(255, Math.round(color.getRed() + (255 - color.getRed()) * factor)),
				(int) Math.min(255, Math.round(color.getGreen() + (255 - color.getGreen()) * factor)),
				(int) Math.min(255, Math.round(color.getBlue() + (255 - color.getBlue()) * factor)));
	}
	
	private static Color darken(Color color, float factor) {
		return Color.rgb(
				(int) Math.max(0, Math.round(color.getRed() - color.getRed() * factor)),
				(int) Math.max(0, Math.round(color.getGreen() - color.getGreen() * factor)),
				(int) Math.max(0, Math.round(color.getBlue() - color.getBlue() * factor)));
	}
}
