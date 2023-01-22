package sporemodder.extras.spuieditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import sporemodder.MainApp;
import sporemodder.extras.spuieditor.components.ComponentFactory;
import sporemodder.extras.spuieditor.components.Image;
import sporemodder.extras.spuieditor.components.SPUIComponent;
import sporemodder.extras.spuieditor.components.SPUIDrawable;
import sporemodder.extras.spuieditor.components.SPUIWinProc;
import sporemodder.extras.spuieditor.components.WinComponent;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIFileResource;
import sporemodder.files.formats.spui.SPUIMain;
import sporemodder.userinterface.dialogs.AdvancedFileChooser;
import sporemodder.userinterface.dialogs.AdvancedFileChooser.ChooserType;
import sporemodder.util.ProjectItem;
import sporemodder.utilities.FilteredTree;
import sporemodder.utilities.FilteredTree.FilteredTreeDragAndDrop;
import sporemodder.utilities.FilteredTreeModel;
import sporemodder.utilities.FilteredTreeModel.TreeFilter;
import sporemodder.utilities.SearchSpec;

public class SPUIEditor extends JFrame implements TreeSelectionListener, DocumentListener, WindowListener, UndoableEditor {

	private static final FileNameExtensionFilter FILEFILTER_SPUI = new FileNameExtensionFilter("Spore User Interface (*.spui)", "spui");
	private static final FileNameExtensionFilter FILEFILTER_SPUI_T = new FileNameExtensionFilter("Text Spore User Interface (*.spui_t)", "spui_t");
	
	private String relativePath;
	private File originalFile;
	private boolean isTextSPUI;
	
	private JPanel contentPane;
	private SPUIMain spui;
	private SPUIViewer viewer;
	
	private final List<Image> imageComponents = new ArrayList<Image>();
	private final List<SPUIDrawable> drawableComponents = new ArrayList<SPUIDrawable>();
	
	private JScrollPane hierarchyPanel;
	private FilteredTree hierarchyTree;
	private FilteredTreeModel hierarchyTreeModel;
	private DefaultMutableTreeNode rootNode;
	
	private DefaultMutableTreeNode imagesNode;
	private DefaultMutableTreeNode drawablesNode;
	
	private JScrollPane propertiesPanel;
	private JPanel panel;
	private JCheckBox cbShowInvisibleComponents;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmShowPreview;
	private JPanel panel_1;
	private JTextField tfSearchBar = new JTextField();
	
	private final List<SearchSpec> searchSpecs = new ArrayList<SearchSpec>();
	private JMenuItem mntmSave;
	private JMenuItem mntmSaveAs;
	private JSeparator separator;
	
	private final List<CommandAction> actions = new ArrayList<CommandAction>();
	private int currentAction;
	
	private JMenu mnEdit;
	private JMenuItem mntmUndo;
	private JMenuItem mntmRedo;
	
	private JMenuItem mntmExport;
	private JMenuItem mntmImport;
	
	public static final Set<String> MissingImages = new HashSet<String>();
	
	/**
	 * This stores the index of the active undoable action when this SPUI was last saved. This can be used to avoid showing the SPUI as unsaved when no actions have been done since the last time it was saved.
	 */
	private int savedActionIndex = -1;
	//private boolean isSaved = true;
	private final ReadOnlyBooleanWrapper isSaved = new ReadOnlyBooleanWrapper(this, "isSaved", true);
	private JMenu mnComponent;
	private JMenu mnAddWindowComponent;
	private JMenu mnAddModifier;
	private JCheckBoxMenuItem chckbxMarkWindowsUsing;
	private JCheckBoxMenuItem chckbxUseLocaleFiles;
//	private JMenuItem mntmCopy;
//	private JMenuItem mntmPaste;
//	private JMenuItem mntmCut;
	private JMenuItem mntmDuplicateComponent;
	private JMenuItem mntmMoveAbove;
	private JMenuItem mntmMoveBelow;
	private JMenuItem mntmDeleteComponent;
	
	private boolean hadErrors;
	private Action saveAction;
	
	private ProjectItem smfxProjectItem = null;
	
