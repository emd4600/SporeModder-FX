package sporemodder.view.colorpicker;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

// https://stackoverflow.com/a/27180647/3779214
public class ColorSwatchUI extends VBox {
	
	private final Pane colorRect;
	private final Pane colorBar;
    private final Pane colorRectOverlayOne;
    private final Pane colorRectOverlayTwo;
    private final Region colorRectIndicator;
    private final Region colorBarIndicator;
    private final Pane newColorRect;

    private final ObjectProperty<Color> currentColorProperty = 
            new SimpleObjectProperty<>(Color.WHITE);
        private final ObjectProperty<Color> customColorProperty = 
            new SimpleObjectProperty<>(Color.TRANSPARENT);
    
	private final DoubleProperty hue = new SimpleDoubleProperty();
	private final DoubleProperty saturation = new SimpleDoubleProperty();
	private final DoubleProperty brightness = new SimpleDoubleProperty();
	
	private DoubleProperty alpha = new SimpleDoubleProperty(1.0) {
        @Override protected void invalidated() {
            setCustomColor(new Color(getCustomColor().getRed(), getCustomColor().getGreen(), 
                    getCustomColor().getBlue(), clamp(alpha.get())));
        }
    };
	
	public ColorSwatchUI() {
		
		getStyleClass().add("simple-color-swatch");
		
		VBox box = new VBox();
		
		box.getStyleClass().add("color-rect-pane");
        customColorProperty().addListener((ov, t, t1) -> colorChanged());
        
        colorRectIndicator = new Region();
        colorRectIndicator.getStyleClass().add("color-rect-indicator");
        colorRectIndicator.setManaged(false);
        colorRectIndicator.setMouseTransparent(true);
        colorRectIndicator.setCache(true);

        final Pane colorRectOpacityContainer = new StackPane();

        colorRect = new StackPane();
        colorRect.getStyleClass().addAll("color-rect", "transparent-pattern");

        Pane colorRectHue = new Pane();
        colorRectHue.backgroundProperty().bind(new ObjectBinding<Background>() {
            {
                bind(hue);
            }

            @Override protected Background computeValue() {
                return new Background(new BackgroundFill(
                        Color.hsb(hue.getValue(), 1.0, 1.0), 
                        CornerRadii.EMPTY, Insets.EMPTY));

            }
        });            
        
        colorRectOverlayOne = new Pane();
        colorRectOverlayOne.getStyleClass().add("color-rect");
        colorRectOverlayOne.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, 
                new Stop(0, Color.rgb(255, 255, 255, 1)), 
                new Stop(1, Color.rgb(255, 255, 255, 0))), 
                CornerRadii.EMPTY, Insets.EMPTY)));

        EventHandler<MouseEvent> rectMouseHandler = event -> {
            final double x = event.getX();
            final double y = event.getY();
            saturation.set(clamp(x / colorRect.getWidth()) * 100);
            brightness.set(100 - (clamp(y / colorRect.getHeight()) * 100));
            updateHSBColor();
        };

        colorRectOverlayTwo = new Pane();
        colorRectOverlayTwo.getStyleClass().addAll("color-rect");
        colorRectOverlayTwo.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, 
                new Stop(0, Color.rgb(0, 0, 0, 0)), new Stop(1, Color.rgb(0, 0, 0, 1))), 
                CornerRadii.EMPTY, Insets.EMPTY)));
        colorRectOverlayTwo.setOnMouseDragged(rectMouseHandler);
        colorRectOverlayTwo.setOnMousePressed(rectMouseHandler);

        Pane colorRectBlackBorder = new Pane();
        colorRectBlackBorder.setMouseTransparent(true);
        colorRectBlackBorder.getStyleClass().addAll("color-rect", "color-rect-border");
		
		
        colorBar = new Pane();
        colorBar.getStyleClass().add("color-bar");
        colorBar.setBackground(new Background(new BackgroundFill(createHueGradient(), 
                CornerRadii.EMPTY, Insets.EMPTY)));

        colorBarIndicator = new Region();
        colorBarIndicator.getStyleClass().add("color-bar-indicator");
        colorBarIndicator.setMouseTransparent(true);
        colorBarIndicator.setCache(true);

        colorRectIndicator.layoutXProperty().bind(
            saturation.divide(100).multiply(colorRect.widthProperty()));
        colorRectIndicator.layoutYProperty().bind(
            Bindings.subtract(1, brightness.divide(100)).multiply(colorRect.heightProperty()));
        colorBarIndicator.layoutXProperty().bind(
            hue.divide(360).multiply(colorBar.widthProperty()));
        colorRectOpacityContainer.opacityProperty().bind(alpha);

        EventHandler<MouseEvent> barMouseHandler = event -> {
            final double x = event.getX();
            hue.set(clamp(x / colorRect.getWidth()) * 360);
            updateHSBColor();
        };

        colorBar.setOnMouseDragged(barMouseHandler);
        colorBar.setOnMousePressed(barMouseHandler);

        newColorRect = new Pane();
        newColorRect.getStyleClass().add("color-new-rect");
        newColorRect.setId("new-color");
        newColorRect.backgroundProperty().bind(new ObjectBinding<Background>() {
            {
                bind(customColorProperty);
            }
            @Override protected Background computeValue() {
            	Stop[] stops = new Stop[4];
            	stops[0] = new Stop(0.0, currentColorProperty.get());
            	stops[1] = new Stop(0.5, currentColorProperty.get());
            	stops[2] = new Stop(0.5, customColorProperty.get());
            	stops[3] = new Stop(1.0, customColorProperty.get());
                return new Background(new BackgroundFill(new LinearGradient(0, 0, 1.0, 0, true, CycleMethod.NO_CYCLE, stops), CornerRadii.EMPTY, Insets.EMPTY));
            }
        });

        colorBar.getChildren().setAll(colorBarIndicator);
        colorRectOpacityContainer.getChildren().setAll(colorRectHue, colorRectOverlayOne, colorRectOverlayTwo);
        colorRect.getChildren().setAll(colorRectOpacityContainer, colorRectBlackBorder, colorRectIndicator);
        VBox.setVgrow(colorRect, Priority.SOMETIMES);
        box.getChildren().addAll(colorBar, colorRect, newColorRect);

        getChildren().add(box);

        if (currentColorProperty.get() == null) {
            currentColorProperty.set(Color.TRANSPARENT);
        }
        updateValues();
	}
	
	public final DoubleProperty hueProperty() {
		return hue;
	}
	
	public final DoubleProperty saturationProperty() {
		return saturation;
	}
	
	public final DoubleProperty brightnessProperty() {
		return brightness;
	}
	
	private void updateValues() {
        hue.set(getCurrentColor().getHue());
        saturation.set(getCurrentColor().getSaturation()*100);
        brightness.set(getCurrentColor().getBrightness()*100);
        alpha.set(getCurrentColor().getOpacity()*100);
        setCustomColor(Color.hsb(hue.get(), clamp(saturation.get() / 100), 
                clamp(brightness.get() / 100), clamp(alpha.get())));
    }

    private void colorChanged() {
        hue.set(getCustomColor().getHue());
        saturation.set(getCustomColor().getSaturation() * 100);
        brightness.set(getCustomColor().getBrightness() * 100);
    }

    private void updateHSBColor() {
        Color newColor = Color.hsb(hue.get(), clamp(saturation.get() / 100), 
                        clamp(brightness.get() / 100), clamp(alpha.get()));
        setCustomColor(newColor);
    }

    @Override 
    protected void layoutChildren() {
        super.layoutChildren();            
        colorRectIndicator.autosize();
    }

    static double clamp(double value) {
        return value < 0 ? 0 : value > 1 ? 1 : value;
    }

    private static LinearGradient createHueGradient() {
        double offset;
        Stop[] stops = new Stop[255];
        for (int x = 0; x < 255; x++) {
            offset = (double)((1.0 / 255) * x);
            int h = (int)((x / 255.0) * 360);
            stops[x] = new Stop(offset, Color.hsb(h, 1.0, 1.0));
        }
        return new LinearGradient(0f, 0f, 1f, 0f, true, CycleMethod.NO_CYCLE, stops);
    }

    public void setCurrentColor(Color currentColor) {
        this.currentColorProperty.set(currentColor);
        updateValues();
    }

    public Color getCurrentColor() {
        return currentColorProperty.get();
    }

    public final ObjectProperty<Color> customColorProperty() {
        return customColorProperty;
    }

    public void setCustomColor(Color color) {
        customColorProperty.set(color);
    }

    public Color getCustomColor() {
        return customColorProperty.get();
    }
}
