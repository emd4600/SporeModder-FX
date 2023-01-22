package sporemodder.extras.spuieditor.components;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import sporemodder.extras.spuieditor.PropertiesPanel;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.components.SPUIWinProc.SPUIDefaultWinProc;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUINumberSections.SectionByte;

public class SimpleLayout extends SPUIDefaultWinProc {
	
	public static final int TYPE = 0x2F418D72;
	
	/**
	 * If true, the right side of the component will be aligned with its parent right side. 
	 */
	public static final int FLAG_RIGHT = 0x8;
	/**
	 * If true, the left side of the component will be aligned with its parent right side (therefore respecting the original width). 
	 */
	public static final int FLAG_LEFT = 0x4;
	/**
	 * If true, the bottom side of the component will be aligned with its parent bottom side. 
	 */
	public static final int FLAG_BOTTOM = 0x2;
	/**
	 * If true, the top side of the component will be aligned with its parent bottom side (therefore respecting the original height).  
	 */
	public static final int FLAG_TOP = 0x1;
	
	private int flags;

	public SimpleLayout(SPUIBlock block) throws InvalidBlockException {
		super(block);
		
		flags = SectionByte.getValues(block.getSection(0x0F3D0000, SectionByte.class), new byte[1], 1)[0];
	}
	
	@Override
	public SPUIBlock saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		builder.addByte(block, 0x0F3D0000, new byte[] {(byte) flags}); 
		
		return block;
	}
	
	public SimpleLayout(SPUIViewer viewer, int flags) {
		this(viewer);
		this.flags = flags;
	}
	
	public SimpleLayout(SPUIViewer viewer) {
		super(viewer);
		flags = 0;
	}
	
	private SimpleLayout() {
		super();
	}
	
	@Override
	public SimpleLayout copyComponent(boolean propagateIndependent) {
		SimpleLayout other = new SimpleLayout();
		copyComponent(other, propagateIndependent);
		other.flags = flags;
		return other;
	}

	@Override
	public void modify(WinComponent component) {
		
		WinComponent parent = component.getParent();
		if (parent == null) {
			return;
		}
		
		Rectangle parentBounds = parent.getRealBounds();
		
		Rectangle result = component.getRealBounds();
		
		// this is what spore does
		if ((flags & FLAG_RIGHT) == FLAG_RIGHT) {
			result.width += parentBounds.width;

			if ((flags & FLAG_LEFT) == 0) {
				result.x += parentBounds.width;
				result.width -= parentBounds.width;
			}
		}
		
		if ((flags & FLAG_BOTTOM) == FLAG_BOTTOM) {
			result.height += parentBounds.height;

			if ((flags & FLAG_TOP) == 0) {
				result.y += parentBounds.height;
				result.height -= parentBounds.height;
			}
		}
	}

	@Override
	public PropertiesPanel getPropertiesPanel() {
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		JLabel lblHorizontal = new JLabel("Horizontal layout:");
		lblHorizontal.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		
		HashMap<String, ImageIcon> horizontalIcons = new HashMap<String, ImageIcon>();
		horizontalIcons.put("Left", createImageIcon("/sporemodder/extras/spuieditor/resources/layout_left.png", null));
		horizontalIcons.put("Right", createImageIcon("/sporemodder/extras/spuieditor/resources/layout_right.png", null));
		horizontalIcons.put("Fill", createImageIcon("/sporemodder/extras/spuieditor/resources/layout_horizontal_fill.png", null));
		
		final JComboBox<String> comboboxHorizontal = new JComboBox<String>(new String[] {
				"Left",
				"Right",
				"Fill"
		});
		if ((flags & 0x8) == 0x8) {
			if ((flags & 0x4) == 0x4) {
				comboboxHorizontal.setSelectedItem("Fill");
			} else {
				comboboxHorizontal.setSelectedItem("Right");
			}
		} else {
			comboboxHorizontal.setSelectedItem("Left");
		}
		comboboxHorizontal.setRenderer(new IconCellRenderer(horizontalIcons));
		comboboxHorizontal.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String item = (String) comboboxHorizontal.getSelectedItem();
				switch(item) {
				case "Left": flags &= ~(FLAG_RIGHT | FLAG_LEFT); break;
				case "Right": flags = (flags & ~FLAG_LEFT) | FLAG_RIGHT; break;
				case "Fill": flags |= (FLAG_RIGHT | FLAG_LEFT); break;
				default: break;
				}
				parent.revalidate();
				viewer.repaint();
			}
		});
		comboboxHorizontal.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		
		
		JLabel lblVertical = new JLabel("Vertical layout:");
		lblVertical.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		
		HashMap<String, ImageIcon> verticalIcons = new HashMap<String, ImageIcon>();
		verticalIcons.put("Top", createImageIcon("/sporemodder/extras/spuieditor/resources/layout_top.png", null));
		verticalIcons.put("Bottom", createImageIcon("/sporemodder/extras/spuieditor/resources/layout_bottom.png", null));
		verticalIcons.put("Fill", createImageIcon("/sporemodder/extras/spuieditor/resources/layout_vertical_fill.png", null));
		
		final JComboBox<String> comboboxVertical = new JComboBox<String>(new String[] {
				"Top",
				"Bottom",
				"Fill"
		});
		if ((flags & 0x2) == 0x2) {
			if ((flags & 0x1) == 0x1) {
				comboboxVertical.setSelectedItem("Fill");
			} else {
				comboboxVertical.setSelectedItem("Bottom");
			}
		} else {
			comboboxVertical.setSelectedItem("Top");
		}
		comboboxVertical.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String item = (String) comboboxVertical.getSelectedItem();
				switch(item) {
				case "Top": flags &= ~(FLAG_BOTTOM | FLAG_TOP); break;
				case "Bottom": flags = (flags & ~FLAG_TOP) | FLAG_BOTTOM; break;
				case "Fill": flags |= (FLAG_BOTTOM | FLAG_TOP); break;
				default: break;
				}
				parent.revalidate();
				viewer.repaint();
			}
		});
		comboboxVertical.setRenderer(new IconCellRenderer(verticalIcons));
		comboboxVertical.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		
		panel.add(lblHorizontal);
		panel.add(comboboxHorizontal);
		panel.add(Box.createVerticalStrut(10));
		panel.add(lblVertical);
		panel.add(comboboxVertical);
		panel.add(Box.createVerticalGlue());
		
		// to avoid elements from stretching vertically
		PropertiesPanel container = super.getPropertiesPanel();
		container.addPanel(panel);
		
		return container;
	}
	
	private class IconCellRenderer extends JLabel implements ListCellRenderer<String> {
		private HashMap<String, ImageIcon> icons;
		
		public IconCellRenderer(HashMap<String, ImageIcon> icons) {
			this.icons = icons;
		}
		
		@Override
		public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {

	        if (isSelected) {
	        	setOpaque(true);
	        	setBackground(list.getSelectionBackground());
	        	setForeground(list.getSelectionForeground());
	        } else {
	        	setOpaque(false);
	        	setBackground(list.getBackground());
	            setForeground(list.getForeground());
	        }

	        ImageIcon icon = icons.get(value);
	        setIcon(icon);
	        setText(value);

	        return this;
		}

	}
	
	/** Returns an ImageIcon, or null if the path was invalid. */
	protected ImageIcon createImageIcon(String path,
	                                           String description) {
	    java.net.URL imgURL = getClass().getResource(path);
	    if (imgURL != null) {
	        return new ImageIcon(imgURL, description);
	    } else {
	        System.err.println("Couldn't find file: " + path);
	        return null;
	    }
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}
	
	
}