	protected static void loadStyleSheet() throws IOException {
		InputStream is = ResourceLoader.getResourceInputStream("sporeuitextstyles.css", 0x0248E873);
		
		if (is != null) {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
				StyleSheet.setActiveStyleSheet(StyleSheet.readStyleSheet(reader));
			}
			finally {
				if (is != null) {
					is.close();
				}
			}
		}
	}

	public SPUIEditor(SPUIMain spui, String title, String path, File file, boolean isTextSPUI, boolean canBeSaved, final Action saveAction, ProjectItem smfxItem) throws InvalidBlockException, IOException {
		this(spui, title, path, file, isTextSPUI, canBeSaved, saveAction);
		smfxProjectItem = smfxItem;
	}
	/**
	 * Create the frame.
	 * @throws InvalidBlockException 
	 * @throws IOException 
	 */
	public SPUIEditor(SPUIMain spui, String title, String path, File file, boolean isTextSPUI, boolean canBeSaved, final Action saveAction) throws InvalidBlockException, IOException {
		setBounds(100, 100, 881, 436);
		setExtendedState( JFrame.MAXIMIZED_BOTH );
		setTitle(title);
		// this is handled by windowClosing event
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		
		loadStyleSheet();
		
		this.spui = spui;
		this.relativePath = path;
		this.originalFile = file;
		this.isTextSPUI = isTextSPUI;
		this.saveAction = saveAction;
		
		MissingImages.clear();
		
		try {
			viewer = new SPUIViewer(spui, this);
		} 
		catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "There was an error while trying to parse SPUI:\n" + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			dispose();
			hadErrors = true;
			return;
		}
		
		if (!MissingImages.isEmpty()) {
			int size = MissingImages.size();
			
			StringBuilder sb = new StringBuilder();
			sb.append("The following images were not found:\n");
			int i = 0;
			for (String str : MissingImages) {
				
				if (i++ < 5) {
					sb.append(str);
					sb.append("\n");
				}
			}
			if (size > 5) {
				sb.append("... and " + (size - 5) + " more.");
			}
			
			JOptionPane.showMessageDialog(this, sb.toString(), "Images not found", JOptionPane.WARNING_MESSAGE);
		}
		
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		mntmShowPreview = new JMenuItem("Show Preview");
		mntmShowPreview.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));
		mntmShowPreview.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					SPUIPreview preview = new SPUIPreview(viewer);
					preview.setDefaultCloseOperation(SPUIPreview.DISPOSE_ON_CLOSE);
					preview.setVisible(true);
				} catch (InvalidBlockException | IOException e) {
					JOptionPane.showMessageDialog(SPUIEditor.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					dispose();
					return;
				}
			}
		});
		
		mntmSave = new JMenuItem("Save");
		mntmSave.setEnabled(canBeSaved);
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		mntmSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				save();
			}
		});
		mnFile.add(mntmSave);
		
		mntmSaveAs = new JMenuItem("Save as...");
		mntmSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		mntmSaveAs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveAs();
			}
		});
		mnFile.add(mntmSaveAs);
		
		separator = new JSeparator();
		mnFile.add(separator);
		mnFile.add(mntmShowPreview);
		
		mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);
		
		mntmUndo = new JMenuItem("Undo");
		mntmUndo.setEnabled(false);
		mntmUndo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				undo();
			}
		});
		mntmUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		mnEdit.add(mntmUndo);
		
		mntmRedo = new JMenuItem("Redo");
		mntmRedo.setEnabled(false);
		mntmRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
		mntmRedo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				redo();
			}
		});
		mnEdit.add(mntmRedo);
		
