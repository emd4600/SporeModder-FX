package sporemodder.extras.spuieditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import sporemodder.extras.spuieditor.ComponentValueAction.ComponentValueListener;
import sporemodder.extras.spuieditor.PanelUtils.BooleanValueAction;
import sporemodder.extras.spuieditor.PanelUtils.FloatTextField;
import sporemodder.extras.spuieditor.PanelUtils.FloatValueAction;
import sporemodder.extras.spuieditor.PanelUtils.IntValueAction;
import sporemodder.extras.spuieditor.components.Image;
import sporemodder.files.formats.spui.SPUIFileResource;

public class UVEditor extends JDialog implements ActionListener, UndoableEditor, WindowListener {
	
	/**
	 * The minimum value the zoom can have. The image won't be zoomed out if the zoom is below this number.
	 * <b>Note:</b> A zoom value of 1.0 is the real scale image.
	 */
	private static final float MIN_ZOOM = 0.25f;
	/**
	 * The maximum value the zoom can have. The image won't be zoomed in if the zoom is above this number.
	 * <b>Note:</b> A zoom value of 1.0 is the real scale image.
	 */
	private static final float MAX_ZOOM = 10.0f;
	
	/**
	 * This value determines how many zoom is increased/decreased each time the zoom key is pressed.
	 */
	private static final float ZOOM_STEP = 0.25f;
	
//	private static final Dimension IMAGEPANEL_MIN = new Dimension(256, 256);
//	
//	private static final Dimension PREVIEW_SIZE = new Dimension(128, 128);
	
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.########");;
	
	
	
	private final List<CommandAction> actions = new ArrayList<CommandAction>();
	private int currentAction;
	
	private final JPanel contentPanel = new JPanel();
	
	private UVEditorImagePanel imagePanel;
	private ImagePreviewPanel imagePreviewPanel;
	
	private final SpinnerModel zoomSpinnerModel = new SpinnerNumberModel(1, MIN_ZOOM, MAX_ZOOM, ZOOM_STEP);
	private JSpinner spinnerZoom;
	
	private final FloatTextField[] coordinateFields = new FloatTextField[4];
	private JCheckBox cbAutoDimensions;
	private JSpinner spinnerWidth;
	private JSpinner spinnerHeight;
	
	private JMenuItem mntmUndo;
	private JMenuItem mntmRedo;
	
	private Image image;
	private Image originalImage;
	
	private boolean snapToPixels;

