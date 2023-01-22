package sporemodder.extras.spuieditor.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import sporemodder.extras.spuieditor.ComponentChooser;
import sporemodder.extras.spuieditor.ComponentValueAction;
import sporemodder.extras.spuieditor.ComponentValueAction.ComponentValueListener;
import sporemodder.extras.spuieditor.ImageChooser;
import sporemodder.extras.spuieditor.ImagePreviewPanel;
import sporemodder.extras.spuieditor.PanelUtils;
import sporemodder.extras.spuieditor.PanelUtils.BooleanValueAction;
import sporemodder.extras.spuieditor.PanelUtils.FloatTextField;
import sporemodder.extras.spuieditor.PanelUtils.FloatValueAction;
import sporemodder.extras.spuieditor.PanelUtils.IntValueAction;
import sporemodder.extras.spuieditor.PanelUtils.PropertyInfo;
import sporemodder.extras.spuieditor.PanelUtils.ShortValueAction;
import sporemodder.extras.spuieditor.PropertiesPanel;
import sporemodder.extras.spuieditor.ResourceLoader;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.UVEditor;
import sporemodder.extras.spuieditor.UndoableEditor;
import sporemodder.files.formats.LocalizedText;
import sporemodder.files.formats.ResourceKey;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIFileResource;
import sporemodder.files.formats.spui.SPUINumberSections.SectionShort;
import sporemodder.files.formats.spui.SPUIObject;
import sporemodder.files.formats.spui.SPUIResource;
import sporemodder.files.formats.spui.SPUIComplexSections.SectionText;
import sporemodder.files.formats.spui.SPUIVectorSections.SectionDimension;
import sporemodder.files.formats.spui.SPUIVectorSections.SectionVec4;
import sporemodder.userinterface.JLabelLink;
import sporemodder.utilities.Hasher;

public class Image extends SPUIDefaultComponent {

	public static final int TYPE = 0x01BE6B15;
	
	private static final int PROPERTY_IMAGEPATH = Hasher.stringToFNVHash("ImagePath");
	
	private SPUIFileResource resource;
	private BufferedImage image;
	private final float[] uvCoords = new float[4];
	private final Dimension dimensions = new Dimension();
	
	public Image(SPUIObject object) throws InvalidBlockException, IOException {
		super(object);
		if (object instanceof SPUIBlock) {
			loadBlock((SPUIBlock) object);
		} else if (object instanceof SPUIFileResource) {
			loadFileResource((SPUIFileResource) object);
		}
	}
	
	private void loadBlock(SPUIBlock block) throws InvalidBlockException, IOException {
		resource = (SPUIFileResource) block.getParent().get(
				SectionShort.getValues(block.getSection(0x01BE0001, SectionShort.class), new short[] { -1 }, 1)[0]);
		
		loadImagePath(block);
		
		image = ResourceLoader.loadImage(resource);
		
		float[] uvCoords = SectionVec4.getValues(block.getSection(0x01BE0002, SectionVec4.class), new float[][] {new float[] {0, 0, 1, 1}}, 1)[0];
		this.uvCoords[0] = uvCoords[0];
		this.uvCoords[1] = uvCoords[1];
		this.uvCoords[2] = uvCoords[2];
		this.uvCoords[3] = uvCoords[3];
		
		int[] dim = SectionDimension.getValues(block.getSection(0x01BE0003, SectionDimension.class), new int[][] {new int[] {0, 0}}, 1)[0];
		dimensions.width = dim[0];
		dimensions.height = dim[1];
		
		addUnassignedInt(block, 0x01BE0004, 0);
		
	}
	
	private void loadFileResource(SPUIFileResource res) throws IOException {
		image = ResourceLoader.loadImage(res);
		resource = res;
		
		if (image != null) {
			dimensions.width = image.getWidth();
			dimensions.height = image.getHeight();
			
			uvCoords[0] = 0;
			uvCoords[1] = 0;
			uvCoords[2] = 1;
			uvCoords[3] = 1;
		}
	}
	
	public Image(SPUIBlock block) throws InvalidBlockException, IOException {
		super(block);

		loadBlock(block);
	}
	
	public Image(SPUIViewer viewer) {
		super(viewer);
		
		resource = new SPUIFileResource();
		resource.setIsAtlas(true);
		
		uvCoords[0] = 0;
		uvCoords[1] = 0;
		uvCoords[2] = 1;
		uvCoords[3] = 1;
		
		unassignedProperties.put(0x01BE0004, 0);
	}
	
	@Override
	public SPUIObject saveComponent(SPUIBuilder builder) {
		
		if (resource == null || resource.isAtlas()) {
			SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
			
			if (resource != null) {
				builder.addObject(resource);
			}
			builder.addReference(block, 0x01BE0001, new SPUIObject[] {resource});
			builder.addVec4(block, 0x01BE0002, new float[][] {uvCoords});
			builder.addDimension(block, 0x01BE0003, new int[][] {new int[] {dimensions.width, dimensions.height}});
			
			saveInt(builder, block, 0x01BE0004);
			
			if (resource != null) {
				addImagePath(builder, block);
			}
			
			return object;
		}
		else {
			// image is directly a FileResource, nothing to do here
			
			return resource;
		}
	}
	
