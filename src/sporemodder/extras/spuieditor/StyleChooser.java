package sporemodder.extras.spuieditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import sporemodder.MainApp;
import sporemodder.utilities.FilteredListModel;
import sporemodder.utilities.SearchSpec;

public class StyleChooser extends JDialog {
	
	public interface StyleValueAction {
		public void styleChanged(String styleName);
	}
	
	private static final String PREVIEW_TEXT = "AaBbCcDdEeFfGg\r\n1234567890\r\n!@#%^&*()";
	private static final String NO_PREVIEW_TEXT = "NO PREVIEW\r\nThe specified style does not exist.";
	
	private static final Color DEFAULT_COLOR = Color.black;
	private static final Color NO_PREVIEW_COLOR = Color.red;

	private final JPanel contentPanel = new JPanel();
	private JTextField tfManualEntry;
	private JTextField tfSearch;
	private JList<StyleSheetInstance> stylesList;
	private FilteredListModel<StyleSheetInstance> listModel;
	
	private JRadioButton rdbtnManualEntry;
	private JRadioButton rdbtnSelectExisting;
	
	private JPanel previewPanel;
	
	private StyleValueAction action;
	
	private boolean wasCancelled;
	
//	/**
//	 * Launch the application.
//	 */
//	public static void main(String[] args) {
//		
//		try {
//			SPUIEditor.loadStyleSheet();
//			
//			StyleChooser dialog = new StyleChooser(null, "Choose a style");
//			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//			dialog.setVisible(true);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * Create the dialog.
	 */
	public StyleChooser(Window parent, String title) {
		super(parent);
		setModal(true);
		setTitle(title);
		setBounds(100, 100, 610, 338);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(8, 0));
		