	public UVEditor(Window parent, final Image image) {
		super(parent);
		
		this.image = image;
		this.originalImage = image.copyComponent(false);
		
		setTitle("UV Editor");
		setSize(601, 373);
		setModalityType(ModalityType.TOOLKIT_MODAL);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		addWindowListener(this);
		
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setLayout(new BorderLayout(0, 0));
		
		imagePanel = new UVEditorImagePanel(this);
		contentPanel.add(new JScrollPane(imagePanel), BorderLayout.CENTER);
		
		PropertiesPanel dataPanel = new PropertiesPanel("Data");
		contentPanel.add(dataPanel, BorderLayout.EAST);
		
		PropertiesPanel panelUVs = new PropertiesPanel("UV Coordinates");
		dataPanel.addPanel(panelUVs);
		
		final float[] uvCoordinates = image.getUVCoords();
		
		coordinateFields[0] = (FloatTextField) panelUVs.addFloatFieldValue("Top-left X", uvCoordinates[0], new FloatValueAction() {
			@Override
			public void valueChanged(float value) {
				uvCoordinates[0] = value;
				uvCoordinatesChanged();
			}
		}, this).components[0];
		
		coordinateFields[1] = (FloatTextField) panelUVs.addFloatFieldValue("Top-left Y", uvCoordinates[1], new FloatValueAction() {
			@Override
			public void valueChanged(float value) {
				uvCoordinates[1] = value;
				uvCoordinatesChanged();
			}
		}, this).components[0];
		
		coordinateFields[2] = (FloatTextField) panelUVs.addFloatFieldValue("Bottom-right X", uvCoordinates[2], new FloatValueAction() {
			@Override
			public void valueChanged(float value) {
				uvCoordinates[2] = value;
				uvCoordinatesChanged();
			}
		}, this).components[0];
		
		coordinateFields[3] = (FloatTextField) panelUVs.addFloatFieldValue("Bottom-right Y", uvCoordinates[3], new FloatValueAction() {
			@Override
			public void valueChanged(float value) {
				uvCoordinates[3] = value;
				uvCoordinatesChanged();
			}
		}, this).components[0];
		
		
		cbAutoDimensions = dataPanel.addBooleanValue("Automatic dimensions", true, new BooleanValueAction() {
			@Override
			public void valueChanged(boolean isSelected) {
				updateDimensions(isSelected);
				spinnerWidth.setEnabled(!isSelected);
				spinnerHeight.setEnabled(!isSelected);
			}
		}, this);
		
		PropertiesPanel panelDimensions = new PropertiesPanel("Dimensions");
		dataPanel.addPanel(panelDimensions);
		final Dimension dim = image.getDimensions();
		
		spinnerWidth = (JSpinner) panelDimensions.addIntSpinnerValue("Width", dim.width, null, null, null, new IntValueAction() {
			@Override
			public void valueChanged(int value) {
				dim.width = value;
				updateDisplay();
			}
		}, this).components[0];
		spinnerWidth.setEnabled(false);
		
		spinnerHeight = (JSpinner) panelDimensions.addIntSpinnerValue("Height", dim.height, null, null, null, new IntValueAction() {
			@Override
			public void valueChanged(int value) {
				dim.height = value;
				updateDisplay();
			}
		}, this).components[0];
		spinnerHeight.setEnabled(false);
		
		dataPanel.addIntProperty(image, 0x01BE0004, this);
		
		
		imagePreviewPanel = new ImagePreviewPanel(image);
		PanelUtils.addGBC(dataPanel, new JScrollPane(imagePreviewPanel), 0, dataPanel.getNextRow(), GridBagConstraints.NORTH, new Insets(0, 0, 5, 0), 2, 1, GridBagConstraints.BOTH, 1.0f, 1.0f);
		
		final JSpinner spinnerPreviewZoom = new JSpinner(new SpinnerNumberModel(1, MIN_ZOOM, MAX_ZOOM, ZOOM_STEP));
		spinnerPreviewZoom.setEditor(new JSpinner.NumberEditor(spinnerPreviewZoom, "####%"));
		spinnerPreviewZoom.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				imagePreviewPanel.setZoomLevel(((Double) spinnerPreviewZoom.getValue()).floatValue());
			}
		});
		PanelUtils.addGBC(dataPanel, new JScrollPane(spinnerPreviewZoom), 0, dataPanel.getNextRow(), GridBagConstraints.SOUTH, new Insets(0, 0, 5, 0), 2, 1, GridBagConstraints.HORIZONTAL, 1.0f);
		
		
		JPanel infoPanel = new JPanel();
		contentPanel.add(infoPanel, BorderLayout.SOUTH);
		infoPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
		infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		spinnerZoom = new JSpinner(zoomSpinnerModel);
		spinnerZoom.setEditor(new JSpinner.NumberEditor(spinnerZoom, "####%"));
		spinnerZoom.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				imagePanel.setZoomLevel(((Double) spinnerZoom.getValue()).floatValue());
			}
		});
		
		infoPanel.add(new JLabel("Zoom: "));
		infoPanel.add(spinnerZoom);
		
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		
		JButton okButton = new JButton("OK");
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(this);
		buttonPane.add(cancelButton);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmChangeImage = new JMenuItem("Change image...");
		mntmChangeImage.setActionCommand("Change_image");
		mnFile.add(mntmChangeImage);
		
		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);
		
		
		mntmUndo = new JMenuItem("Undo");
		mntmUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		mntmUndo.setEnabled(false);
		mntmUndo.setActionCommand("Undo");
		mntmUndo.addActionListener(this);
		mnEdit.add(mntmUndo);
		
		mntmRedo = new JMenuItem("Redo");
		mntmRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
		mntmRedo.setEnabled(false);
		mntmRedo.addActionListener(this);
		mntmRedo.setActionCommand("Redo");
		mnEdit.add(mntmRedo);
		
		mnEdit.addSeparator();
		
		final JCheckBoxMenuItem mntmSnapToPixels = new JCheckBoxMenuItem("Snap to pixels");
		mntmSnapToPixels.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setSnapToPixels(mntmSnapToPixels.isSelected());
				updateDisplay();
			}
		});
		mnEdit.add(mntmSnapToPixels);
		
		pack();
		setLocationRelativeTo(null);
	}
	
	public void updateDisplay() {
		image.getPreviewPanel().revalidate();
		image.getPreviewPanel().repaint();
		image.getSPUIViewer().repaint();
		imagePanel.repaint();
		imagePreviewPanel.revalidate();
		imagePreviewPanel.repaint();
	}
	
	public void updateDimensions(boolean automatic) {
		if (automatic) {
			BufferedImage bufferedImage = image.getBufferedImage();
			
			float[] uvCoords = null;
			if (snapToPixels) {
				uvCoords = new float[4];
				image.snapToPixels(uvCoords);
			} else {
				uvCoords = image.getUVCoords();;
			}
			
			spinnerWidth.setValue((uvCoords[2] - uvCoords[0]) * bufferedImage.getWidth());
			spinnerHeight.setValue((uvCoords[3] - uvCoords[1]) * bufferedImage.getHeight());
		}
	}
	
	public void updateCoordinates() {
		float[] uvCoords = image.getUVCoords();
		for (int i = 0; i < uvCoords.length; i++) {
			coordinateFields[i].setText(DECIMAL_FORMAT.format(uvCoords[i]));
		}
		updateDimensions(cbAutoDimensions.isSelected());
	}
	
	private void uvCoordinatesChanged() {
		updateDimensions(cbAutoDimensions.isSelected());
		updateDisplay();
	}
	
	private void restoreOriginalImage(Image destImage, Image sourceImage) {
		float[] uvCoords = destImage.getUVCoords();
		float[] originalUVCoords = sourceImage.getUVCoords();
		
		for (int i = 0; i < originalUVCoords.length; i++) {
			uvCoords[i] = originalUVCoords[i];
		}
		
		Dimension dim = destImage.getDimensions();
		Dimension originalDim = sourceImage.getDimensions();
		
		dim.width = originalDim.width;
		dim.height = originalDim.height;
		
		destImage.setResource(new SPUIFileResource(sourceImage.getResource()));
		
		destImage.getUnassignedProperties().put(0x01BE0004, sourceImage.getUnassignedProperties().get(0x01BE0004));
		
		updateDisplay();
	}
	
	private void updateUndoRedoButtons() {
		if (actions.isEmpty()) {
			mntmUndo.setEnabled(false);
			mntmRedo.setEnabled(false);
		}
		else {
			mntmUndo.setEnabled(currentAction >= 0);
			mntmRedo.setEnabled(currentAction < actions.size() - 1);
		}
	}
	
	@Override
	public void undo() {
		actions.get(currentAction).undo();
		currentAction--;
		updateUndoRedoButtons();
		updateDisplay();
	}
	
	@Override
	public void redo() {
		// we have to execute the next action
		actions.get(++currentAction).redo();
		updateUndoRedoButtons();
		updateDisplay();
	}
	
	@Override
	public void addCommandAction(CommandAction action) {
		if (actions.size() == 0) {
			actions.add(action);
			currentAction = 0;
		}
		else {
			// we don't want to shift the current action
			actions.add(++currentAction, action);
		}
		updateUndoRedoButtons();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case "OK":
			if (snapToPixels) {
				image.snapToPixels(image.getUVCoords());
			}
			if (image.getSPUIViewer() != null && image.getSPUIViewer().getEditor() != null) {
				image.getSPUIViewer().getEditor().addCommandAction(new ComponentValueAction<Image>(originalImage, image.copyComponent(false), new ComponentValueListener<Image>() {
					@Override
					public void valueChanged(Image value) {
						restoreOriginalImage(image, value);
					}
				}));
			}
			dispose();
			return;
			
		case "Cancel":
			restoreOriginalImage(image, originalImage);
			dispose();
			return;
			
		case "Undo":
			undo();
			break;
			
		case "Redo":
			redo();
			break;
			
		case "Change_image":
			image.showImageChooser(this, this);
			updateDisplay();
			break;
		}
		
	}

	public Image getImage() {
		return image;
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		restoreOriginalImage(image, originalImage);
		dispose();
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
	}

	public boolean isSnapToPixels() {
		return snapToPixels;
	}
	
	public void setSnapToPixels(boolean value) {
		this.snapToPixels = value;
	}
}