	public void addImagePath(SPUIBuilder builder, SPUIBlock block) {
		if (resource != null && resource.getRealPath() != null) {
			builder.addText(block, PROPERTY_IMAGEPATH, new LocalizedText[] {new LocalizedText(resource.getRealPath())});
		}
	}
	
	public static void addImagePath(SPUIBuilder builder, SPUIBlock block, Image[] images) {
		if (images != null && images.length > 0) {
			LocalizedText[] data = new LocalizedText[images.length];
			
			// only add the property if any path needs to be saved here
			boolean addProperty = false;
			
			for (int i = 0; i < images.length; i++) {
				if (images[i] != null && images[i].getResource() != null && images[i].getResource().getRealPath() != null) {
					data[i] = new LocalizedText(images[i].getResource().getRealPath());
					addProperty = true;
				}
				else {
					data[i] = new LocalizedText((String) null);
				}
			}
			
			if (addProperty) {
				builder.addText(block, PROPERTY_IMAGEPATH, data);
			}
		}
	}
	
	private void loadImagePath(SPUIBlock block) throws InvalidBlockException {
		LocalizedText[] imagePath = SectionText.getValues(block.getSection(PROPERTY_IMAGEPATH, SectionText.class), null, 1);
		if (imagePath != null && imagePath.length > 0) {
			resource.setRealPath(imagePath[0].getString());
		}
	}
	
	// Sets the image path before loading the image if it's non-atlas
	public static void loadImagePath(SPUIBlock parentBlock, SPUIObject object) throws InvalidBlockException {
		if (object instanceof SPUIFileResource && !((SPUIFileResource) object).isAtlas()) {
			LocalizedText[] imagePath = SectionText.getValues(parentBlock.getSection(PROPERTY_IMAGEPATH, SectionText.class), null, 1);
			if (imagePath != null && imagePath.length > 0) {
				((SPUIFileResource) object).setRealPath(imagePath[0].getString());
			}
		}
	}
	
	public static void loadImages(SPUIBlock block, short[] indices, Image[] dst) throws InvalidBlockException, IOException {
		LocalizedText[] imagePaths = SectionText.getValues(block.getSection(PROPERTY_IMAGEPATH, SectionText.class), null, dst.length);
		
		for (int i = 0; i < dst.length; i++) {
			if (indices[i] == -1) {
				continue;
			}
			SPUIObject object = block.getParent().get(indices[i]);
			
			if (object instanceof SPUIFileResource && !((SPUIFileResource) object).isAtlas()
					&& imagePaths != null && LocalizedText.isValid(imagePaths[i])) {
				
				((SPUIFileResource) object).setRealPath(imagePaths[i].getString());
			}
			
			dst[i] = (Image) ResourceLoader.getComponent(object);
		}
	}
	
	public void snapToPixels(float[] dest) {
		int width = image.getWidth();
		int height = image.getHeight();
		dest[0] = Math.round(uvCoords[0] * width) / (float) width;
		dest[1] = Math.round(uvCoords[1] * height) / (float) height;
		dest[2] = Math.round(uvCoords[2] * width) / (float) width;
		dest[3] = Math.round(uvCoords[3] * height) / (float) height;
	}
	
	public boolean isAtlasImage() {
		return resource == null || resource.isAtlas();
	}
	
	@Override
	public boolean isUnique() {
		if (resource == null || resource.isAtlas()) {
			return object == null || !(object instanceof SPUIResource);
		}
		else {
			return false;  // FileResources are not unique!
		}
	}
	
	public static boolean isValid(Image image) {
		return image != null && image.image != null;
	}
	
	private Image() {
		super();
	}
	
	public Image(SPUIFileResource res) throws IOException {
		super();
		
		loadFileResource(res);
	}
	
	@Override
	public Image copyComponent(boolean propagate) {
		Image other = new Image();
		super.copyComponent(other, propagate);
		
		other.resource = new SPUIFileResource(resource);
		other.image = image;
		other.uvCoords[0] = uvCoords[0];
		other.uvCoords[1] = uvCoords[1];
		other.uvCoords[2] = uvCoords[2];
		other.uvCoords[3] = uvCoords[3];
		other.dimensions.width = dimensions.width;
		other.dimensions.height = dimensions.height;
		
		return other;
	}
	
