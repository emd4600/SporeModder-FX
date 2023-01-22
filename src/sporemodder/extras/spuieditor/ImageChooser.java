package sporemodder.extras.spuieditor;

import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import sporemodder.MainApp;
import sporemodder.ProjectManager;
import sporemodder.files.formats.spui.SPUIFileResource;
import sporemodder.util.ProjectItem;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.Project;
import sporemodder.utilities.ProjectTreeModel;
import sporemodder.utilities.ProjectTreeNode;
import sporemodder.utilities.SearchSpec;

public class ImageChooser extends StandardChooser {
	
	private final List<SearchSpec> searchSpecs = new ArrayList<SearchSpec>();
	private ProjectTreeModel treeModel;
	
	public ImageChooser(Window parent, String title) {
		super(parent, title, null);
		
		setHasNone(false);
		setHasCreateNew(false);
		setDelegate(new ChooserDelegate() {

			@Override
			public JPanel getPreviewPanel() {
				if (getChosenType() == CHOOSER_EXISTING) {
					JPanel panel = new JPanel();
					panel.setBorder(BorderFactory.createTitledBorder("Preview"));
					
					TreePath path = getSelectedObject();
					if (path != null) {
						File file = ProjectManager.get().getFile(Project.getRelativePath(path));
						if (file != null && file.isFile()) {
							try {
								BufferedImage image = ImageIO.read(file);
								if (image != null) {
									panel.add(new JLabel(new ImageIcon(image)));
								}
							}
							catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					
					return panel;
				}
				
				// default, just a panel with "Preview" title
				JPanel panel = new JPanel();
				panel.setBorder(BorderFactory.createTitledBorder("Preview"));
				return panel;
			}

			@Override
			public void acceptAction(TreePath selectedObject, int chosenType) {
			}

			@Override
			public boolean isValid() {
				if (getChosenType() == CHOOSER_NONE) {
					return true;
				}
				else {
					TreePath path = getSelectedObject();
					if (path != null) {
						File file = ProjectManager.get().getFile(Project.getRelativePath(path));
						if (file != null && file.isFile()) {
							try {
								return ImageIO.read(file) != null;
							}
							catch (IOException e) {
								return false;
							}
						}
					}
					return false;
				}
			}

			@Override
			public void searchAction(String text, DocumentEvent event) {
				searchSpecs.clear();
				
				if (text != null && text.length() > 0) {
					treeModel.setFilterSearch(true);
					List<String> strings = MainApp.getSearchStrings(text);
					
					for (String str : strings) {
						searchSpecs.add(new SearchSpec(str.toLowerCase()));
					}
					
					((ProjectTreeNode) ImageChooser.this.getTreeView().getModel().getRoot()).searchFast(searchSpecs, false);
				}
				else {
					treeModel.setFilterSearch(true);
				}
				
				ImageChooser.this.getTreeView().updateUI();
				ImageChooser.this.getTreeView().repaint();
			}
			
		});
		
		getTreeView().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 2 && delegate.isValid()) {
					acceptAction();
				}
			}
		});
		
		/*ProjectTreeNode rootNode;
		
		if (MainApp.getUserInterface() != null) {
			rootNode = (ProjectTreeNode) MainApp.getUserInterface().getProjectPanel().getTreeModel().getRoot();
		}
		else {
			rootNode = new ProjectTreeNode(MainApp.getCurrentProject().getProjectName(), true);
		}*/
		ProjectTreeNode rootNode = new ProjectTreeNode(ProjectManager.get().getTreeView().getRoot().getValue());
		
		treeModel = new ProjectTreeModel(rootNode);
		
//		treeModel.setFilter(treeModel.new ProjectTreeFilter() {
//			@Override
//			public boolean accept(DefaultMutableTreeNode node) {
//				if (super.accept(node)) {
//					return true;
//				}
//				ProjectTreeNode n = (ProjectTreeNode) node;
//				String str = n.name.toLowerCase();
//				List<SearchSpec> specs = ImageChooser.this.getSearchSpecs();
//				for (SearchSpec spec : specs) {
//					if (!spec.getLowercaseString().equals(str)) {
//						return false;
//					}
//				}
//				return true;
//			}
//		});
		
		//MainApp.getCurrentProject().loadNodesFastEx(treeModel, rootNode);
		getTreeView().setModel(treeModel);
	}
	
	private DefaultMutableTreeNode searchNode(DefaultMutableTreeNode parentNode, String string) {
		 @SuppressWarnings("rawtypes")
		 Enumeration e = parentNode.children();
		 
		 while (e.hasMoreElements()) {
			 DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
			 if (string.equals(node.toString())) {
				 return node;
			 }
		 }
		 
		 return null;
	}
	
	public void setSelectedObject(SPUIFileResource file) {
		 String groupStr;
		 String nameStr;
		 
		 if (file.getRealPath() == null) {
			 groupStr = Hasher.getFileName(file.getGroupID());
			 nameStr = Hasher.getFileName(file.getInstanceID()) + "." + Hasher.getTypeName(file.getTypeID());
		 }
		 else {
			 String[] splits = file.getRealPath().split("!", 2);
			 groupStr = splits[0];
			 nameStr = splits[1];
		 }
		 
		 ((DefaultMutableTreeNode) getTreeView().getModel().getRoot()).children();
		 
		 
		 
		 DefaultMutableTreeNode groupNode = searchNode((DefaultMutableTreeNode) getTreeView().getModel().getRoot(), groupStr);
		 
		 if (groupNode != null) {
			 DefaultMutableTreeNode node = searchNode(groupNode, nameStr);
			 if (node != null) {
				 super.setSelectedObject(new TreePath(node.getPath()));
			 }
		 }
	}
}
