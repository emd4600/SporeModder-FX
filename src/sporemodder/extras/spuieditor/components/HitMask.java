package sporemodder.extras.spuieditor.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreePath;

import sporemodder.extras.spuieditor.ComponentChooser;
import sporemodder.extras.spuieditor.ComponentValueAction;
import sporemodder.extras.spuieditor.ComponentValueAction.ComponentValueListener;
import sporemodder.extras.spuieditor.ImageChooser;
import sporemodder.extras.spuieditor.PanelUtils;
import sporemodder.extras.spuieditor.PanelUtils.EnumValueAction;
import sporemodder.extras.spuieditor.PanelUtils.ShortValueAction;
import sporemodder.extras.spuieditor.PropertiesPanel;
import sporemodder.extras.spuieditor.ResourceLoader;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.UndoableEditor;
import sporemodder.files.formats.ResourceKey;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIFileResource;
import sporemodder.files.formats.spui.SPUIHitMaskResource;
import sporemodder.files.formats.spui.SPUIObject;
import sporemodder.userinterface.JLabelLink;
import sporemodder.userinterface.dialogs.AdvancedFileChooser;
import sporemodder.userinterface.dialogs.AdvancedFileChooser.ChooserType;

public class HitMask extends SPUIDefaultComponent implements SPUIComponent {
	
	private BufferedImage image;
	
	public HitMask(SPUIHitMaskResource resource) throws IOException {
		this.object = resource;
		
		if (resource.getFile() != null) {
			image = ResourceLoader.loadImage(resource.getFile());
		}
	}
	
	public HitMask(SPUIViewer viewer) {
		super(viewer);
		object = new SPUIHitMaskResource();
		
	}

	private HitMask() {
		super();
	}

	@Override
	public HitMask copyComponent(boolean propagateIndependent) {
		HitMask other = new HitMask();
		super.copyComponent(other, propagateIndependent);
		other.object = new SPUIHitMaskResource((SPUIHitMaskResource) object);
		return other;
	}
	
	@Override
	public boolean isUnique() {
		return false;
	}
	
	@Override
	public SPUIObject saveComponent(SPUIBuilder builder)	{
		// we don't add the object so it doesn't get duplicated (it gets added when saving the reference)
		return object;
	}
	
	public boolean isValidPoint(Point p, Rectangle bounds) {
		if (!bounds.contains(p)) {
			return false;
		}
		
		float relativeX = (p.x - bounds.x) / (float) bounds.width;
		float relativeY = (p.y - bounds.y) / (float) bounds.height;
		
		if (image == null) {
			SPUIHitMaskResource resource = (SPUIHitMaskResource) object;
			
			int pos = Math.round(relativeY * resource.getHeight()) * resource.getWidth() + Math.round(relativeX * resource.getWidth());
			int[] rle = resource.getRLEHitMask();
			if (rle == null) {
				return false;
			}
			else {
				for (int i = 0; i < rle.length; i += 2) {
					if (rle[i] < pos && pos < rle[i+1]) {
						return true;
					}
				}
				return false;
			}
		}
		else {
			return isOpaque(image, Math.round(relativeX * image.getWidth()), Math.round(relativeY * image.getHeight()));
		}
	}
	
	public static ComponentChooser<HitMask> getComponentChooser(SPUIViewer viewer) {
		List<Class<? extends HitMask>> list = new ArrayList<Class<? extends HitMask>>();
		list.add(HitMask.class);
		
		return new ComponentChooser<HitMask>(viewer.getEditor(), "Choose a hit mask", false, list, viewer.getEditor());
	}
	