		ButtonGroup buttonGroup = new ButtonGroup();
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.WEST);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel_1 = new JPanel();
				panel.add(panel_1, BorderLayout.NORTH);
				panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));
				{
					JPanel panel_1_1 = new JPanel();
					panel_1.add(panel_1_1);
					panel_1_1.setAlignmentX(Component.LEFT_ALIGNMENT);
					panel_1_1.setLayout(new BorderLayout(0, 0));
					{
						rdbtnManualEntry = new JRadioButton("Manual entry:");
						rdbtnManualEntry.addItemListener(new ItemListener() {
							@Override
							public void itemStateChanged(ItemEvent arg0) {
								enableComponents(arg0.getStateChange() == ItemEvent.SELECTED);
								previewPanel.repaint();
							}
						});
						buttonGroup.add(rdbtnManualEntry);
						panel_1_1.add(rdbtnManualEntry, BorderLayout.WEST);
					}
					{
						tfManualEntry = new JTextField();
						tfManualEntry.getDocument().addDocumentListener(new DocumentListener() {
							private void action() {
								previewPanel.repaint();
								if (action != null) {
									action.styleChanged(getManualStyleName(tfManualEntry.getText()));
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
						});
						tfManualEntry.setEnabled(false);
						tfManualEntry.setColumns(13);
						tfManualEntry.setMaximumSize(tfManualEntry.getPreferredSize());
						panel_1_1.add(tfManualEntry, BorderLayout.EAST);
					}
				}
				{
					rdbtnSelectExisting = new JRadioButton("Select existing:");
					rdbtnSelectExisting.setSelected(true);
					rdbtnSelectExisting.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent arg0) {
							enableComponents(arg0.getStateChange() != ItemEvent.SELECTED);
							previewPanel.repaint();
						}
					});
					panel_1.add(rdbtnSelectExisting);
					buttonGroup.add(rdbtnSelectExisting);
				}
				{
					tfSearch = new JTextField();
					tfSearch.getDocument().addDocumentListener(new DocumentListener() {

						@Override
						public void changedUpdate(DocumentEvent arg0) {
							listModel.doFilter();
						}

						@Override
						public void insertUpdate(DocumentEvent arg0) {
							listModel.doFilter();
						}

						@Override
						public void removeUpdate(DocumentEvent arg0) {
							listModel.doFilter();
						}
					});
					panel_1.add(tfSearch);
					tfSearch.setAlignmentX(Component.LEFT_ALIGNMENT);
					tfSearch.setColumns(25);
					tfSearch.setMaximumSize(tfSearch.getPreferredSize());
				}
			}
			{
				JScrollPane scrollPane = new JScrollPane();
				panel.add(scrollPane, BorderLayout.CENTER);
				
				stylesList = new JList<StyleSheetInstance>();
				stylesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				stylesList.setAlignmentX(Component.LEFT_ALIGNMENT);
				stylesList.addListSelectionListener(new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent arg0) {
						previewPanel.repaint();
						if (action != null) {
							action.styleChanged(getSelectedStyleName());
						}
					}
				});
				stylesList.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent evt) {
						if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 2) {
							acceptAction();
						}
					}
				});
				
				scrollPane.setViewportView(stylesList);
			}
		}
		{
			previewPanel = new JPanel() {
				@Override
				public void paintComponent(Graphics g) {
					super.paintComponent(g);
					
					Graphics2D g2d = (Graphics2D) g.create();
					
					String previewText = PREVIEW_TEXT;
					Color previewColor = DEFAULT_COLOR;
					
					StyleSheetInstance instance = getSelectedStyleSheetInstance();
					if (instance == null) {
						previewText = NO_PREVIEW_TEXT;
						previewColor = NO_PREVIEW_COLOR;
					}
					
					StyleSheetInstance.paintText(
							/* graphics */	g2d, 
							/* style */		instance, 
							/* text */		previewText, 
							/* fontColor */	previewColor, 
							/* realBounds*/	new Rectangle(0, 0, previewPanel.getWidth(), previewPanel.getHeight()), 
							/* margins*/	new float[] {15, 15, 15, 15});
				}
			};
			previewPanel.setBorder(new TitledBorder(null, "Preview", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			contentPanel.add(previewPanel, BorderLayout.CENTER);
			previewPanel.setLayout(new BorderLayout(0, 0));
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						acceptAction();
					}
					
				});
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						wasCancelled = true;
						dispose();
					}
					
				});
				buttonPane.add(cancelButton);
			}
		}
		
		fillStylesList();
	}
	
	private void acceptAction() {
		wasCancelled = false;
		dispose();
	}
	
	public void setStyleValueAction(StyleValueAction valueAction) {
		this.action = valueAction;
	}
	
	private void enableComponents(boolean enableManual) {
		tfManualEntry.setEnabled(enableManual);
		tfSearch.setEnabled(!enableManual);
		stylesList.setEnabled(!enableManual);
	}

	private void fillStylesList() {
		List<StyleSheetInstance> instances = StyleSheet.getActiveStyleSheet().getInstances();
		
		stylesList.setListData((StyleSheetInstance[]) instances.toArray(new StyleSheetInstance[instances.size()]));
		
		listModel = new FilteredListModel<StyleSheetInstance>(stylesList.getModel());
		listModel.setFilter(new FilteredListModel.Filter() {
			@Override
			public boolean accept(Object element) {
				String searchText = tfSearch.getText();
				if (searchText == null || searchText.isEmpty()) {
					return true;
				}
				
				String name = ((StyleSheetInstance) element).getName().toLowerCase();
				
				List<SearchSpec> specs = SearchSpec.generateSearchSpecs(MainApp.getSearchStrings(searchText));
				for (SearchSpec spec : specs) {
					if (!name.contains(spec.getLowercaseString())) {
						return false;
					}
				}
				return true;
			}
		});
		stylesList.setModel(listModel);
		
	}
	
	public StyleSheetInstance getSelectedStyleSheetInstance() {
		if (rdbtnManualEntry.isSelected()) {
			String text = tfManualEntry.getText();
			StyleSheetInstance instance = StyleSheet.getActiveStyleSheet().getStyleInstance(text);
			
			if (instance == null) {
				// users might use a hash without 0x and #, consider this case
				try {
					instance = StyleSheet.getActiveStyleSheet().getStyleInstance("0x" + text);
				} catch (Exception e) {};
			}
			
			return instance;
		}
		else {
			if (stylesList.isSelectionEmpty()) {
				return null;
			} else {
				return stylesList.getSelectedValue();
			}
		}
	}
	
	private String getManualStyleName(String value) {
		try {
			StyleSheetInstance style = StyleSheet.getActiveStyleSheet().getStyleInstance(value);
			if (style == null) {
				// users might use a hash without 0x and #, consider this case
				style = StyleSheet.getActiveStyleSheet().getStyleInstance("0x" + value);
				if (style != null) {
					value = "0x" + value;
				}
			}
		} catch (Exception e) {};
		
		return value;
	}
	
	public String getSelectedStyleName() {
		if (rdbtnManualEntry.isSelected()) {
			return getManualStyleName(tfManualEntry.getText());
		}
		else {
			if (stylesList.isSelectionEmpty()) {
				return null;
			} else {
				return stylesList.getSelectedValue().getName();
			}
		}
	}
	
	public void setSelectedStyle(String styleName) {
		StyleSheetInstance instance = StyleSheet.getActiveStyleSheet().getStyleInstance(styleName);

		
		if (instance != null) {
			stylesList.setSelectedValue(instance, true);
			rdbtnManualEntry.setSelected(false);
		}
		else {
			tfManualEntry.setText(styleName);
			rdbtnManualEntry.setSelected(true);
		}
	}
	
	public boolean wasCancelled() {
		return wasCancelled;
	}
	
}
