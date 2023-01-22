package sporemodder.extras.spuieditor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import sporemodder.utilities.FilteredTree;
import sporemodder.utilities.FilteredTreeModel;

public class StandardChooser extends JDialog implements TreeSelectionListener, ActionListener, ItemListener, DocumentListener, WindowListener {
	
	public interface ChooserDelegate {
		public boolean isValid();
		public JPanel getPreviewPanel();
		public void acceptAction(TreePath selectedObject, int chosenType);
		public void searchAction(String text, DocumentEvent event);
	}
	
	public static final int CHOOSER_CREATE_NEW = 0;
	public static final int CHOOSER_NONE = 1;
	public static final int CHOOSER_EXISTING = 2;
	
	private JSplitPane splitPane;
	
	private JTextField tfSearch;
	private JRadioButton rdbtnNone;
	private JRadioButton rdbtnSelectExisting;
	private JRadioButton rdbtnCreateNew;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	
	private final FilteredTree treeView;
	
	private JButton okButton;
	
	protected ChooserDelegate delegate;
	
	private boolean wasCancelled;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			StandardChooser dialog = new StandardChooser(null, "Choose a new drawable", null, new ChooserDelegate() {
				@Override
				public JPanel getPreviewPanel() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public void acceptAction(TreePath selectedObject, int chosenType) {
					System.out.println("You selected " + selectedObject);
				}

				@Override
				public boolean isValid() {
					return true;
				}

				@Override
				public void searchAction(String text, DocumentEvent arg0) {
					// TODO Auto-generated method stub
					
				}
			});
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected StandardChooser(Window parent, String title, FilteredTree treeView) {
		this(parent, title, treeView, null);
	}