//		separator_2 = new JSeparator();
//		mnEdit.add(separator_2);
//		
//		//TODO Cut
//		mntmCut = new JMenuItem("Cut");
//		mntmCut.setEnabled(false);
//		mntmCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
//		mnEdit.add(mntmCut);
//		
//		//TODO Copy
//		mntmCopy = new JMenuItem("Copy");
//		mntmCopy.setEnabled(false);
//		mntmCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
//		mnEdit.add(mntmCopy);
//		
//		//TODO Paste
//		mntmPaste = new JMenuItem("Paste");
//		mntmPaste.setEnabled(false);
//		mntmPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
//		mnEdit.add(mntmPaste);
		
		mnComponent = new JMenu("Component");
		menuBar.add(mnComponent);
		
		mnAddWindowComponent = new JMenu("Add window component");
		mnAddWindowComponent.setEnabled(false);
		mnComponent.add(mnAddWindowComponent);
		
		ComponentFactory.fillInsertMenu(mnAddWindowComponent, this, WinComponent.INTERFACE_NAME);
		
		//TODO Add modifier
		mnAddModifier = new JMenu("Add window procedure");
		mnAddModifier.setEnabled(false);
		mnComponent.add(mnAddModifier);
		
		ComponentFactory.fillInsertMenu(mnAddModifier, this, SPUIWinProc.INTERFACE_NAME);
		
		mntmDuplicateComponent = new JMenuItem("Duplicate component");
		mntmDuplicateComponent.setEnabled(false);
		mntmDuplicateComponent.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode node = ((DefaultMutableTreeNode) hierarchyTree.getSelectionPath().getLastPathComponent());
				
				SPUIComponent comp = ((SPUIComponent) node.getUserObject());
				SPUIComponent copyComp = comp.copyComponent(false);
				copyComp.setSPUIViewer(viewer);
				
				InsertComponentAction action = new InsertComponentAction(
						(ComponentContainer) ((DefaultMutableTreeNode) node.getParent()).getUserObject(), 
						copyComp, 
						comp,
						SPUIEditor.this, 
						node.getParent().getIndex(node));
				
				action.redo();
				
				addCommandAction(action);
			}
		});
		mntmDuplicateComponent.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK));
		mnComponent.add(mntmDuplicateComponent);
		
		mntmDeleteComponent = new JMenuItem("Delete component");
		mntmDeleteComponent.setEnabled(false);
		mntmDeleteComponent.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (viewer.getActiveComponent() != null) {
					removeComponent(viewer.getActiveComponent());
				}
			}
		});
		mntmDeleteComponent.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		mnComponent.add(mntmDeleteComponent);
		
		mntmMoveAbove = new JMenuItem("Move above");
		mntmMoveAbove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (hierarchyTree.getSelectionCount() > 0) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) hierarchyTree.getSelectionPath().getLastPathComponent();
					DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
					
					MoveComponentAction action = new MoveComponentAction(
							SPUIEditor.this, node, parent, parent, 
							parent.getIndex(node) - 1);
					
					action.redo();
					addCommandAction(action);
				}
			}
		});
		mntmMoveAbove.setEnabled(false);
		mnComponent.add(mntmMoveAbove);
		
		mntmMoveBelow = new JMenuItem("Move below");
		mntmMoveBelow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (hierarchyTree.getSelectionCount() > 0) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) hierarchyTree.getSelectionPath().getLastPathComponent();
					DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
					
					MoveComponentAction action = new MoveComponentAction(
							SPUIEditor.this, node, parent, parent, 
							parent.getIndex(node) + 2);
					
					action.redo();
					addCommandAction(action);
				}
			}
		});
		mntmMoveBelow.setEnabled(false);
		mnComponent.add(mntmMoveBelow);
		
		mnComponent.add(new JSeparator());
		
		
		mntmExport = new JMenuItem("Export component...");
		mntmExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SPUIBuilder builder = new SPUIBuilder();
				builder.addComponent(viewer.getActiveComponent().copyComponent(true));
				saveAs(builder);
			}
		});
		mnComponent.add(mntmExport);
		
		
		mntmImport = new JMenuItem("Import as component...");
		mntmImport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					importAsComponent();
				} catch (InvalidBlockException | IOException | ArgScriptException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(SPUIEditor.this, "Error reading file: " + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		mnComponent.add(mntmImport);
		
		
		mnComponent.add(new JSeparator());
		
		chckbxMarkWindowsUsing = new JCheckBoxMenuItem("Mark windows using component");
		chckbxMarkWindowsUsing.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				viewer.setMarkUsingComponents(arg0.getStateChange() == ItemEvent.SELECTED);
			}
		});
		mnComponent.add(chckbxMarkWindowsUsing);
		
		chckbxUseLocaleFiles = new JCheckBoxMenuItem("Use locale files");
		chckbxUseLocaleFiles.setSelected(viewer.isUseLocaleFiles());
		chckbxUseLocaleFiles.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				viewer.setUseLocaleFiles(arg0.getStateChange() == ItemEvent.SELECTED);
			}
		});
		mnComponent.add(chckbxUseLocaleFiles);
		
		JCheckBoxMenuItem chckbxRenderAnimated = new JCheckBoxMenuItem("Render animated icons");
		chckbxRenderAnimated.setSelected(SPUIViewer.RENDER_ANIMATED_ICONS);
		chckbxRenderAnimated.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				SPUIViewer.RENDER_ANIMATED_ICONS = arg0.getStateChange() == ItemEvent.SELECTED;
				
				//MainApp.writeSettings();
			}
		});
		mnComponent.add(chckbxRenderAnimated);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JSplitPane splitPaneMain = new JSplitPane();
		splitPaneMain.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		splitPaneMain.setDividerLocation(400);
		contentPane.add(splitPaneMain, BorderLayout.CENTER);
		
		splitPaneMain.setRightComponent(viewer);
		
		Rectangle bounds = viewer.getTotalBounds();
		if (bounds != null) {
			contentPane.setPreferredSize(new Dimension(bounds.x + bounds.height + 5, bounds.y + bounds.height + 5));
		}
		
		// this will get replaced, it's jsut a placeholder
		rootNode = new DefaultMutableTreeNode("Layout");
		
		hierarchyTreeModel = new FilteredTreeModel(rootNode);
		hierarchyTreeModel.setFilter(new TreeFilter() {
			private boolean checkSearch(DefaultMutableTreeNode node) {
				String str = node.toString().toLowerCase();
				for (SearchSpec spec : searchSpecs) {
					if (!str.contains(spec.getLowercaseString())) {
						// check children nodes
						
						@SuppressWarnings("unchecked")
						Enumeration<DefaultMutableTreeNode> children = node.children();

						while (children.hasMoreElements()) {
							if (checkSearch(children.nextElement())) {
								return true;
							}
						}
						
						return false;
					}
				}
				
				return true;
			}
			
			@Override
			public boolean accept(DefaultMutableTreeNode node) {
				if (searchSpecs.size() > 0) {
					if (!checkSearch(node)) {
						return false;
					}
				}
				if (cbShowInvisibleComponents.isSelected()) {
					return true;
				}
				Object obj = node.getUserObject();
				
				if (obj != null && obj instanceof WinComponent) {
					return (((WinComponent) obj).getFlags() & WinComponent.FLAG_VISIBLE) == WinComponent.FLAG_VISIBLE;
				}
				
				return true;
			}
		});
		hierarchyTreeModel.setFilterEnabled(true);
		
		panel = new JPanel();
		//contentPane.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		splitPaneMain.setLeftComponent(panel);
		
		panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.NORTH);
		panel_1.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		tfSearchBar.getDocument().addDocumentListener(this);
		/* ////////////
		panel_1.add(tfSearchBar);
		*/
		tfSearchBar.setColumns(20);
		
		Action invisibleComponentAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				viewer.setShowInvisibleComponents(cbShowInvisibleComponents.isSelected());
				hierarchyTree.updateUI();
				hierarchyTree.repaint();
			}
		};
		
		cbShowInvisibleComponents = new JCheckBox(invisibleComponentAction);
		cbShowInvisibleComponents.setText("Show all");
		cbShowInvisibleComponents.setToolTipText("Ctrl + H");
		cbShowInvisibleComponents.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK), "ctrl_h");
		cbShowInvisibleComponents.getActionMap().put("ctrl_h", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cbShowInvisibleComponents.doClick();
			}
		});
		////////////panel_1.add(cbShowInvisibleComponents);
