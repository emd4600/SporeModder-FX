package sporemodder.extras.spuieditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import sporemodder.extras.spuieditor.ComponentChooser.ComponentChooserCallback;
import sporemodder.extras.spuieditor.ComponentValueAction.ComponentValueListener;
import sporemodder.extras.spuieditor.components.PropertyObject;
import sporemodder.extras.spuieditor.components.SPUIComponent;
import sporemodder.extras.spuieditor.components.SPUIDefaultComponent;
import sporemodder.files.formats.LocalizedText;
import sporemodder.userinterface.HintTextField;
import sporemodder.userinterface.JGradientButton;
import sporemodder.userinterface.JLabelLink;
import sporemodder.utilities.Hasher;

public class PanelUtils {

	public static Component addGBC(JPanel panel, Component component, int gridx, int gridy, int anchor, Insets insets) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = anchor;
		gbc.insets = insets;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		
		panel.add(component, gbc);
		
		return component;
	}
	public static Component addGBC(JPanel panel, Component component, int gridx, int gridy, int anchor, Insets insets, float weightx) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = anchor;
		gbc.insets = insets;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.weightx = weightx;
		
		panel.add(component, gbc);
		
		return component;
	}
	
	public static Component addGBC(JPanel panel, Component component, int gridx, int gridy, int anchor, Insets insets, int gridwidth, int gridheight, int fill) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = gridwidth;
		gbc.gridheight = gridheight;
		gbc.anchor = anchor;
		gbc.fill = fill;
		gbc.insets = insets;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		
		panel.add(component, gbc);
		
		return component;
	}
	public static Component addGBC(JPanel panel, Component component, int gridx, int gridy, int anchor, Insets insets, int gridwidth, int gridheight, int fill, float weightx, float weighty) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = gridwidth;
		gbc.gridheight = gridheight;
		gbc.anchor = anchor;
		gbc.fill = fill;
		gbc.insets = insets;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		
		panel.add(component, gbc);
		
		return component;
	}
	public static Component addGBC(JPanel panel, Component component, int gridx, int gridy, int anchor, Insets insets, int gridwidth, int gridheight, int fill, float weightx) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = gridwidth;
		gbc.gridheight = gridheight;
		gbc.anchor = anchor;
		gbc.fill = fill;
		gbc.insets = insets;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.weightx = weightx;
		
		panel.add(component, gbc);
		
		return component;
	}
	
	public static GridBagLayout setGBLayout(JPanel panel, int[] columnWidths, int[] rowHeights, double[] columnWeights, double[] rowWeights) {
		GridBagLayout gbl = new GridBagLayout();
		gbl.columnWidths = columnWidths;
		gbl.rowHeights = rowHeights;
		gbl.columnWeights = columnWeights;
		gbl.rowWeights = rowWeights;
		panel.setLayout(gbl);
		
		return gbl;
	}
	
	public static JCheckBox addBooleanProperty(JPanel panel, int currentRow, final PropertyObject component, final int property, boolean value, UndoableEditor editor) {
		return PanelUtils.addBooleanValue(panel, currentRow, Hasher.getSPUIName(property), value, new BooleanValueAction() {
			@Override
			public void valueChanged(boolean isSelected) {
				component.getUnassignedProperties().put(property, isSelected);
			}
		}, editor);
	}
	
	public static PropertyInfo addIntProperty(JPanel panel, int currentRow, final PropertyObject component, final int property, int value,  UndoableEditor editor) {
		return PanelUtils.addIntValue(panel, currentRow, Hasher.getSPUIName(property), value, new IntValueAction() {
			@Override
			public void valueChanged(int value) {
				component.getUnassignedProperties().put(property, value);
			}
		}, editor);
	}
	
	public static PropertyInfo addFloatProperty(JPanel panel, int currentRow, final PropertyObject component, final int property, float value,  UndoableEditor editor) {
		return PanelUtils.addFloatFieldValue(panel, currentRow, Hasher.getSPUIName(property), value, new FloatValueAction() {
			@Override
			public void valueChanged(float value) {
				component.getUnassignedProperties().put(property, value);
			}
		}, editor);
	}
	
	public static PropertyInfo addColorProperty(final JPanel panel, int currentRow, final PropertyObject component, final int property, final int value, UndoableEditor editor) {
		return PanelUtils.addColorValue(panel, currentRow, Hasher.getSPUIName(property), "Choose " + Hasher.getSPUIName(property), decodeColor(value), new ColorValueAction() {
			@Override
			public void valueChanged(Color color) {
				component.getUnassignedProperties().put(property, encodeColor(color));
			}
		}, editor);
	}
	
	public static <T extends SPUIComponent> PropertyInfo addShortProperty(JPanel panel, int currentRow, final PropertyObject component, final SPUIViewer viewer, final int property, final Object value, 
			final ComponentChooser<T> chooser, final boolean updateHierarchyTree) {
		
		return PanelUtils.addShortValue(panel, currentRow, Hasher.getSPUIName(property), value, new ShortValueAction() {
			@Override
			public void linkAction(JLabelLink labelLink) {
				if (component.getUnassignedProperties().containsKey(property)) {
					viewer.getEditor().setSelectedComponent((SPUIComponent) component.getUnassignedProperties().get(property));
				}
			}

			@Override
			public void changeAction(JLabelLink labelLink) {
				ComponentChooser.showChooserAction(new ComponentChooserCallback<T>() {
					@SuppressWarnings("unchecked")
					@Override
					public T getValue() {
						if (component.getUnassignedProperties().containsKey(property)) {
							return (T) component.getUnassignedProperties().get(property);
						}
						return null;
					}

					@Override
					public void valueChanged(T value) {
						component.getUnassignedProperties().put(property, value);
						if (updateHierarchyTree) {
							viewer.getEditor().fillHierarchyTree();
							viewer.getEditor().setSelectedComponent(viewer.getActiveComponent());
						}
					}
				}, chooser, labelLink, viewer);
				
			}
		});
	}
	
	public static JPanel addTextProperty(JPanel panel, int currentRow, String propertyName, final LocalizedText text, UndoableEditor editor) {
		return PanelUtils.addTextValue(panel, currentRow, propertyName, text, new TextValueAction() {
			@Override
			public void textChanged(LocalizedText text) {
				// we only need an action so this is added to undo history
			}
		}, editor);
	}
	
	
	public static class FloatTextField extends JTextField {
		
		public FloatTextField() {
			((PlainDocument) getDocument()).setDocumentFilter(new DocumentFilter() {
				public void insertString(DocumentFilter.FilterBypass fb, int offset,
	                    String string, AttributeSet attr) 
	                    		throws BadLocationException {
					
					String str = string;
					StringBuffer sb = new StringBuffer(getText());
					sb.insert(offset, string);
					String sbString = sb.toString();
					// special cases
					if (!sbString.endsWith("e") && !sbString.endsWith("E") && !sbString.endsWith("e-") && !sbString.endsWith("E-")) {
						try {
							Float.parseFloat(sb.toString());
						} catch (NumberFormatException e) {
							str = "";
						}
					}
				
					super.insertString(fb, offset, str, attr);
				}
				
				public void replace(DocumentFilter.FilterBypass fb,
				               int offset, int length, String string, AttributeSet attr) throws BadLocationException {
					if (length > 0) fb.remove(offset, length);
					insertString(fb, offset, string, attr);
				}
			});
		}
	}
	
	public static Color decodeColor(int value) {
		return new Color(
				(int) (value & 0xFF0000) >> 16, 
				(int) (value & 0xFF00) >> 8, 
				(int) value & 0xFF, 
				(int) (((long) value & 0xFF000000L) >> 24));
	}
	
	public static int encodeColor(Color color) {
		return  color.getRed() << 16 |
				color.getGreen() << 8 |
				color.getBlue() |
				color.getAlpha() << 24;
	}
	
	public static interface ColorValueAction {
		public void valueChanged(Color color);
	}
	
	public static PropertyInfo addColorValue(final JPanel panel, int currentRow, String text, final String chooserText, Color value, final ColorValueAction action, final UndoableEditor editor) {
		
		final JGradientButton btnColor = new JGradientButton("...");
		btnColor.setColor(value);
		btnColor.setTransferHandler(new ColorButtonTransferHandler(btnColor, action, editor));
		btnColor.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                JButton button = (JButton) e.getSource();
                TransferHandler handle = button.getTransferHandler();
                handle.exportAsDrag(button, e, TransferHandler.COPY);
            }
        });
		btnColor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JColorChooser cc = new JColorChooser();
				final Color originalColor = btnColor.getColor();
				cc.setColor(originalColor);
				cc.getSelectionModel().addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent arg0) {
						btnColor.setColor(cc.getColor());
						btnColor.repaint();
						if (action != null) {
							action.valueChanged(cc.getColor());
						}
					}
				});
				JDialog dialog = JColorChooser.createDialog(panel, chooserText, true, cc, new ActionListener() {
					// Accept
					@Override
					public void actionPerformed(ActionEvent arg0) {
						btnColor.setColor(cc.getColor());
						if (editor != null) {
							editor.addCommandAction(new ComponentValueAction<Color>(originalColor, cc.getColor(), new ComponentValueListener<Color>() {
								@Override
								public void valueChanged(Color value) {
									btnColor.setColor(value);
									btnColor.repaint();
									if (action != null) {
										action.valueChanged(value);
									}
								}
							}));
						}
					}
				}, new ActionListener() {
					// Cancel
					@Override
					public void actionPerformed(ActionEvent arg0) {
						btnColor.setColor(originalColor);
						btnColor.repaint();
						if (action != null) {
							action.valueChanged(originalColor);
						}
					}
				});
				dialog.setVisible(true);
				dialog.dispose();
			}
		});
		
		PropertyInfo pi = new PropertyInfo(new JLabel(text), btnColor);
		
		PanelUtils.addGBC(panel, pi.label, 0, currentRow, GridBagConstraints.WEST, new Insets(0, 5, 5, 5), 1.0f);
		PanelUtils.addGBC(panel, btnColor, 1, currentRow, GridBagConstraints.EAST, new Insets(0, 0, 5, 0));
		
		return pi;
	}
	
	public static class ColorButtonTransferable implements Transferable {
		public static DataFlavor supportedFlavor;
		private Color value;
		
		static {
			try {
				supportedFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + 
						";class=java.awt.Color");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		public ColorButtonTransferable(Color value) {
			this.value = value;
		}
		
		@Override
		public Object getTransferData(DataFlavor arg0) throws UnsupportedFlavorException, IOException {
			if (arg0.equals(supportedFlavor)) {
				return value;
			}
			return null;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] {supportedFlavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor arg0) {
			return arg0.equals(supportedFlavor);
		}
	}
	
	// class for getting "drag and drop" color functionality for color buttons
	public static class ColorButtonTransferHandler extends TransferHandler {
		
		private JGradientButton btnColor;
		private ColorValueAction action;
		private UndoableEditor editor;
		
		public ColorButtonTransferHandler(JGradientButton btnColor, ColorValueAction action, UndoableEditor editor) {
			this.btnColor = btnColor;
			this.action = action;
			this.editor = editor;
		}
		
		public Color getValue() {
			return btnColor.getColor();
		}
		
		@Override
		public int getSourceActions(JComponent c) {
			return DnDConstants.ACTION_COPY;
		}
		
		@Override
		protected Transferable createTransferable(JComponent c) {
			ColorButtonTransferable t = new ColorButtonTransferable(getValue());
			return t;
		}
		
		@Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            super.exportDone(source, data, action);
            // Decide what to do after the drop has been accepted
        }
		
		@Override
		public boolean canImport(TransferHandler.TransferSupport support) {
			return support.isDataFlavorSupported(ColorButtonTransferable.supportedFlavor);
		}
		
		@Override
		public boolean importData(TransferHandler.TransferSupport support) {
			boolean accept = false;
			
			if (canImport(support)) {
				try {
					Transferable t = support.getTransferable();
					Object value = t.getTransferData(ColorButtonTransferable.supportedFlavor);
					
					if (value instanceof Color) {
						Component component = support.getComponent();
						
						if (component instanceof JGradientButton) {
							Color originalColor = ((JGradientButton) component).getColor();
							((JGradientButton) component).setColor((Color) value);
							
							if (action != null) {
								action.valueChanged((Color) value);
							}
							
							editor.addCommandAction(new ComponentValueAction<Color>(originalColor, (Color) value, new ComponentValueListener<Color>() {
								@Override
								public void valueChanged(Color value) {
									btnColor.setColor(value);
									btnColor.repaint();
									if (action != null) {
										action.valueChanged(value);
									}
								}
							}));
							accept = true;
						}
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			return accept;
		}
	}
	
	
	public static interface FloatValueAction {
		public void valueChanged(float value);
	}
	
	public static PropertyInfo addFloatValue(JPanel panel, int currentRow, String text, float value, Float min, Float max, Float stepSize, final FloatValueAction action, final UndoableEditor editor) {
		final JSpinner spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(new Float(value), min, max, stepSize == null ? new Float(0.1f) : stepSize));
		
		final ChangeListener changeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (action != null) {
					action.valueChanged(((Float) spinner.getValue()).floatValue());
				}
			}
		};
		spinner.addChangeListener(changeListener);
		
		if (editor !=  null) {
			((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().addFocusListener(new FocusListener() {
				float originalValue = 0;
				@Override
				public void focusGained(FocusEvent arg0) {
					originalValue = ((Float) spinner.getValue()).floatValue();
				}
				@Override
				public void focusLost(FocusEvent arg0) {
					float v = ((Float) spinner.getValue()).floatValue();
					if (v != originalValue) {
						editor.addCommandAction(new ComponentValueAction<Float>(originalValue, v, new ComponentValueListener<Float>() {
							@Override
							public void valueChanged(Float value) {
								// disable change listener for this
								spinner.removeChangeListener(changeListener);
								spinner.setValue(value);
								spinner.addChangeListener(changeListener);
								
								if (action != null) {
									action.valueChanged(value);
								}
							}
						}));
					}
				}
			});
		}
		
		PropertyInfo pi = new PropertyInfo(new JLabel(text), spinner);
		
		PanelUtils.addGBC(panel, pi.label, 0, currentRow, GridBagConstraints.WEST, new Insets(0, 5, 5, 5));
		PanelUtils.addGBC(panel, spinner, 1, currentRow, GridBagConstraints.EAST, new Insets(0, 0, 5, 0), 1, 1, GridBagConstraints.HORIZONTAL, 1.0f);
		
		return pi;
	}
	
	// Uses a text field instead of a spinner
	public static PropertyInfo addFloatFieldValue(JPanel panel, int currentRow, String text, float value, final FloatValueAction action, final UndoableEditor editor) {
		
		final FloatTextField textField = new FloatTextField();
		textField.setColumns(15);
		textField.setText(Float.toString(value));
		
		if (action != null) {
			final DocumentListener documentListener = new DocumentListener() {
				private void action() {
					
					try {
						action.valueChanged(Float.parseFloat(textField.getText()));
						textField.setBackground(Color.white);
					}
					catch (Exception e) {
						textField.setBackground(Color.red);
					}
				}
				@Override
				public void changedUpdate(DocumentEvent arg0) {
					action();
				}

				@Override
				public void insertUpdate(DocumentEvent arg0) {
					action();
				}

				@Override
				public void removeUpdate(DocumentEvent arg0) {
					action();
				}
			};
			textField.getDocument().addDocumentListener(documentListener);
			
			textField.addFocusListener(new FocusListener() {
				float originalValue = 0;
				@Override
				public void focusGained(FocusEvent arg0) {
					originalValue = Float.parseFloat(textField.getText());
				}

				@Override
				public void focusLost(FocusEvent arg0) {
					float v = Float.parseFloat(textField.getText());
					if (v != originalValue) {
						editor.addCommandAction(new ComponentValueAction<Float>(originalValue, v, new ComponentValueListener<Float>() {
							@Override
							public void valueChanged(Float value) {
								// disable change listener for this
								textField.getDocument().removeDocumentListener(documentListener);
								textField.setText(Float.toString(value));
								textField.getDocument().addDocumentListener(documentListener);
								
								if (action != null) {
									action.valueChanged(value);
								}
							}
						}));
					}
				}
			});
		}
		
		PropertyInfo pi = new PropertyInfo(new JLabel(text), textField);
		
		addGBC(panel, pi.label, 0, currentRow, GridBagConstraints.WEST, new Insets(0, 5, 5, 5), 1.0f);
		addGBC(panel, textField, 1, currentRow, GridBagConstraints.EAST, new Insets(0, 0, 5, 0));
		
		return pi;
	}
	
	public static interface IntValueAction {
		public void valueChanged(int value);
	}
	
	public static PropertyInfo addIntValue(JPanel panel, int currentRow, String text, String textValue, final IntValueAction action, final UndoableEditor editor) {
		
		final JTextField textField = new JTextField();
		textField.setColumns(15);
		textField.setText(textValue);
		((PlainDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
			public void insertString(DocumentFilter.FilterBypass fb, int offset,
                    String string, AttributeSet attr) 
                    		throws BadLocationException {
			
				super.insertString(fb, offset, Hasher.validateIntString(textField.getText(), offset, string), attr);
			}
			
			public void replace(DocumentFilter.FilterBypass fb,
			               int offset, int length, String string, AttributeSet attr) throws BadLocationException {
				if (length > 0) fb.remove(offset, length);
				insertString(fb, offset, string, attr);
			}
		});
		if (action != null) {
			final DocumentListener documentListener = new DocumentListener() {
				private void action() {
					try {
						action.valueChanged(Hasher.decodeInt(textField.getText()));
						textField.setBackground(Color.white);
					}
					catch (Exception e) {
						e.printStackTrace();
						textField.setBackground(Color.red);
					}
				}
				@Override
				public void changedUpdate(DocumentEvent arg0) {
					action();
				}

				@Override
				public void insertUpdate(DocumentEvent arg0) {
					action();
				}

				@Override
				public void removeUpdate(DocumentEvent arg0) {
					action();
				}
			};
			textField.getDocument().addDocumentListener(documentListener);
			
			textField.addFocusListener(new FocusListener() {
				String originalValue = null;
				
				@Override
				public void focusGained(FocusEvent arg0) {
					originalValue = textField.getText();
				}

				@Override
				public void focusLost(FocusEvent arg0) {
					String text = textField.getText();
					if (!text.equals(originalValue)) {
						editor.addCommandAction(new ComponentValueAction<String>(originalValue, text, new ComponentValueListener<String>() {
							@Override
							public void valueChanged(String value) {
								// disable change listener for this
								textField.getDocument().removeDocumentListener(documentListener);
								textField.setText(value);
								textField.getDocument().addDocumentListener(documentListener);
								
								if (action != null) {
									action.valueChanged(Hasher.decodeInt(value));
								}
							}
						}));
					}
				}
			});
		}
		
		PropertyInfo pi = new PropertyInfo(new JLabel(text), textField);
		
		addGBC(panel, pi.label, 0, currentRow, GridBagConstraints.WEST, new Insets(0, 5, 5, 5), 1.0f);
		addGBC(panel, textField, 1, currentRow, GridBagConstraints.EAST, new Insets(0, 0, 5, 0));
		
		return pi;
	}
	
	public static PropertyInfo addIntValue(JPanel panel, int currentRow, String text, int value, final IntValueAction action, UndoableEditor editor) {
		
		return addIntValue(panel, currentRow, text, Integer.toString(value), action, editor);
	}
	
	public static PropertyInfo addIntSpinnerValue(JPanel panel, int currentRow, String text, int value, 
			Integer min, Integer max, Integer stepSize, final IntValueAction action, final UndoableEditor editor) {
		
		final JSpinner spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(new Integer(value), min, max, stepSize == null ? new Integer(1) : stepSize));
		((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(15);
		final ChangeListener changeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (action != null) {
					action.valueChanged(((Number) spinner.getValue()).intValue());
				}
			}
		};
		spinner.addChangeListener(changeListener);
		
		if (editor !=  null) {
			((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().addFocusListener(new FocusListener() {
				int originalValue = 0;
				@Override
				public void focusGained(FocusEvent arg0) {
					originalValue = ((Number) spinner.getValue()).intValue();
				}
				@Override
				public void focusLost(FocusEvent arg0) {
					int v = ((Number) spinner.getValue()).intValue();
					if (v != originalValue) {
						editor.addCommandAction(new ComponentValueAction<Integer>(originalValue, v, new ComponentValueListener<Integer>() {
							@Override
							public void valueChanged(Integer value) {
								// disable change listener for this
								spinner.removeChangeListener(changeListener);
								spinner.setValue(value);
								spinner.addChangeListener(changeListener);
								
								if (action != null) {
									action.valueChanged(value);
								}
							}
						}));
					}
				}
			});
		}
		
		PropertyInfo pi = new PropertyInfo(new JLabel(text), spinner);
		
		addGBC(panel, pi.label, 0, currentRow, GridBagConstraints.WEST, new Insets(0, 5, 5, 5));
		addGBC(panel, spinner, 1, currentRow, GridBagConstraints.EAST, new Insets(0, 0, 5, 0), 1.0f);
		
		return pi;
	}
	
	public static interface TextFieldValueAction {
		public void documentModified(DocumentEvent event, String textFieldText);
	}
	
	public static PropertyInfo addTextFieldValue(JPanel panel, int currentRow, String text, String value, final TextFieldValueAction action, final UndoableEditor editor) {
		
		final JTextField tfText = new JTextField();
		if (value != null) {
			tfText.setText(value);
		}
		if (action != null) {
			final DocumentListener documentListener = new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent arg0) {
					action.documentModified(arg0, tfText.getText());
				}
				@Override
				public void insertUpdate(DocumentEvent arg0) {
					action.documentModified(arg0, tfText.getText());
				}
				@Override
				public void removeUpdate(DocumentEvent arg0) {
					action.documentModified(arg0, tfText.getText());
				}
			};
			tfText.getDocument().addDocumentListener(documentListener);
			
			tfText.addFocusListener(new FocusListener() {
				String originalValue = null;
				@Override
				public void focusGained(FocusEvent arg0) {
					originalValue = tfText.getText();
				}

				@Override
				public void focusLost(FocusEvent arg0) {
					String v = tfText.getText();
					if (!v.equals(originalValue)) {
						editor.addCommandAction(new ComponentValueAction<String>(originalValue, v, new ComponentValueListener<String>() {
							@Override
							public void valueChanged(String value) {
								// disable change listener for this
								tfText.getDocument().removeDocumentListener(documentListener);
								tfText.setText(value);
								tfText.getDocument().addDocumentListener(documentListener);
								
								if (action != null) {
									action.documentModified(null, value);
								}
							}
						}));
					}
				}
			});
		}
		tfText.setColumns(15);
		
		PropertyInfo pi = new PropertyInfo(new JLabel(text), tfText);
		
		PanelUtils.addGBC(panel, pi.label, 0, currentRow, GridBagConstraints.WEST, new Insets(0, 5, 5, 5), 1.0f);
		PanelUtils.addGBC(panel, tfText, 1, currentRow, GridBagConstraints.EAST, new Insets(0, 0, 5, 0));
		
		return pi;
	}
	
	public static interface EnumValueAction {
		public void valueChanged(int selectedIndex, Object selectedValue);
	}
	
	public static PropertyInfo addEnumValue(JPanel panel, int currentRow, String text, String selectedValue, String[] values, final EnumValueAction action, final UndoableEditor editor) {
		int valueIndex = -1;
		for (int i = 0; i < values.length; i++) {
			if (values[i].equals(selectedValue)) {
				valueIndex = i;
				break;
			}
		}
		return addEnumValue(panel, currentRow, text, valueIndex, values, action, editor);
	}
	
	public static PropertyInfo addEnumValue(JPanel panel, int currentRow, String text, int valueIndex, String[] values, final EnumValueAction action, final UndoableEditor editor) {
		final JComboBox<String> combobox = new JComboBox<String>(values);
		combobox.setSelectedIndex(valueIndex);
		
		if (action != null) {
			final ActionListener actionListener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					action.valueChanged(combobox.getSelectedIndex(), combobox.getSelectedItem());
				}
			};
			combobox.addActionListener(actionListener);
			
			combobox.addFocusListener(new FocusListener() {
				int originalValue = 0;
				@Override
				public void focusGained(FocusEvent arg0) {
					originalValue = combobox.getSelectedIndex();
				}

				@Override
				public void focusLost(FocusEvent arg0) {
					int v = combobox.getSelectedIndex();
					if (v != originalValue) {
						editor.addCommandAction(new ComponentValueAction<Integer>(originalValue, v, new ComponentValueListener<Integer>() {
							@Override
							public void valueChanged(Integer value) {
								// disable change listener for this
								combobox.removeActionListener(actionListener);
								combobox.setSelectedIndex(value);
								combobox.addActionListener(actionListener);
								
								action.valueChanged(value, combobox.getItemAt(value));
							}
						}));
					}
				}
			});
		}
		
		PropertyInfo pi = new PropertyInfo(new JLabel(text), combobox);
		
		PanelUtils.addGBC(panel, pi.label, 0, currentRow, GridBagConstraints.WEST, new Insets(0, 5, 5, 5), 1.0f);
		PanelUtils.addGBC(panel, combobox, 1, currentRow, GridBagConstraints.EAST, new Insets(0, 0, 5, 0));
		
		return pi;
	}
	
	public static interface BooleanValueAction {
		public void valueChanged(boolean isSelected);
	}
	
	public static JCheckBox addBooleanValue(JPanel panel, int currentRow, String text, boolean value, final BooleanValueAction action, final UndoableEditor editor) {
		final JCheckBox checkBox = new JCheckBox(text);
		checkBox.setSelected(value);
		if (action != null) {
			final ActionListener actionListener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					action.valueChanged(checkBox.isSelected());
				}
			};
			checkBox.addActionListener(actionListener);
			
			checkBox.addFocusListener(new FocusListener() {
				boolean originalValue = false;
				@Override
				public void focusGained(FocusEvent arg0) {
					originalValue = checkBox.isSelected();
				}

				@Override
				public void focusLost(FocusEvent arg0) {
					boolean b = checkBox.isSelected();
					if (b != originalValue) {
						editor.addCommandAction(new ComponentValueAction<Boolean>(originalValue, b, new ComponentValueListener<Boolean>() {
							@Override
							public void valueChanged(Boolean value) {
								// disable change listener for this
								checkBox.removeActionListener(actionListener);
								checkBox.setSelected(value);
								checkBox.addActionListener(actionListener);
								
								if (action != null) {
									action.valueChanged(value);
								}
							}
						}));
					}
				}
			});
		}
		
		PanelUtils.addGBC(panel, checkBox, 0, currentRow, GridBagConstraints.WEST, new Insets(0, 5, 5, 5));
		
		return checkBox;
	}
	
	public static interface ShortValueAction {
		public void linkAction(JLabelLink labelLink);
		public void changeAction(JLabelLink labelLink);
	}

	public static PropertyInfo addShortValue(JPanel panel, int currentRow, String text, Object value, final ShortValueAction action) {
		
		final JLabelLink labelLink = new JLabelLink("None", null, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String newText = ((JLabelLink)arg0.getSource()).getText();
				if (newText.equals("None")) {
					((JLabelLink)arg0.getSource()).setToolTipText(null);
				} else {
					((JLabelLink)arg0.getSource()).setToolTipText(newText);
				}
			}
		});
		labelLink.setHorizontalAlignment(SwingConstants.RIGHT);
		//labelLink.setMinimumSize(new Dimension(0, labelLink.getMinimumSize().height));
		//labelLink.setPreferredSize(new Dimension(0, labelLink.getPreferredSize().height));
		JButton btnChange;
		if (false) {
			btnChange = new JButton(">>");
			
			Insets newMargin = new Insets(4, 4, 4, 4);
			Insets oldMargin = btnChange.getMargin();
			
			if (oldMargin != null) {
				newMargin.top = oldMargin.top;
				newMargin.bottom = oldMargin.bottom;
			}
			btnChange.setMargin(newMargin);
		} else {
			btnChange = new JButton("Change");
		}
		
		if (action != null) {
			btnChange.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					action.changeAction(labelLink);
				}
			});
		}
		
		if (value != null) {
			String newLabel = value.toString();
			labelLink.setText(newLabel);
			labelLink.setActionActive(true);
		} else {
			labelLink.setText("None");
			labelLink.setActionActive(false);
		}
		if (action != null) {
			labelLink.setLinkAction(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					action.linkAction(labelLink);
				}
			});
		}
		
		PropertyInfo pi = new PropertyInfo(null, labelLink, btnChange);
		
		if (text == null) {
			PanelUtils.addGBC(panel, labelLink, 0, currentRow, GridBagConstraints.WEST, new Insets(0, 5, 5, 5), 1, 1, GridBagConstraints.BOTH, 1.0f);
			// PanelUtils.addGBC(panel, labelLink, 0, currentRow, GridBagConstraints.WEST, new Insets(0, 5, 5, 5), 1.0f);
			PanelUtils.addGBC(panel, btnChange, 1, currentRow, GridBagConstraints.EAST, new Insets(0, 0, 5, 0));
		}
		else {
			pi.label = new JLabel(text);
			PanelUtils.addGBC(panel, pi.label, 0, currentRow, GridBagConstraints.WEST, new Insets(0, 5, 5, 5)); /* 1.0f */
			PanelUtils.addGBC(panel, labelLink, 1, currentRow, GridBagConstraints.EAST, new Insets(0, 5, 5, 5), 1, 1, GridBagConstraints.BOTH, 1.0f);
			// PanelUtils.addGBC(panel, labelLink, 1, currentRow, GridBagConstraints.EAST, new Insets(0, 0, 5, 5), 1.0f);
			PanelUtils.addGBC(panel, btnChange, 2, currentRow, GridBagConstraints.EAST, new Insets(0, 0, 5, 0));
		}
		
		return pi;
	}
	