	private void setImage(SPUIFileResource file, JLabelLink labelLink, JPanel previewPanel) {
		SPUIHitMaskResource resource = (SPUIHitMaskResource) object;
		resource.setFile(file);
		
		try {
			// forceLoad
			image = file == null ? null : ResourceLoader.loadImage(file, true);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(viewer, "Error reading image " + file.toString() + "\n" + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		
		labelLink.setText(file == null ? "None" : file.toString());
		labelLink.setActionActive(file != null);
		previewPanel.repaint();
	}
	
	private void showImageChooser(java.awt.Window parent, UndoableEditor editor, final JLabelLink labelLink, final JPanel previewPanel) {
		SPUIHitMaskResource resource = (SPUIHitMaskResource) object;
		SPUIFileResource originalImage = resource.getFile();
		
		ImageChooser chooser = new ImageChooser(parent, "Choose image");
		chooser.setChosenType(ImageChooser.CHOOSER_EXISTING);
		if (originalImage != null) {
			chooser.setSelectedObject(originalImage);
		}
		chooser.setDefaultCloseOperation(ImageChooser.DISPOSE_ON_CLOSE);
		chooser.setVisible(true);
		
		if (!chooser.wasCancelled()) {
			
			SPUIFileResource file;
			TreePath path = chooser.getSelectedObject();
			if (path == null) {
				file = null;
			}
			else {
				file = new SPUIFileResource();
				file.setIsAtlas(false);
				file.getResourceKey().parseTreePath(path);
				file.setRealPath(ResourceKey.getStringFromTreePath(path));
			}
			
			setImage(file, labelLink, previewPanel);
			
			if (file == null && originalImage != null || 
					(file != null && !file.equals(originalImage)) || 
					(originalImage != null && !originalImage.equals(file))) {
				
				editor.addCommandAction(new ComponentValueAction<SPUIFileResource>(originalImage, file, new ComponentValueListener<SPUIFileResource>() {
					@Override
					public void valueChanged(SPUIFileResource value) {
						setImage(value, labelLink, previewPanel);
					}
				}));
			}
		}
	}

	@Override
	public PropertiesPanel getPropertiesPanel() {
		PropertiesPanel panel = new PropertiesPanel();
		setDefaultPropertiesPanel(panel);
		
		final SPUIHitMaskResource resource = (SPUIHitMaskResource) object;
		
		final PropertiesPanel previewPanel = new PropertiesPanel("Preview") {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (image == null) {
					
					int[] indices = resource.getRLEHitMask();
					if (indices != null) {
						int width = resource.getWidth();
						int height = resource.getHeight();
						
						Dimension dim = getSize();
						int x = (dim.width - width) / 2;
						int y = (dim.height - height) / 2;
						
						g.setColor(Color.WHITE);
						g.fillRect(x, y, width, height);
						
						g.setColor(Color.BLACK);
						for (int i = 0; i < indices.length; i+=2) {
							g.drawLine(
									x + indices[i] % width, 
									y + indices[i] / width, 
									x + indices[i + 1] % width, 
									y + indices[i] / width);
						}
					}
				}
				else {
					int width = image.getWidth();
					int height = image.getHeight();
					
					Dimension dim = getSize();
					int x = (dim.width - width) / 2;
					int y = (dim.height - height) / 2;
					
					g.drawImage(image, x, y, null);
				}
			}
			
			@Override
			public Dimension getPreferredSize() {
				if (resource.getFile() == null) {
					return new Dimension(resource.getWidth(), resource.getHeight());
				}
				else {
					BufferedImage image;
					try {
						image = ResourceLoader.loadImage(resource.getFile());
					} catch (IOException e) {
						e.printStackTrace();
						return super.getPreferredSize();
					}
					if (image == null) {
						return super.getPreferredSize();
					}
					else {
						return new Dimension(image.getWidth(), image.getHeight());
					}
				}
			}
		};
		
		final JButton btnExport = new JButton("Export");
		btnExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				AdvancedFileChooser chooser = new AdvancedFileChooser(null, viewer.getEditor(), JFileChooser.FILES_ONLY, false, ChooserType.SAVE, 
						new FileNameExtensionFilter("Portable Network Graphics (PNG)", "png"));
				
				String result = chooser.launch();
				
				if (result != null) {
					exportAsImage(result);
				}
				
			}
		});
		
		final JButton btnImport = new JButton("Import");
		btnImport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				AdvancedFileChooser chooser = new AdvancedFileChooser(null, viewer.getEditor(), JFileChooser.FILES_ONLY, false, ChooserType.SAVE, 
						new FileNameExtensionFilter("Portable Network Graphics (PNG)", "png"));
				
				String result = chooser.launch();
				if (result != null) {
					importFromImage(result);
					previewPanel.repaint();
				}
			}
		});
		
		final PropertiesPanel imagePanel = new PropertiesPanel("Image");
		imagePanel.addShortValue("Image", resource.getFile(), new ShortValueAction() {

			@Override
			public void linkAction(JLabelLink labelLink) {
				showImageChooser(viewer.getEditor(), viewer.getEditor(), labelLink, previewPanel);
			}

			@Override
			public void changeAction(JLabelLink labelLink) {
				showImageChooser(viewer.getEditor(), viewer.getEditor(), labelLink, previewPanel);
			}
		});
		
		panel.addEnumValue("HitMask type", resource.getFile() != null ? 0 : 1, new String[] {"Image", "RLE"}, new EnumValueAction() {
			@Override
			public void valueChanged(int selectedIndex, Object selectedValue) {
				if (selectedIndex == 0) {
					btnExport.setVisible(false);
					btnImport.setVisible(false);
					imagePanel.setVisible(true);
				}
				else {
					btnExport.setVisible(true);
					btnImport.setVisible(true);
					imagePanel.setVisible(false);
					// we must set the image to null
					resource.setFile(null);
				}
				previewPanel.repaint();
			}
		}, viewer.getEditor());
		
		panel.addComponent(btnImport);
		panel.addComponent(btnExport);
		
		panel.addPanel(imagePanel);
		
		if (resource.getFile() == null) {
			btnExport.setVisible(true);
			btnImport.setVisible(true);
			imagePanel.setVisible(false);
		}
		else {
			btnExport.setVisible(false);
			btnImport.setVisible(false);
			imagePanel.setVisible(true);
		}
		
		PanelUtils.addGBC(panel, previewPanel, 0, panel.getNextRow(), GridBagConstraints.WEST, new Insets(0, 0, 5, 0), 2, 1, GridBagConstraints.BOTH, 1.0f, 1.0f);
		
		return panel;
	}
	
	
	private void exportAsImage(String path) {
		SPUIHitMaskResource resource = (SPUIHitMaskResource) object;
		
		BufferedImage img = new BufferedImage(resource.getWidth(), resource.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = img.createGraphics();
		
		int[] indices = resource.getRLEHitMask();
		if (indices != null) {
			int width = resource.getWidth();
			g.setColor(Color.BLACK);
			
			for (int i = 0; i < indices.length; i+=2) {
				g.drawLine(
						indices[i] % width, 
						indices[i] / width, 
						indices[i + 1] % width, 
						indices[i] / width);
			}
		}
		
		try {
			ImageIO.write(img, "PNG", new File(path));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(viewer, "Error writing image:\n" + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	private boolean isOpaque(BufferedImage img, int x, int y) {
		return (((img.getRGB(x, y) & 0xFF000000) >> 24) & 0xFF) >= 128;
	}
	
	private void importFromImage(String path) {
		SPUIHitMaskResource resource = (SPUIHitMaskResource) object;
		
		try {
			BufferedImage img = ImageIO.read(new File(path));
			int width = img.getWidth();
			int height = img.getHeight();
			List<Integer> rle = new ArrayList<Integer>();
			
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					if (isOpaque(img, j, i)) {
						
						rle.add(i * width + j);
						
						while (j+1 < width && isOpaque(img, j, i)) {
							j++;
						}
						
						rle.add(i * width + j);
					}
				}
			}
			
			int[] indices = new int[rle.size()];
			for (int i = 0; i < indices.length; i++) {
				indices[i] = rle.get(i);
			}
			
			resource.setWidth(width);
			resource.setHeight(height);
			resource.setRLEHitMask(indices);
			
		} catch (IOException e) {
			JOptionPane.showMessageDialog(viewer, "Error reading image:\n" + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
}