	private void loadImage() {
		try {
			// force load
			image = ResourceLoader.loadImage(resource, true);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(viewer, "Error loading image: " + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		
		if (image != null && (dimensions.width == 0 && dimensions.height == 0)) {
			dimensions.width = image.getWidth();
			dimensions.height = image.getHeight();
			updatePanel();
		}
		
		if (panelComponents != null) {
			if (panelComponents.lblImageResource != null) {
				panelComponents.lblImageResource.setText(resource.toString());
			}
			if (panelComponents.previewPanel != null) {
				panelComponents.previewPanel.repaint();
			}
			if (panelComponents.btnUVEditor != null) {
				panelComponents.btnUVEditor.setEnabled(image != null);
			}
		}
		if (viewer != null) {
			viewer.repaint();
		}
	}
	
	private static boolean compareRealPath(SPUIFileResource res1, SPUIFileResource res2) {
		if (res1.getRealPath() != null) {
			return res1.getRealPath().equals(res2.getRealPath());
		}
		else {
			return res2.getRealPath() == null;
		}
	}
	
	public void showImageChooser(java.awt.Window parent, UndoableEditor editor) {
		ImageChooser chooser = new ImageChooser(parent, "Choose image");
		chooser.setChosenType(ImageChooser.CHOOSER_EXISTING);
		chooser.setSelectedObject(resource);
		chooser.setDefaultCloseOperation(ImageChooser.DISPOSE_ON_CLOSE);
		chooser.setVisible(true);
		
		if (!chooser.wasCancelled()) {
			SPUIFileResource originalImage = resource;
			
			TreePath path = chooser.getSelectedObject();
			if (path != null) {
				resource = new SPUIFileResource();
				resource.setIsAtlas(originalImage.isAtlas());
				resource.getResourceKey().parseTreePath(path);
				resource.setRealPath(ResourceKey.getStringFromTreePath(path));
				
				// We always want to reload the image
				loadImage();
				
				// We only want to add an action if the image key was changed
				if (!resource.getResourceKey().equals(originalImage.getResourceKey()) &&
						compareRealPath(resource, originalImage)) {
					
					editor.addCommandAction(new ComponentValueAction<SPUIFileResource>(originalImage, resource, new ComponentValueListener<SPUIFileResource>() {
						@Override
						public void valueChanged(SPUIFileResource value) {
							resource = value;
							loadImage();
						}
					}));
				}
				
			}
		}
	}
	
	public ImagePreviewPanel getPreviewPanel() {
		return panelComponents.previewPanel;
	}
	
	public void setResource(SPUIFileResource resource) {
		this.resource = resource;	
	}
	
	public SPUIFileResource getResource() {
		return resource;
	}
	
	public static ComponentChooser<Image> getImageChooser(SPUIViewer viewer) {
		List<Class<? extends Image>> components = new ArrayList<Class<? extends Image>>();
		components.add(Image.class);
		
		final ComponentChooser<Image> chooser = new ComponentChooser<Image>(viewer.getEditor(), 
				"Choose an image", true, components, viewer.getEditor());
		
		chooser.setDelegate(chooser.new ComponentChooserDelegate() {
			ImagePreviewPanel imagePreviewPanel = new ImagePreviewPanel(null);
			
			@Override
			public JPanel getPreviewPanel() {
				JPanel panel = super.getPreviewPanel();
				
				if (panel == null && chooser.getChosenType() == ComponentChooser.CHOOSER_EXISTING) {
					TreePath selectedObject = chooser.getSelectedObject();
					if (selectedObject == null || selectedObject.getPathCount() <= 1) {
						return null;
					}
					
					panel = new JPanel();
					panel.setBorder(BorderFactory.createTitledBorder("Preview"));
					
					imagePreviewPanel.setImage((Image) ((DefaultMutableTreeNode) selectedObject.getLastPathComponent()).getUserObject());
					
					panel.add(new JScrollPane(imagePreviewPanel));
					
					return panel;
				}
				else {
					return panel;
				}
			}
		});
		
		return chooser;
	}
	
	private void updatePanel() {
		if (panelComponents != null && resource.isAtlas()) {
			panelComponents.tfUV0.setText(Float.toString(uvCoords[0]));
			panelComponents.tfUV1.setText(Float.toString(uvCoords[1]));
			panelComponents.tfUV2.setText(Float.toString(uvCoords[2]));
			panelComponents.tfUV3.setText(Float.toString(uvCoords[3]));
			panelComponents.tfWidth.setText(Integer.toString(dimensions.width));
			panelComponents.tfHeight.setText(Integer.toString(dimensions.height));
			if (unassignedProperties.containsKey(0x01BE0004)) {
				panelComponents.tfUnknown.setText(Integer.toString((int) unassignedProperties.get(0x01BE0004)));
			}
		}
	}
	
	protected class ImagePanelComponents {
		protected JButton btnUVEditor;
		protected ImagePreviewPanel previewPanel;
		protected JLabelLink lblImageResource;
		protected FloatTextField tfUV0;
		protected FloatTextField tfUV1;
		protected FloatTextField tfUV2;
		protected FloatTextField tfUV3;
		protected JTextField tfWidth;
		protected JTextField tfHeight;
		protected JTextField tfUnknown;
	}
	
	private ImagePanelComponents panelComponents;
	
	
	@Override
	public PropertiesPanel getPropertiesPanel() {
		final PropertiesPanel panel = super.getPropertiesPanel();
		
		panelComponents = new ImagePanelComponents();
		
		panelComponents.previewPanel = new ImagePreviewPanel(this);
		panelComponents.previewPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		PropertiesPanel imagePanel = new PropertiesPanel("Image");
		panel.addPanel(imagePanel);
		
		// These ones go after the image panel, but we must put them here so we have their reference
		
		panelComponents.btnUVEditor = new JButton("UV Editor");
		panelComponents.btnUVEditor.setEnabled(image != null);
		
		panelComponents.btnUVEditor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				UVEditor editor = new UVEditor(viewer.getEditor(), Image.this);
				editor.setDefaultCloseOperation(UVEditor.DISPOSE_ON_CLOSE);
				editor.setVisible(true);
				
				updatePanel();
			}
		});
		panel.addComponent(panelComponents.btnUVEditor);
		