public static PropertyInfo addLinkValue(JPanel panel, int currentRow, String text, Object value, final ShortValueAction action) {
		
		final JLabelLink labelLink = new JLabelLink("None", null);
		labelLink.setHorizontalAlignment(SwingConstants.RIGHT);
		
		if (value != null) {
			labelLink.setText(value.toString());
			labelLink.setActionActive(true);
		} else {
			labelLink.setText("None");
			labelLink.setActionActive(false);
		}
		if (action != null) {
			labelLink.setLinkAction(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					action.linkAction(labelLink);
				}
			});
		}
		
		PropertyInfo pi = new PropertyInfo(new JLabel(text), labelLink);
		
		PanelUtils.addGBC(panel, pi.label, 0, currentRow, GridBagConstraints.WEST, new Insets(0, 5, 5, 5)); /* 1.0f */
		PanelUtils.addGBC(panel, labelLink, 1, currentRow, GridBagConstraints.EAST, new Insets(0, 5, 5, 5), 1, 1, GridBagConstraints.BOTH, 1.0f);
		
		return pi;
	}
	
	public static void addPanel(JPanel parent, JPanel panel, int currentRow) {
		PanelUtils.addGBC(parent, panel, 0, currentRow++, GridBagConstraints.WEST, new Insets(5, 0, 5, 0), 2, 1, GridBagConstraints.HORIZONTAL, 1.0f);
	}
	
	public static void addComponent(JPanel parent, JComponent component, int currentRow) {
		PanelUtils.addGBC(parent, component, 0, currentRow++, GridBagConstraints.WEST, new Insets(0, 0, 5, 0));
	}
	
	public static void removePanel(JPanel parent, JPanel panel) {
		parent.remove(panel);
	}
	
	public static interface TextValueAction {
		public void textChanged(LocalizedText text);
	}
	
	public static JPanel addTextValue(JPanel parentPanel, int currentRow, String title, final LocalizedText value, final TextValueAction action, final UndoableEditor editor) {
		final LocalizedText text = new LocalizedText((String) null);
		text.copy(value);
		
		JPanel textPanel = new JPanel();
		if (title != null) {
			textPanel.setBorder(BorderFactory.createTitledBorder(title));
		}
		
		PanelUtils.addGBC(parentPanel, textPanel, 0, currentRow, GridBagConstraints.WEST, new Insets(0, 0, 5, 0), 2, 1, GridBagConstraints.HORIZONTAL);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		textPanel.setLayout(gridBagLayout);
		
		final JRadioButton rdbtnText = new JRadioButton("Text: ");
		PanelUtils.addGBC(textPanel, rdbtnText, 0, 0, GridBagConstraints.WEST, new Insets(0, 5, 5, 5));
		
		final JTextField tfText = new JTextField();
		tfText.setColumns(10);
		
		PanelUtils.addGBC(textPanel, tfText, 1, 0, GridBagConstraints.WEST, new Insets(0, 5, 5, 5), 2, 1, GridBagConstraints.HORIZONTAL);
		
		final JRadioButton rdbtnLocale = new JRadioButton("Locale:");
		PanelUtils.addGBC(textPanel, rdbtnLocale, 0, 1, GridBagConstraints.WEST, new Insets(0, 5, 5, 5));
		
		final HintTextField tfTableID = new HintTextField("tableID");
		tfTableID.setEnabled(false);
		tfTableID.setColumns(10);
		PanelUtils.addGBC(textPanel, tfTableID, 1, 1, GridBagConstraints.WEST, new Insets(0, 5, 5, 5), 1, 1, GridBagConstraints.HORIZONTAL, 1.0f);
		
		final HintTextField tfInstanceID = new HintTextField("instanceID");
		tfInstanceID.setEnabled(false);
		tfInstanceID.setColumns(10);
		PanelUtils.addGBC(textPanel, tfInstanceID, 2, 1, GridBagConstraints.WEST, new Insets(0, 5, 5, 5), 1, 1, GridBagConstraints.HORIZONTAL, 1.0f);
		
		
		if (text.tableID == -1 && text.instanceID == -1) {
			rdbtnText.setSelected(true);
			tfText.setEnabled(true);
			tfTableID.setEnabled(false);
			tfInstanceID.setEnabled(false);
			tfText.setText(text.text);
		} else {
			rdbtnLocale.setSelected(true);
			tfTableID.setText(Hasher.getFileName(text.tableID));
			tfInstanceID.setText(Hasher.getFileName(text.instanceID));
			tfText.setEnabled(false);
			tfTableID.setEnabled(true);
			tfInstanceID.setEnabled(true);
		}
		
		
		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnText);
		group.add(rdbtnLocale);
		
		final ItemListener rdbtnTextListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				tfText.setEnabled(rdbtnText.isSelected());
				tfInstanceID.setEnabled(rdbtnLocale.isSelected());
				tfTableID.setEnabled(rdbtnLocale.isSelected());
				if (rdbtnText.isSelected()) {
					text.text = tfText.getText();
					text.tableID = -1;
					text.instanceID = -1;
				} else {
					text.text = null;
					text.tableID = Hasher.getFileHash(tfTableID.getText());
					text.instanceID = Hasher.getFileHash(tfInstanceID.getText());
				}
				if (action != null) {
					action.textChanged(text);
				}
			}
		};
		rdbtnText.addItemListener(rdbtnTextListener);
		
		final ItemListener rdbtnLocaleListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				tfText.setEnabled(rdbtnText.isSelected());
				tfInstanceID.setEnabled(rdbtnLocale.isSelected());
				tfTableID.setEnabled(rdbtnLocale.isSelected());
				if (rdbtnText.isSelected()) {
					text.tableID = -1;
					text.instanceID = -1;
				} else {
					text.tableID = Hasher.getFileHash(tfTableID.getText());
					text.instanceID = Hasher.getFileHash(tfInstanceID.getText());
				}
				if (action != null) {
					action.textChanged(text);
				}
			}
		};
		rdbtnLocale.addItemListener(rdbtnLocaleListener);
		
		final DocumentListener tfTextListener = new DocumentListener() {
			private void action() {
				text.text = tfText.getText();
				text.tableID = -1;
				text.instanceID = -1;
				if (action != null) {
					action.textChanged(text);
				}
			}
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				action();
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				action();
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				action();
			}
		};
		tfText.getDocument().addDocumentListener(tfTextListener);
		
		final DocumentListener tfTableIDListener = new DocumentListener() {
			private void action() {
				text.tableID = Hasher.getFileHash(tfTableID.getText());
				if (action != null) {
					action.textChanged(text);
				}
			}
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				action();
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				action();
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				action();
			}
		};
		tfTableID.getDocument().addDocumentListener(tfTableIDListener);
		
		final DocumentListener tfInstanceIDListener = new DocumentListener() {
			private void action() {
				text.instanceID = Hasher.getFileHash(tfInstanceID.getText());
				if (action != null) {
					action.textChanged(text);
				}
			}
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				action();
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				action();
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				action();
			}
		};
		tfInstanceID.getDocument().addDocumentListener(tfInstanceIDListener);
		
		if (action != null) {
			final LocalizedText temporaryText = new LocalizedText(text);
			FocusListener focusListener = new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent arg0) {
					if (editor != null && ((text.text != null && !text.text.equals(temporaryText.text)) || text.tableID != temporaryText.tableID || text.instanceID != temporaryText.instanceID)) {
						
						rdbtnText.removeItemListener(rdbtnTextListener);
						rdbtnLocale.removeItemListener(rdbtnLocaleListener);
						tfText.getDocument().removeDocumentListener(tfTextListener);
						tfTableID.getDocument().removeDocumentListener(tfTableIDListener);
						tfInstanceID.getDocument().removeDocumentListener(tfInstanceIDListener);
						
						// we must use a copy of the localized text so it isn't modified
						editor.addCommandAction(new ComponentValueAction<LocalizedText>(new LocalizedText(temporaryText), text, new ComponentValueListener<LocalizedText>() {
							@Override
							public void valueChanged(LocalizedText value) {
								text.copy(value);
								if (text.tableID == -1 && text.instanceID == -1) {
									rdbtnText.setSelected(true);
									tfText.setEnabled(true);
									tfTableID.setEnabled(false);
									tfInstanceID.setEnabled(false);
									tfText.setText(text.text);
									tfTableID.setText(null);
									tfInstanceID.setText(null);
								} else {
									rdbtnLocale.setSelected(true);
									tfTableID.setText(Hasher.getFileName(text.tableID));
									tfInstanceID.setText(Hasher.getFileName(text.instanceID));
									tfText.setEnabled(false);
									tfTableID.setEnabled(true);
									tfInstanceID.setEnabled(true);
								}
								action.textChanged(text);
							}
						}));
						
						rdbtnText.addItemListener(rdbtnTextListener);
						rdbtnLocale.addItemListener(rdbtnLocaleListener);
						tfText.getDocument().addDocumentListener(tfTextListener);
						tfTableID.getDocument().addDocumentListener(tfTableIDListener);
						tfInstanceID.getDocument().addDocumentListener(tfInstanceIDListener);
						
						temporaryText.copy(text);
					}
				}
			};
			
			rdbtnText.addFocusListener(focusListener);
			rdbtnLocale.addFocusListener(focusListener);
			tfText.addFocusListener(focusListener);
			tfTableID.addFocusListener(focusListener);
			tfInstanceID.addFocusListener(focusListener);
		}
		
		return textPanel;
	}
	
	public static class PropertyInfo {
		public PropertyInfo(JLabel label, JComponent ... components) {
			this.label = label;
			this.components = components;
		}
		
		public JLabel label;
		public JComponent[] components;
		
		public void setVisible(boolean isVisible) {
			if (label != null) {
				label.setVisible(isVisible);
			}
			for (JComponent component : components) {
				component.setVisible(isVisible);
			}
		}
		
		public void setTooltip(String tooltipText) {
			if (label != null) {
				label.setToolTipText(tooltipText);
			}
			// we have disabled this because it's annoying
//			for (JComponent component : components) {
//				component.setToolTipText(tooltipText);
//			}
		}
	}
	
	
	public static void addMarginsValue(PropertiesPanel parentPanel, String text, final float[] values, SPUIViewer viewer) {
		PropertiesPanel panel = new PropertiesPanel(text);
		parentPanel.addPanel(panel);
		
		for (int i = 0; i < values.length; i++) {
			final int index = i;
			panel.addFloatValue(SPUIDefaultComponent.getConstantString(i), values[i], null, null, 1f, new FloatValueAction() {
				@Override
				public void valueChanged(float value) {
					values[index] = value;
				}
			}, viewer.getEditor());
		}
	}
}
