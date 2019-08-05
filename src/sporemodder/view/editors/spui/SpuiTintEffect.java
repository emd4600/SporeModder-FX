package sporemodder.view.editors.spui;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.RectBounds;
import com.sun.javafx.geom.Rectangle;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.BoundsAccessor;

import javafx.scene.Node;
import javafx.scene.image.PixelReader;
import sporemodder.file.spui.SPUIRectangle;

import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.FilterContext;
import com.sun.scenario.effect.Filterable;
import com.sun.scenario.effect.ImageData;

public class SpuiTintEffect extends Effect {
	float left, top, width, height;
	
	public SpuiTintEffect(SPUIRectangle rect) {
		left = rect.x1;
		top = rect.y1;
		width = rect.x2 - rect.x1;
		height = rect.y2 - rect.y1;
	}

	@Override
	public ImageData filter(FilterContext context, BaseTransform transform, Rectangle rect, Object obj, Effect effect) {
		Filterable img = Effect.getCompatibleImage(context, (int)width, (int)height);
		//img.pix
		
		
		ImageData data = new ImageData(context, img, new Rectangle(getBounds(transform, effect)));
		return data;
	}

	@Override
	public AccelType getAccelType(FilterContext context) {
		return AccelType.NONE;
	}

	@Override
	public BaseBounds getBounds(BaseTransform transform, Effect effect) {
		return new RectBounds(left, top, width, height);
	}

	@Override
	public boolean reducesOpaquePixels() {
		return false;
	}
}