		final PropertyInfo piWidth = panel.addIntValue("Width", dimensions.width, new IntValueAction() {
			@Override
			public void valueChanged(int value) {
				dimensions.width = value;
				panelComponents.previewPanel.repaint();
				viewer.repaint();
			}
		}, viewer.getEditor());
		
		final PropertyInfo piHeight = panel.addIntValue("Height", dimensions.height, new IntValueAction() {
			@Override
			public void valueChanged(int value) {
				dimensions.height = value;
				panelComponents.previewPanel.repaint();
				viewer.repaint();
			}
		}, viewer.getEditor());
		
		panelComponents.tfWidth = (JTextField) piWidth.components[0];
		panelComponents.tfHeight = (JTextField) piHeight.components[0];
		
		final PropertiesPanel panelUVs = new PropertiesPanel("UV Coordinates");
		panel.addPanel(panelUVs);
		
		panelComponents.tfUV0 = (FloatTextField) panelUVs.addFloatFieldValue("Top-left X", uvCoords[0], new FloatValueAction() {
			@Override
			public void valueChanged(float value) {
				uvCoords[0] = value;
				panelComponents.previewPanel.repaint();
				viewer.repaint();
			}
		}, viewer.getEditor()).components[0];
		
		panelComponents.tfUV1 = (FloatTextField) panelUVs.addFloatFieldValue("Top-left Y", uvCoords[1], new FloatValueAction() {
			@Override
			public void valueChanged(float value) {
				uvCoords[1] = value;
				panelComponents.previewPanel.repaint();
				viewer.repaint();
			}
		}, viewer.getEditor()).components[0];
		
		panelComponents.tfUV2 = (FloatTextField) panelUVs.addFloatFieldValue("Bottom-right X", uvCoords[2], new FloatValueAction() {
			@Override
			public void valueChanged(float value) {
				uvCoords[2] = value;
				panelComponents.previewPanel.repaint();
				viewer.repaint();
			}
		}, viewer.getEditor()).components[0];
		
		panelComponents.tfUV3 = (FloatTextField) panelUVs.addFloatFieldValue("Bottom-right Y", uvCoords[3], new FloatValueAction() {
			@Override
			public void valueChanged(float value) {
				uvCoords[3] = value;
				panelComponents.previewPanel.repaint();
				viewer.repaint();
			}
		}, viewer.getEditor()).components[0];
		
		
		final PropertyInfo piUnknown = panel.addIntProperty(this, 0x01BE0004, viewer.getEditor());
		panelComponents.tfUnknown = (JTextField) piUnknown.components[0];
		
		
		panelComponents.lblImageResource = (JLabelLink) imagePanel.addShortValue("Resource: ", resource, new ShortValueAction() {
			private void action() {
				showImageChooser(viewer.getEditor(), viewer.getEditor());
			}
			
			@Override
			public void linkAction(JLabelLink labelLink) {
				action();
			}
			@Override
			public void changeAction(JLabelLink labelLink) {
				action();
			}
		}).components[0];
		
		imagePanel.addBooleanValue("Is atlas image", resource.isAtlas(), new BooleanValueAction() {
			@Override
			public void valueChanged(boolean isSelected) {
				resource.setIsAtlas(isSelected);
				piWidth.setVisible(isSelected);
				piHeight.setVisible(isSelected);
				panelUVs.setVisible(isSelected);
				piUnknown.setVisible(isSelected);
				panelComponents.btnUVEditor.setVisible(isSelected);
				
				panelComponents.previewPanel.revalidate();
				panelComponents.previewPanel.repaint();
				
				viewer.repaint();
			}
		}, viewer.getEditor());
		
		if (!resource.isAtlas()) {
			piWidth.setVisible(false);
			piHeight.setVisible(false);
			panelUVs.setVisible(false);
			piUnknown.setVisible(false);
			panelComponents.btnUVEditor.setVisible(false);
		}
		