	/**
	 * Create the dialog.
	 */
	public StandardChooser(Window parent, String title, FilteredTree treeView, ChooserDelegate delegate) {
		super(parent);
		
		if (treeView == null) {
			treeView = new FilteredTree(new FilteredTreeModel(new DefaultMutableTreeNode()));
		}
		
		this.treeView = treeView;
		this.treeView.addTreeSelectionListener(this);
		
		this.delegate = delegate;
		
		setResizable(true);
		setModal(true);
		setTitle(title);
		setSize(800, 600);
		addWindowListener(this);
		
		getContentPane().setLayout(new BorderLayout());
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("OK");
				okButton.setEnabled(false);
				okButton.setActionCommand("OK");
				okButton.addActionListener(this);
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(this);
				buttonPane.add(cancelButton);
			}
		}
		{
			splitPane = new JSplitPane();
			getContentPane().add(splitPane, BorderLayout.CENTER);
			{
				JPanel panel = new JPanel();
				splitPane.setLeftComponent(panel);
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				{
					rdbtnCreateNew = new JRadioButton("Create new");
					rdbtnCreateNew.addItemListener(this);
					rdbtnCreateNew.setAlignmentX(LEFT_ALIGNMENT);
					buttonGroup.add(rdbtnCreateNew);
					panel.add(rdbtnCreateNew);
				}
				{
					rdbtnNone = new JRadioButton("None");
					rdbtnNone.addItemListener(this);
					rdbtnNone.setAlignmentX(LEFT_ALIGNMENT);
					buttonGroup.add(rdbtnNone);
					panel.add(rdbtnNone);
				}
				{
					rdbtnSelectExisting = new JRadioButton("Select existing:");
					rdbtnSelectExisting.setSelected(true);
					rdbtnSelectExisting.setAlignmentX(LEFT_ALIGNMENT);
					rdbtnSelectExisting.addItemListener(this);
					buttonGroup.add(rdbtnSelectExisting);
					panel.add(rdbtnSelectExisting);
				}
				{
					JPanel explorerPanel = new JPanel();
					explorerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
					panel.add(explorerPanel);
					explorerPanel.setLayout(new BorderLayout(0, 0));
					{
						tfSearch = new JTextField();
						tfSearch.getDocument().addDocumentListener(this);
						explorerPanel.add(tfSearch, BorderLayout.NORTH);
						tfSearch.setColumns(10);
					}
					{
						explorerPanel.add(new JScrollPane(this.treeView), BorderLayout.CENTER);
					}
				}
			}
			{
				JPanel panel = new JPanel();
				panel.setBorder(new TitledBorder(null, "Preview", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				splitPane.setRightComponent(panel);
			}
		}
		
		setLocationRelativeTo(null);
	}
	
	public void setHasCreateNew(boolean value) {
		rdbtnCreateNew.setVisible(value);
		if (!value && rdbtnCreateNew.isSelected()) {
			if (rdbtnNone.isVisible()) {
				rdbtnNone.setSelected(true);
			}
			else {
				rdbtnSelectExisting.setSelected(true);
			}
		}
	}
	
	public void setHasNone(boolean value) {
		rdbtnNone.setVisible(value);
		if (!value && rdbtnNone.isSelected()) {
			if (rdbtnCreateNew.isVisible()) {
				rdbtnCreateNew.setSelected(true);
			}
			else {
				rdbtnSelectExisting.setSelected(true);
			}
		}
	}
	
	public void setHasExisting(boolean value) {
		rdbtnSelectExisting.setVisible(value);
		treeView.setVisible(value);
		tfSearch.setVisible(value);
		if (!value && rdbtnSelectExisting.isSelected()) {
			if (rdbtnCreateNew.isVisible()) {
				rdbtnCreateNew.setSelected(true);
			}
			else {
				rdbtnNone.setSelected(true);
			}
		}
	}
	
	public void setOKButtonEnabled(boolean isEnabled) {
		okButton.setEnabled(isEnabled);
	}
	
	public void setDelegate(ChooserDelegate delegate) {
		this.delegate = delegate;
	}

	public FilteredTree getTreeView() {
		return treeView;
	}
	
	public int getChosenType() {
		if (rdbtnCreateNew.isSelected()) return CHOOSER_CREATE_NEW;
		else if (rdbtnNone.isSelected()) return CHOOSER_NONE;
		else if (rdbtnSelectExisting.isSelected()) return CHOOSER_EXISTING;
		return -1;
	}
	
	/**
	 * Returns the selected <code>TreePath</code> if the type is "Select existing", null otherwise.
	 * @return
	 */
	public TreePath getSelectedObject() {
		int chosenType = getChosenType();
		switch(chosenType) {
		case CHOOSER_EXISTING:
			return treeView.getSelectionPath();
		case CHOOSER_CREATE_NEW:
		case CHOOSER_NONE:
		default:
			return null;
		}
	}
	
	public boolean wasCancelled() {
		return wasCancelled;
	}
	
	public void setChosenType(int chosenType) {
		switch(chosenType) {
		case CHOOSER_CREATE_NEW:
			rdbtnCreateNew.setSelected(true);
			break;
		case CHOOSER_NONE:
			rdbtnNone.setSelected(true);
			break;
		case CHOOSER_EXISTING:
			rdbtnSelectExisting.setSelected(true);
			break;
		}
	}
	
	public void setSelectedObject(TreePath path) {
		if (getChosenType() == CHOOSER_EXISTING) {
			treeView.setSelectionPath(path);
			treeView.scrollPathToVisible(path);
		}
	}
	
	protected void acceptAction() {
		delegate.acceptAction(getSelectedObject(), getChosenType());
		wasCancelled = false;
		dispose();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		if (arg0.getActionCommand().equals("OK")) {
			acceptAction();
		}
		else if (arg0.getActionCommand().equals("Cancel")) {
			wasCancelled = true;
			dispose();
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent arg0) {
		splitPane.setRightComponent(delegate.getPreviewPanel());
		
		okButton.setEnabled(delegate.isValid());
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		// for the radio buttons
		splitPane.setRightComponent(delegate.getPreviewPanel());
		
		if (rdbtnSelectExisting.isSelected()) {
			tfSearch.setEnabled(true);
			treeView.setEnabled(true);
		}
		else {
			tfSearch.setEnabled(false);
			treeView.setEnabled(false);
		}
		
		okButton.setEnabled(delegate.isValid());
	}
	
	@Override
	public void changedUpdate(DocumentEvent event) {
		delegate.searchAction(tfSearch.getText(), event);
	}

	@Override
	public void insertUpdate(DocumentEvent event) {
		delegate.searchAction(tfSearch.getText(), event);
	}

	@Override
	public void removeUpdate(DocumentEvent event) {
		delegate.searchAction(tfSearch.getText(), event);
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		wasCancelled = true;
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
}