//		panel_1.add(new JLabel("test"));
		
		// if we don't do this, the check box is too big and it doesn't show
//		int divider = cbShowInvisibleComponents.getWidth() + tfSearchBar.getWidth();
//		if (divider < 400) {
//			divider = 400;
//		}
		menuBar.add(new JSeparator(SwingConstants.VERTICAL));
		menuBar.add(cbShowInvisibleComponents);
		
		
		inspectorSplitPane = new JSplitPane();
		//panel.add(inspectorSplitPane);
		inspectorSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		inspectorSplitPane.setDividerLocation(400);
		
		hierarchyPanel = new JScrollPane();
		inspectorSplitPane.setTopComponent(hierarchyPanel);
		
		hierarchyTree = new FilteredTree(hierarchyTreeModel, new FilteredTreeDragAndDrop() {
			final DataFlavor flavor = new DataFlavor(SPUIComponent.class, "SPUIComponent");
			
			@Override
			public DataFlavor getDataFlavor() {
				return flavor;
			}

			@Override
			public boolean canBeDragged(DefaultMutableTreeNode node) {
				ComponentContainer userObject = (ComponentContainer) node.getUserObject();
				return userObject.nodeIsMovable();
			}

			
			@Override
			public void move(DefaultMutableTreeNode selectedNode, DefaultMutableTreeNode newParent, int childIndex) {
				
				MoveComponentAction action = new MoveComponentAction(
						SPUIEditor.this, selectedNode, (DefaultMutableTreeNode) selectedNode.getParent(), newParent, childIndex);
				
				action.redo();
				
				SPUIEditor.this.addCommandAction(action);
			}

			@Override
			public boolean canBeDropped(DefaultMutableTreeNode node, DefaultMutableTreeNode newParent) {
				ComponentContainer parentObject = (ComponentContainer) newParent.getUserObject();
				
				if (newParent == node || newParent.isNodeAncestor(node)) {
					return false;
				}
				
				return parentObject.nodeAcceptsComponent((SPUIComponent) node.getUserObject());
			}
			
		});
		hierarchyTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		hierarchyTree.addTreeSelectionListener(this);
		
		hierarchyPanel.setViewportView(hierarchyTree);
		
		
		propertiesPanel = new JScrollPane();
		propertiesPanel.getVerticalScrollBar().setUnitIncrement(16);
		inspectorSplitPane.setBottomComponent(propertiesPanel);
		//pack();
		
		fillImageAndDrawableComponents();
		fillHierarchyTree();
	}
	
	private void updateIsSaved() {
		if (currentAction != savedActionIndex) {
			for (int i = currentAction; i >= 0; i--) {
				if (actions.get(i).isSignificant()) {
					isSaved.set(false);
					//setTitle(MainApp.getCurrentProject().getProjectName() + " - *" + relativePath);
					return;
				}
			}
		}
		isSaved.set(true);
		//setTitle(MainApp.getCurrentProject().getProjectName() + " - " + relativePath);
	}
	
	@Override
	public void undo() {
		actions.get(currentAction).undo();
		currentAction--;
		updateIsSaved();
		updateUndoRedoButtons();
	}
	
	@Override
	public void redo() {
		// we have to execute the next action
		actions.get(++currentAction).redo();
		updateIsSaved();
		updateUndoRedoButtons();
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
		updateIsSaved();
		updateUndoRedoButtons();
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
	
	public JScrollPane getPropertiesPanel() {
		return propertiesPanel;
	}
	
	public SPUIViewer getSPUIViewer() {
		return viewer;
	}
	
	public DefaultMutableTreeNode getRootNode() {
		return rootNode;
	}
	
	public void setSelectedComponent(SPUIComponent comp) {
		updateMenus();
		
		if (comp == null) {
			hierarchyTree.setSelectionPath(null);
		}
		
		TreePath selectionPath = hierarchyTree.getSelectionPath();
		if (selectionPath != null && 
				((DefaultMutableTreeNode) selectionPath.getLastPathComponent()).getUserObject() == comp) {
			return;
		}
		
		TreePath path = comp.getHierarchyTreePath();
		if (path != null) {
			hierarchyTree.setSelectionPath(path);
			hierarchyTree.scrollPathToVisible(path);
		}
	}
	
	
	public void updateMenus() {
		SPUIComponent component = viewer.getActiveComponent();
		if (component == null) {
			System.out.println("TEST");
			// check if it's the root node, we can add windows there
			if (hierarchyTree.getSelectionCount() > 0
					&& hierarchyTree.getSelectionPath().getLastPathComponent() == rootNode) {
				
				mnAddWindowComponent.setEnabled(true);
				mnAddModifier.setEnabled(true);
				mntmDuplicateComponent.setEnabled(false);
				mntmDeleteComponent.setEnabled(false);
				mntmMoveAbove.setEnabled(false);
				mntmMoveBelow.setEnabled(false);
				
				mntmExport.setEnabled(true);
				mntmImport.setEnabled(true);
				
//				mntmCut.setEnabled(false);
//				mntmCopy.setEnabled(false);
//				//TODO check if the clipboard value is correct?
//				mntmPaste.setEnabled(true);
			}
			else {
				mnAddWindowComponent.setEnabled(false);
				mnAddModifier.setEnabled(false);
				mntmDuplicateComponent.setEnabled(false);
				mntmDeleteComponent.setEnabled(false);
				mntmMoveAbove.setEnabled(false);
				mntmMoveBelow.setEnabled(false);
				mntmExport.setEnabled(false);
				mntmImport.setEnabled(false);
				
//				mntmCut.setEnabled(false);
//				mntmCopy.setEnabled(false);
//				mntmPaste.setEnabled(false);
			}
		}
		else {
			
			updateMoveMenus(component);
			
			if (component instanceof WinComponent) {
				mnAddWindowComponent.setEnabled(true);
				mnAddModifier.setEnabled(true);
				mntmExport.setEnabled(true);
				mntmImport.setEnabled(true);
			} else {
				mnAddWindowComponent.setEnabled(false);
				mnAddModifier.setEnabled(false);
				mntmExport.setEnabled(false);
				mntmImport.setEnabled(false);
			}
			
			// can all components be duplicated?
			mntmDuplicateComponent.setEnabled(true);
			mntmDeleteComponent.setEnabled(true);
			
//			mntmCut.setEnabled(true);
//			mntmCopy.setEnabled(true);
//			//TODO check if the clipboard value is correct?
//			mntmPaste.setEnabled(true);
		}
	}
	
	private void updateMoveMenus(SPUIComponent activeComponent) {
		if (activeComponent == null) {
			mntmMoveAbove.setEnabled(false);
			mntmMoveBelow.setEnabled(false);
		}
		else {
			mntmMoveAbove.setEnabled(activeComponent.nodeCanBeMovedAbove());
			mntmMoveBelow.setEnabled(activeComponent.nodeCanBeMovedBelow());
		}
	}
	
	public void updateHierarchyTree() {
		hierarchyTree.updateUI();
		hierarchyTree.repaint();
	}
	
	public JTree getHierarchyTree() {
		return hierarchyTree;
	}
	

	private void fillImageAndDrawableComponents() throws InvalidBlockException, IOException {
		for (SPUIFileResource res : spui.getResources().getFileResources()) {
			SPUIComponent comp = ResourceLoader.getComponent(res);
			// we must set the viewer here
			comp.setSPUIViewer(viewer);
			imageComponents.add((Image) comp);
		}
		for (SPUIBlock block : spui.getBlocks()) {
			int type = block.getResource().getHash();
			
			if (type == Image.TYPE) {
				SPUIComponent comp = ResourceLoader.getComponent(block);
				// we must set the viewer here
				comp.setSPUIViewer(viewer);
				imageComponents.add((Image) comp);
				continue;
			}
			
			for (int t : SPUIDrawable.DrawableTypes) {
				if (type == t) {
					SPUIComponent comp = ResourceLoader.getComponent(block);
					if (comp != null) {
						// we must set the viewer here
						comp.setSPUIViewer(viewer);
						drawableComponents.add((SPUIDrawable) comp);
					}
					break;
				}
			}
		}
	}

	public void fillHierarchyTree() {
		
		// so it uses default calculations
		hierarchyTreeModel.setFilterEnabled(false);
		
		rootNode.removeAllChildren();
		hierarchyTreeModel.reload();
		
		imagesNode = new DefaultMutableTreeNode(new RootComponentContainer("Images", this));
		drawablesNode = new DefaultMutableTreeNode(new RootComponentContainer("Drawables", this));
		
		for (int i = 0; i < imageComponents.size(); i++) {
			imageComponents.get(i).fillHierarchyTree(hierarchyTreeModel, imagesNode, imagesNode.getChildCount());
		}
		
		for (int i = 0; i < drawableComponents.size(); i++) {
			drawableComponents.get(i).fillHierarchyTree(hierarchyTreeModel, drawablesNode, drawablesNode.getChildCount());
		}
		
		int ind = 0;
		
//		for (WinComponent window : viewer.getMainWindows()) {
//			window.fillHierarchyTree(hierarchyTreeModel, rootNode, ind++);
//		}
		
		rootNode = (DefaultMutableTreeNode) viewer.getLayoutWindow().fillHierarchyTree(hierarchyTreeModel, null, 0);
		
		ind = rootNode.getChildCount();
		
		hierarchyTreeModel.insertNodeInto(imagesNode, rootNode, ind++);
		hierarchyTreeModel.insertNodeInto(drawablesNode, rootNode, ind++);
		
		hierarchyTree.expandRow(0);
		
		hierarchyTreeModel.setFilterEnabled(true);
	}

	@Override
	public void valueChanged(TreeSelectionEvent arg0) {
		updateMenus();
		if (hierarchyTree.getSelectionCount() > 0) {
			Object obj = ((DefaultMutableTreeNode) arg0.getPath().getLastPathComponent()).getUserObject();
			if (obj instanceof SPUIComponent) {
				viewer.setActiveComponent((SPUIComponent) obj, hierarchyTree.getSelectionCount() == 1);
			}
		}
		
	}

	private void documentChangeAction(DocumentEvent arg0) {
		if (arg0.getDocument() == tfSearchBar.getDocument()) {
			searchSpecs.clear();
			List<String> strings = MainApp.getSearchStrings(tfSearchBar.getText());
			
			for (String str : strings) {
				searchSpecs.add(new SearchSpec(str.toLowerCase()));
			}
			
			hierarchyTree.updateUI();
			hierarchyTree.repaint();
		}
	}
	@Override
	public void changedUpdate(DocumentEvent arg0) {
		documentChangeAction(arg0);
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		documentChangeAction(arg0);
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		documentChangeAction(arg0);
	}

	
	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
		ResourceLoader.clearResources();
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (!isSavedProperty().get()) {
			int result = JOptionPane.showOptionDialog(this, "Do you want to save changes you made to " + relativePath + "?", "Save", 
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[] {"Save", "Don't save", "Cancel"}, "Save");
			
			if (result == JOptionPane.YES_OPTION) {
				save();
				viewer.dispose();
				dispose();
			}
			else if (result == JOptionPane.NO_OPTION) {
				viewer.dispose();
				dispose();
			}
		} 
		else {
			if (viewer != null) {
				viewer.dispose();
			}
//			Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
			dispose();
		}
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}
	
	private boolean save(File file, boolean isText) {
		try {
			SPUIBuilder builder = new SPUIBuilder();
			
			for (SPUIWinProc modifier : viewer.getLayoutWindow().getModifiers()) {
				builder.addComponent(modifier);
			}
			for (WinComponent window : viewer.getLayoutWindow().getChildren()) {
				builder.addComponent(window);
			}
			
			return save(file, isText, builder);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(SPUIEditor.this, "The SPUI couldn't be saved:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	
	private boolean save(File file, boolean isText, SPUIBuilder builder) {
		if (isText) {
			try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
				builder.generateSPUI().toArgScript().write(out);
				return true;
			} catch (Exception e) {
				JOptionPane.showMessageDialog(SPUIEditor.this, "The SPUI couldn't be saved:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		else {
			try (FileStreamAccessor out = new FileStreamAccessor(file, "rw", true)) {
				builder.generateSPUI().write(out);
				return true;
			} catch (Exception e) {
				JOptionPane.showMessageDialog(SPUIEditor.this, "The SPUI couldn't be saved:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
	}
	
	public void save() {
		
		if (save(originalFile == null ? smfxProjectItem.getFile() : originalFile, isTextSPUI)) {
			savedActionIndex = currentAction;
			updateIsSaved();
			
			if (saveAction != null) {
				saveAction.actionPerformed(new ActionEvent(SPUIEditor.this, 0, null));
			}
			
			fillHierarchyTree();
			setSelectedComponent(viewer.getActiveComponent());
		}
		
	}
	
	private void saveAs() {
		AdvancedFileChooser chooser = new AdvancedFileChooser(null, this, JFileChooser.FILES_ONLY, false, ChooserType.SAVE, FILEFILTER_SPUI, FILEFILTER_SPUI_T);
		String result = chooser.launch();
		
		if (result != null) {
			save(new File(result), result.endsWith(".spui_t"));
		}
	}
	
	private void saveAs(SPUIBuilder builder) {
		AdvancedFileChooser chooser = new AdvancedFileChooser(null, this, JFileChooser.FILES_ONLY, false, ChooserType.SAVE, FILEFILTER_SPUI, FILEFILTER_SPUI_T);
		String result = chooser.launch();
		
		if (result != null) {
			save(new File(result), result.endsWith(".spui_t"), builder);
		}
	}

	protected void removeComponentNode(SPUIComponent component) {
		hierarchyTreeModel.removeNodeFromParent((MutableTreeNode) component.getHierarchyTreePath().getLastPathComponent());
	}
	
	public FilteredTreeModel getHierarchyTreeModel() {
		return hierarchyTreeModel;
	}

	public void removeComponent(SPUIComponent component) {
		if (component != null) {
			RemoveComponentAction removeAction = new RemoveComponentAction(component);
			viewer.getLayoutWindow().removeComponent(removeAction, true);
			
//			for (WinComponent window : viewer.getMainWindows()) {
//				window.removeComponent(removeAction, true);
//			}
			
			addCommandAction(removeAction);
			
			removeComponentNode(component);
			
			viewer.setActiveComponent(null, false);
			viewer.repaint();
		}
	}
	
	public ComponentContainer getSelectedComponentContainer() {
		TreePath path = hierarchyTree.getSelectionPath();
		if (path == null) {
			return null;
		}
		
		return (ComponentContainer) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
	}
	
	public void addImageComponent(Image component) {
		imageComponents.add(component);
		
		component.fillHierarchyTree(hierarchyTreeModel, imagesNode, imagesNode.getChildCount());
	}
	
	public void addDrawableComponent(SPUIDrawable component) {
		drawableComponents.add(component);
		
		component.fillHierarchyTree(hierarchyTreeModel, drawablesNode, drawablesNode.getChildCount());
	}
	
	public void removeImageComponent(Image component) {
		imageComponents.remove(component);
		
		hierarchyTreeModel.removeNodeFromParent(component.getNode());
	}
	
	public void removeDrawableComponent(SPUIDrawable component) {
		drawableComponents.remove(component);
		
		hierarchyTreeModel.removeNodeFromParent(component.getNode());
	}

	public boolean hadErrors() {
		return hadErrors;
	}

	private void importAsComponent() throws InvalidBlockException, IOException, ArgScriptException {
		AdvancedFileChooser chooser = new AdvancedFileChooser(null, this, JFileChooser.FILES_ONLY, false, ChooserType.OPEN, FILEFILTER_SPUI, FILEFILTER_SPUI_T);
		String result = chooser.launch();
		
		if (result != null) {
			
			SPUIMain spui = new SPUIMain();
			
			if (result.endsWith(".spui_t")) {
				try (BufferedReader in = new BufferedReader(new FileReader(result))) {
					spui.parse(in);
				}
			} else {
				try (InputStreamAccessor in = new FileStreamAccessor(result, "r")) {
					spui.read(in);
				}
			}
			
			WinComponent selectedComp = (WinComponent) viewer.getActiveComponent();
			
			for (SPUIBlock block : spui.getBlocks()) {
				if (block.isRoot()) {
					SPUIComponent component = ResourceLoader.getComponent(block);
					component.setSPUIViewer(viewer);
					if (component instanceof WinComponent) {
						WinComponent mainWindow = (WinComponent) component;
						mainWindow.setParent(selectedComp);
						selectedComp.getChildren().add(mainWindow);
					}
					else if (component instanceof SPUIWinProc) {
						SPUIWinProc modifier = (SPUIWinProc) component;
						modifier.setParent(selectedComp);
						selectedComp.getModifiers().add(modifier);
					}
				}
			}
			
			fillHierarchyTree();
			setSelectedComponent(selectedComp);
			selectedComp.revalidate();
			viewer.repaint();
		}
	}
	
	
	private JSplitPane inspectorSplitPane;
	public JSplitPane getInspectorSplitPane() {
		return inspectorSplitPane;
	}
	
	public JTextField getSearchBar() {
		return tfSearchBar;
	}
	
	/**
	 * The property that controls whether the file is saved. If any changes have been done to the text since the last save, this value is false.
	 * @return
	 */
	public final ReadOnlyBooleanProperty isSavedProperty() {
		return isSaved.getReadOnlyProperty();
	}
}