		PanelUtils.addGBC(panel, new JScrollPane(panelComponents.previewPanel), 0, panel.getNextRow(), GridBagConstraints.WEST, new Insets(0, 0, 5, 0), 2, 1, GridBagConstraints.BOTH, 1.0f, 1.0f);
		
		return panel;
	}
	
	

	public BufferedImage getBufferedImage() {
		return image;
	}

	public float[] getUVCoords() {
		return resource.isAtlas() ? uvCoords : new float[] {0, 0, 1.0f, 1.0f};
	}

	public Dimension getDimensions() {
		return resource.isAtlas() ? dimensions : new Dimension(image.getWidth(), image.getHeight());
	}

	public int[] getImageUVCoords() {
		int width = image.getWidth();
		int height = image.getHeight();
		int[] result = new int[4];
		
		float[] uvCoords = getUVCoords();
		
		result[0] = Math.round(uvCoords[0] * width);
		result[1] = Math.round(uvCoords[1] * height);
		result[2] = Math.round(uvCoords[2] * width);
		result[3] = Math.round(uvCoords[3] * height);
		
		return result;
	}

	public static BufferedImage drawTintedImage(BufferedImage image, Dimension dim, int[] uvCoordinates, Color tint) {
		BufferedImage temp = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = temp.createGraphics();
		
		Image.drawImage(g, image, 
				0, 0, dim.width, dim.height, 
				uvCoordinates[0], uvCoordinates[1], uvCoordinates[2], uvCoordinates[3]);
		
		float[] scaleFactors = new float[4];
		float[] offsets = new float[4];
		
		scaleFactors[0] = tint.getRed() / 255f;
		scaleFactors[1] = tint.getGreen() / 255f;
		scaleFactors[2] = tint.getBlue() / 255f;
		scaleFactors[3] = tint.getAlpha() / 255f;
		
		BufferedImage img = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
		
		img.createGraphics().drawImage(temp, new RescaleOp(scaleFactors, offsets, null), 0, 0);
		
		return img;
	}
	
	public static void drawImage(Graphics2D graphics, Image image, int dx, int dy) {
		
		int[] imageUVCoords = image.getImageUVCoords();
		Dimension dim = image.getDimensions();
		
		drawImage(graphics, image.getBufferedImage(), dx, dy, dx + dim.width, dy + dim.height,
				imageUVCoords[0], imageUVCoords[1], imageUVCoords[2], imageUVCoords[3]);
	}
	
	public static void drawImage(Graphics2D graphics, Image image, int dx, int dy, int width, int height) {
		
		int[] imageUVCoords = image.getImageUVCoords();
		
		drawImage(graphics, image.getBufferedImage(), dx, dy, dx + width, dy + height,
				imageUVCoords[0], imageUVCoords[1], imageUVCoords[2], imageUVCoords[3]);
	}
	
	public static void drawImage(Graphics2D graphics, BufferedImage image,
			int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2) {
		
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		
		int destWidth = dx2 - dx1;
		int destHeight = dy2 - dy1;
		
		int[] sourceCoords = new int[] {sx1, sy1, sx2, sy2};
		int[] destCoords = new int[] {dx1, dy1, dx2, dy2};
		
		sourceCoords[0] = Math.max(0, Math.min(sx1, imageWidth));
		sourceCoords[1] = Math.max(0, Math.min(sy1, imageHeight));
		sourceCoords[2] = Math.max(0, Math.min(sx2, imageWidth));
		sourceCoords[3] = Math.max(0, Math.min(sy2, imageHeight));
		
		destCoords[0] =  Math.round(dx1 + destWidth * (1 - (sx2 - sourceCoords[0]) / (float) (sx2 - sx1)));
		destCoords[1] =  Math.round(dy1 + destHeight * (1 - (sy2 - sourceCoords[1]) / (float) (sy2 - sy1)));
		
		destCoords[2] =  Math.round(dx2 - destWidth * (1 - (sourceCoords[2] - sx1) / (float) (sx2 - sx1)));
		destCoords[3] =  Math.round(dy2 - destHeight * (1 - (sourceCoords[3] - sy1) / (float) (sy2 - sy1)));
		
		graphics.drawImage(image, 
				destCoords[0], destCoords[1], destCoords[2], destCoords[3], 
				sourceCoords[0], sourceCoords[1], sourceCoords[2], sourceCoords[3], null);
	}
	
	public static void drawTiled(Graphics2D graphics, Image image, Rectangle bounds) {
		Dimension dim = image.getDimensions();
		Image.drawTiled((Graphics2D) graphics.create(bounds.x, bounds.y, bounds.width, bounds.height),
				image.getBufferedImage(), dim, bounds, new Point(0, 0), 
				new Point(dim.width, dim.height), image.getImageUVCoords());
	}
	
	public static void drawTiled(Graphics2D graphics, BufferedImage image, Dimension realDim, Rectangle bounds, Point _p1, Point _p2, int[] uvCoordinates) {
		
		if (bounds.width == 0 || bounds.height == 0) {
			return;
		}
		
		Point p1 = new Point(_p1);
		Point p2 = new Point(_p2);
		
		if (p2.x - p1.x == 0 || p2.y - p1.y == 0) {
			return;
		}
		
		while (p1.y + realDim.height <= bounds.height) {
			
			while (p1.x + realDim.width <= bounds.width) {
				Image.drawImage(graphics, image, p1.x, p1.y, p2.x, p2.y, 
						uvCoordinates[0], uvCoordinates[1], uvCoordinates[2], uvCoordinates[3]);
				
				p1.x += realDim.width;
				p2.x += realDim.width;
			}
			
			// Draw remaining horizontal
			Image.drawImage(graphics, image, p1.x, p1.y, p2.x, p2.y, 
					uvCoordinates[0], uvCoordinates[1], uvCoordinates[2], uvCoordinates[3]);
			
			
			p1.y += realDim.height;
			p2.y += realDim.height;
			// return to original x pos
			p1.x = _p1.x;
			p2.x = _p2.x;
		}
		
		// Draw remaining vertical
		while (p1.x + realDim.width <= bounds.width) {
			
			Image.drawImage(graphics, image, p1.x, p1.y, p2.x, p2.y, 
					uvCoordinates[0], uvCoordinates[1], uvCoordinates[2], uvCoordinates[3]);
			
			p1.x += realDim.width;
			p2.x += realDim.width;
		}
		
		// Draw remaining horizontal
		Image.drawImage(graphics, image, p1.x, p1.y, p2.x, p2.y, 
				uvCoordinates[0], uvCoordinates[1], uvCoordinates[2], uvCoordinates[3]);
	}
	
	// returns centerBounds
	private static Rectangle drawSlicedEdges(Graphics2D graphics, Rectangle bounds, Dimension imageDim, int[] uvCoordinates, BufferedImage image,
			float[] sliceProportions, float[] scales, boolean isTiled) {
		
		int left = Math.round(sliceProportions[LEFT] * scales[X] * imageDim.width);
		int top = Math.round(sliceProportions[TOP] * scales[Y] * imageDim.height);
		int right = Math.round(sliceProportions[RIGHT] * scales[X] * imageDim.width);
		int bottom = Math.round(sliceProportions[BOTTOM] * scales[Y] * imageDim.height);
		
		Rectangle centerBounds = new Rectangle();
		centerBounds.x = bounds.x + left;
		centerBounds.y = bounds.y + top;
		centerBounds.width = bounds.width - right - (centerBounds.x - bounds.x);
		centerBounds.height = bounds.height - bottom - (centerBounds.y - bounds.y);
		
		if (centerBounds.width < 0) {
			centerBounds.x = (int) Math.round(centerBounds.x + centerBounds.width*0.5f);
			centerBounds.width = 0;
		}
		
		if (centerBounds.height < 0) {
			centerBounds.y = (int) Math.round(centerBounds.y + centerBounds.height*0.5f);
			centerBounds.height = 0;
		}
		
		int uvWidth = uvCoordinates[2] - uvCoordinates[0];
		int uvHeight = uvCoordinates[3] - uvCoordinates[1];
		
		/* --  Draw top-left corner -- */
		
		Image.drawImage(graphics, image, bounds.x, bounds.y, centerBounds.x, centerBounds.y, 
				uvCoordinates[0], 
				uvCoordinates[1], 
				Math.round(uvCoordinates[0] + uvWidth * sliceProportions[LEFT]), 
				Math.round(uvCoordinates[1] + uvHeight * sliceProportions[TOP]));
		
		/* --  Draw top-right corner -- */
		
		Image.drawImage(graphics, image, centerBounds.x + centerBounds.width, bounds.y, bounds.x + bounds.width, centerBounds.y, 
				Math.round(uvCoordinates[0] + uvWidth * (1 - sliceProportions[RIGHT])), 
				uvCoordinates[1], 
				uvCoordinates[2], 
				Math.round(uvCoordinates[3] - uvHeight * (1 - sliceProportions[TOP])));
		
		/* --  Draw bottom-right corner -- */
		
		Image.drawImage(graphics, image, centerBounds.x + centerBounds.width, centerBounds.y + centerBounds.height, bounds.x + bounds.width, bounds.y + bounds.height, 
				Math.round(uvCoordinates[2] - uvWidth * sliceProportions[RIGHT]), 
				Math.round(uvCoordinates[1] + uvHeight * (1 - sliceProportions[BOTTOM])), 
				uvCoordinates[2], 
				uvCoordinates[3]);
		
		/* --  Draw bottom-left corner -- */
		
		Image.drawImage(graphics, image, bounds.x, centerBounds.y + centerBounds.height, centerBounds.x, bounds.y + bounds.height, 
				uvCoordinates[0], 
				Math.round(uvCoordinates[1] + uvHeight * (1 - sliceProportions[BOTTOM])), 
				Math.round(uvCoordinates[0] + uvWidth * sliceProportions[LEFT]), 
				uvCoordinates[3]);
		
		if (centerBounds.width > 0) {
			/* --  Draw top edge -- */
			
			drawEdge(graphics, image, isTiled,  
					centerBounds.x, bounds.y, centerBounds.x + centerBounds.width, centerBounds.y, 
					Math.round(uvCoordinates[0] + uvWidth * sliceProportions[LEFT]), 
					uvCoordinates[1], 
					Math.round(uvCoordinates[2] - uvWidth * sliceProportions[RIGHT]), 
					Math.round(uvCoordinates[3] - uvHeight * (1 - sliceProportions[TOP])));
			
			/* --  Draw bottom edge -- */
			
			drawEdge(graphics, image, isTiled,
					centerBounds.x, centerBounds.y + centerBounds.height, centerBounds.x + centerBounds.width, bounds.y + bounds.height, 
					Math.round(uvCoordinates[0] + uvWidth * sliceProportions[LEFT]), 
					Math.round(uvCoordinates[1] + uvHeight * (1 - sliceProportions[BOTTOM])), 
					Math.round(uvCoordinates[2] - uvWidth * sliceProportions[RIGHT]), 
					uvCoordinates[3]);
			
		}
		if (centerBounds.height > 0) {
			/* --  Draw left edge -- */
			
			drawEdge(graphics, image, isTiled, 
					bounds.x, centerBounds.y, centerBounds.x, centerBounds.y + centerBounds.height, 
					uvCoordinates[0], 
					Math.round(uvCoordinates[1] + uvHeight * sliceProportions[TOP]), 
					Math.round(uvCoordinates[2] - uvWidth * (1 - sliceProportions[LEFT])), 
					Math.round(uvCoordinates[1] + uvHeight * (1 - sliceProportions[BOTTOM])));
			
			/* --  Draw right edge -- */
			
			drawEdge(graphics, image, isTiled, 
					centerBounds.x + centerBounds.width, centerBounds.y, bounds.x + bounds.width, centerBounds.y + centerBounds.height,
					Math.round(uvCoordinates[0] + uvWidth * (1 - sliceProportions[RIGHT])), 
					Math.round(uvCoordinates[1] + uvHeight * sliceProportions[TOP]), 
					uvCoordinates[2], 
					Math.round(uvCoordinates[1] + uvHeight * (1 - sliceProportions[BOTTOM])));
		}
		
		return centerBounds;
	}
	
	private static void drawEdge(Graphics2D g, BufferedImage image, boolean isTiled, 
			int dx1, int dy1, int dx2, int dy2, 
			int sx1, int sy1, int sx2, int sy2) {
		
		if (sx1 == sx2) {
			sx2 += 1;
		}
		if (sy1 == sy2) {
			sy2 += 1;
		}
		
		if (!isTiled) {
			Image.drawImage(g, image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2);
		}
		else {
			Image.drawTiled((Graphics2D) g.create(dx1, dy1, dx2 - dx1, dy2 - dy1), image, 
					new Dimension(sx2 - sx1, sy2 - sy1), new Rectangle(0, 0, dx2 - dx1, dy2 - dy1), 
					new Point(0, 0), new Point(sx2 - sx1, sy2 - sy1), new int[] {sx1, sy1, sx2, sy2});
		}
		
	}
	
	public static void drawSlicedImage(Graphics2D graphics, Rectangle bounds, Image image, int[] uvCoordinates, float[] sliceProportions, float[] scales, boolean isTiled) {
		Dimension dim = image.getDimensions();
		
		Rectangle centerBounds = drawSlicedEdges(graphics, bounds, dim, uvCoordinates, image.getBufferedImage(),
				sliceProportions, scales, isTiled);
		
		int uvWidth = uvCoordinates[2] - uvCoordinates[0];
		int uvHeight = uvCoordinates[3] - uvCoordinates[1];
		
		/* -- Draw center region -- */
		
		int[] centerUVs = new int[4];
		centerUVs[0] = (int) Math.round(uvCoordinates[0] + uvWidth*sliceProportions[LEFT]);
		centerUVs[1] = (int) Math.round(uvCoordinates[1] + uvHeight*sliceProportions[TOP]);
		centerUVs[2] = (int) Math.round(uvCoordinates[2] - uvWidth*sliceProportions[RIGHT]);
		centerUVs[3] = (int) Math.round(uvCoordinates[3] - uvHeight*sliceProportions[BOTTOM]);
		
		if (centerUVs[0] == centerUVs[2]) {
			centerUVs[2] = centerUVs[0] + 1;
		}
		if (centerUVs[1] == centerUVs[3]) {
			centerUVs[3] = centerUVs[1] + 1;
		}
		
		if (isTiled) {
			Dimension centerDim = new Dimension(Math.round(dim.width - dim.width*sliceProportions[0] - dim.width*sliceProportions[2]),
					Math.round(dim.height - uvHeight*sliceProportions[1] - dim.height*sliceProportions[3]));
			
			Image.drawTiled((Graphics2D) graphics.create(centerBounds.x, centerBounds.y, centerBounds.width, centerBounds.height), 
					image.getBufferedImage(), centerDim, centerBounds, new Point(), new Point(centerDim.width, centerDim.height), centerUVs);
		}
		else {
			Image.drawImage(graphics, image.getBufferedImage(), centerBounds.x, centerBounds.y, centerBounds.x + centerBounds.width, centerBounds.y + centerBounds.height, 
					centerUVs[0], centerUVs[1], centerUVs[2], centerUVs[3]);
		}
	}
	
	
	
	public static final int STATE_INDEX_DISABLED = 0;
	public static final int STATE_INDEX_IDLE = 1;
	public static final int STATE_INDEX_HOVER = 2;
	public static final int STATE_INDEX_ONCLICK = 3;
	
	public static void drawComponentTiledStates(Graphics2D graphics, Rectangle bounds, WinComponent component, Image image, SPUIViewer viewer) {
		drawComponentTiledStates(graphics, bounds, component.getFlags(), 
				(viewer.isPreview() && component.isActionableComponent()) ? ((ActionableComponent) component).getState() : 0, image);
	}
	
	public static void drawComponentTiledStates(Graphics2D graphics, Rectangle bounds, int flags, int state, Image image) {
		if (image == null) {
			return;
		}
		
		int index = STATE_INDEX_IDLE;
		
		if ((flags & WinComponent.FLAG_ENABLED) == WinComponent.FLAG_ENABLED) {
			
			if ((state & ActionableComponent.STATE_CLICK) == ActionableComponent.STATE_CLICK
					|| (state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED) {
				index = STATE_INDEX_ONCLICK;
			}
			else if ((state & ActionableComponent.STATE_HOVER) == ActionableComponent.STATE_HOVER) {
				index = STATE_INDEX_HOVER;
			}
			else {
				index = STATE_INDEX_IDLE;
			}
		}
		else {
			index = STATE_INDEX_DISABLED;
		}
		
		float[] uvCoordinates = image.getUVCoords();
		BufferedImage bufferedImage = image.getBufferedImage();
		
		float tileSize = (uvCoordinates[2] - uvCoordinates[0]) / 4.0f;
		
		Image.drawImage(graphics, bufferedImage, 
				bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height,
				(int) Math.round((uvCoordinates[0] + tileSize * index) * bufferedImage.getWidth()),
				(int) Math.round(uvCoordinates[1] * bufferedImage.getHeight()), 
				(int) Math.round((uvCoordinates[0] + tileSize * index + tileSize) * bufferedImage.getWidth()), 
				(int) Math.round(uvCoordinates[3] * bufferedImage.getHeight()));
	}
	
	public static void drawComponentTiledStates(Graphics2D graphics, Rectangle bounds, WinComponent component, Image image, float[] sliceProportions, SPUIViewer viewer) {
		if (image == null) {
			return;
		}
		
		int index = STATE_INDEX_IDLE;
		
		if ((component.getFlags() & WinComponent.FLAG_ENABLED) == WinComponent.FLAG_ENABLED) {
			if (viewer.isPreview() && component.isActionableComponent()) {
				int state = ((ActionableComponent) component).getState();
				
				if ((state & ActionableComponent.STATE_CLICK) == ActionableComponent.STATE_CLICK
						|| (state & ActionableComponent.STATE_SELECTED) == ActionableComponent.STATE_SELECTED) {
					index = STATE_INDEX_ONCLICK;
				}
				else if ((state & ActionableComponent.STATE_HOVER) == ActionableComponent.STATE_HOVER) {
					index = STATE_INDEX_HOVER;
				}
				else {
					index = STATE_INDEX_IDLE;
				}
			}
			else {
				index = STATE_INDEX_IDLE;
			}
		}
		else {
			index = STATE_INDEX_DISABLED;
		}
		
		float[] uvCoordinates = image.getUVCoords();
		BufferedImage bufferedImage = image.getBufferedImage();
		
		float tileSize = (uvCoordinates[2] - uvCoordinates[0]) / 4.0f;
		
		int[] finalUVCoords = new int[4];
		finalUVCoords[0] = (int) Math.round((uvCoordinates[0] + tileSize * index) * bufferedImage.getWidth());
		finalUVCoords[1] = (int) Math.round(uvCoordinates[1] * bufferedImage.getHeight());
		finalUVCoords[2] = (int) Math.round((uvCoordinates[0] + tileSize * index + tileSize) * bufferedImage.getWidth()); 
		finalUVCoords[3] = (int) Math.round(uvCoordinates[3] * bufferedImage.getHeight());
		
		Image.drawSlicedImage(graphics, bounds, image, finalUVCoords, sliceProportions, new float[] {1.0f, 1.0f}, false);
		
	}
}
