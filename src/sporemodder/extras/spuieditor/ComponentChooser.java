package sporemodder.extras.spuieditor;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import sporemodder.MainApp;
import sporemodder.extras.spuieditor.ComponentValueAction.ComponentValueListener;
import sporemodder.extras.spuieditor.components.Image;
import sporemodder.extras.spuieditor.components.SPUIComponent;
import sporemodder.extras.spuieditor.components.SPUIDrawable;
import sporemodder.userinterface.JLabelLink;
import sporemodder.utilities.FilteredTree;
import sporemodder.utilities.FilteredTreeModel.TreeFilter;
import sporemodder.utilities.SearchSpec;

public class ComponentChooser<T extends SPUIComponent> extends StandardChooser {
	
	public class ComponentChooserDelegate implements ChooserDelegate {
		@Override
		public boolean isValid() {
			if (getChosenType() == CHOOSER_CREATE_NEW) {
				return !createComponentList.isSelectionEmpty();
			}
			else if (getChosenType() == CHOOSER_NONE) {
				return true;
			}
			else {
				TreePath path = getSelectedObject();
				if (path != null) {
					Object userObject = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
					if (componentTypes != null) {
						for (Class<? extends T> componentType : componentTypes) {
							if (componentType.isInstance(userObject)) {
								return true;
							}
						}
						return false;
					} else {
						return true;
					}
				}
				return false;
			}
		}

		@Override
		public JPanel getPreviewPanel() {
			if (getChosenType() == CHOOSER_CREATE_NEW) {
				JPanel panel = new JPanel();
				panel.setBorder(BorderFactory.createTitledBorder("Create new"));
				panel.setLayout(new BorderLayout());
				
				panel.add(createComponentList, BorderLayout.CENTER);
				
				return panel;
			}
			else {
				return null;
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void acceptAction(TreePath selectedObject, int chosenType) {
			if (chosenType == CHOOSER_CREATE_NEW) {
				String selectedValue = createComponentList.getSelectedValue();
				if (selectedValue != null) {
					
					Class<? extends T> type = null;
					
					for (Class<? extends T> supportedType : componentTypes) {
						if (supportedType.getSimpleName().equals(selectedValue)) {
							type = supportedType;
							break;
						}
					}
					
					if (type != null) {
						try {
							Constructor<? extends T> constructor = type.getConstructor(SPUIViewer.class);
							resultObject = constructor.newInstance(editor.getSPUIViewer());
							
							if (resultObject instanceof SPUIDrawable) {
								editor.addDrawableComponent((SPUIDrawable) resultObject);
							}
							else if (resultObject instanceof Image) {
								editor.addImageComponent((Image) resultObject);
							}
						
						} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			else if (chosenType == CHOOSER_NONE) {
				resultObject = null;
			}
			else {
				resultObject = (T) ((DefaultMutableTreeNode) selectedObject.getLastPathComponent()).getUserObject();
			}
		}

		@Override
		public void searchAction(String text, DocumentEvent event) {
			searchSpecs.clear();
			
			if (text != null && text.length() > 0) {
				List<String> strings = MainApp.getSearchStrings(text);
				
				for (String str : strings) {
					searchSpecs.add(new SearchSpec(str.toLowerCase()));
				}
				
			}
			
			getTreeView().updateUI();
			getTreeView().repaint();
		}
	}
	
	private final List<SearchSpec> searchSpecs = new ArrayList<SearchSpec>();
	private final JList<String> createComponentList = new JList<String>();
	private final List<Class<? extends T>> componentTypes;
	private SPUIEditor editor;
	private T resultObject;

	public ComponentChooser(Window parent, String title, boolean allowsExisting, List<Class<? extends T>> componentTypes, SPUIEditor editor) {
		super(parent, title, null);
		
		this.editor = editor;
		this.componentTypes = componentTypes;
		
		setDelegate(new ComponentChooserDelegate());
		
		if (!allowsExisting) {
			setHasExisting(false);
		}
		
		final FilteredTree treeView = getTreeView();
		
		treeView.getModel().setRoot((TreeNode) editor.getHierarchyTreeModel().getRoot());
		treeView.getModel().setFilterEnabled(true);
		treeView.getModel().setFilter(new TreeFilter() {
			private boolean checkChildren(DefaultMutableTreeNode node) {
				@SuppressWarnings("rawtypes")
				Enumeration e = node.children();
				while (e.hasMoreElements()) {
					boolean result = accept((DefaultMutableTreeNode) e.nextElement());
					if (result) {
						return true;
					}
				}
				return false;
			}
			@Override
			public boolean accept(DefaultMutableTreeNode node) {
				Object userObject = node.getUserObject();
				String str = userObject.toString().toLowerCase();
				
				for (SearchSpec spec : searchSpecs) {
					if (!str.contains(spec.getLowercaseString())) {
						return checkChildren(node);
					}
				}
				
				if (ComponentChooser.this.componentTypes != null) {
					for (Class<? extends T> componentType : ComponentChooser.this.componentTypes) {
						if (componentType.isInstance(userObject)) {
							return true;
						}
					}
					return checkChildren(node);
				}
				
				return true;
			}
		});
		treeView.updateUI();
		treeView.repaint();
		
		treeView.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 2 &&
						delegate.isValid()) {
					acceptAction();
				}
			}
		});
		
		
		String[] createComponents = new String[componentTypes.size()];
		for (int i = 0; i < createComponents.length; i++) {
			createComponents[i] = componentTypes.get(i).getSimpleName();
		}
		
		createComponentList.setListData(createComponents);
		createComponentList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				setOKButtonEnabled(!createComponentList.isSelectionEmpty());
			}
		});
		createComponentList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 2) {
					acceptAction();
				}
			}
		});
	}

	public T getResultantObject() {
		return resultObject;
	}
	
	public ComponentChooser<T> showChooser() {
		setDefaultCloseOperation(ComponentChooser.DISPOSE_ON_CLOSE);
		setVisible(true);
		return this;
	}
	
	private static void setLabelLink(JLabelLink labelLink, Object newValue) {
		labelLink.setActionActive(newValue != null);
		labelLink.setText(newValue == null ? "None" : newValue.toString());
	}
	
	private static void setObject(Object newValue, Object oldValue, JLabelLink labelLink, SPUIViewer viewer) {
		setLabelLink(labelLink, newValue);
		
		if (viewer != null && newValue != oldValue) {
			viewer.repaint();
		}
	}
	
	// Remember: whatever you change, you must change it in both methods
	
	@SuppressWarnings("unchecked")
	public static <T extends SPUIComponent> void showChooserAction(
			final SPUIComponent object, String arrayName, final int arrayIndex, ComponentChooser<T> chooser, final JLabelLink labelLink, final SPUIViewer viewer, final boolean needsRepaint) {
		
		try {
			final Field field = object.getClass().getDeclaredField(arrayName);
			field.setAccessible(true);
			T oldValue = ((T[]) field.get(object))[arrayIndex];
			
			if (oldValue == null) {
				chooser.setChosenType(CHOOSER_NONE);
			}
			else {
				chooser.setChosenType(CHOOSER_EXISTING);
				chooser.setSelectedObject(oldValue.getHierarchyTreePath());
			}
			
			chooser.showChooser();
			
			if (!chooser.wasCancelled()) {
				T newValue = chooser.getResultantObject();
				((T[]) field.get(object))[arrayIndex] = newValue;
				setObject(newValue, oldValue, labelLink, needsRepaint ? viewer : null);
				
				if (newValue != oldValue) {
					viewer.getEditor().addCommandAction(new ComponentValueAction<T>(oldValue, newValue, new ComponentValueListener<T>() {

						@Override
						public void valueChanged(T value) {
							try {
								Object previousValue = ((T[]) field.get(object))[arrayIndex];
								((T[]) field.get(object))[arrayIndex] = value;
								setObject(value, previousValue, labelLink, needsRepaint ? viewer : null);
							} catch (IllegalArgumentException | IllegalAccessException e) {
								e.printStackTrace();
							}
						}
						
					}));
				}
			}
			
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends SPUIComponent> void showChooserAction(
			final SPUIComponent object, String fieldName, ComponentChooser<T> chooser, final JLabelLink labelLink, final SPUIViewer viewer, final boolean needsRepaint) {
		
		try {
			
			final Field field = object.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			T oldValue = (T) field.get(object);
			
			if (oldValue == null) {
				chooser.setChosenType(CHOOSER_NONE);
			}
			else {
				chooser.setChosenType(CHOOSER_EXISTING);
				chooser.setSelectedObject(oldValue.getHierarchyTreePath());
			}
			
			chooser.showChooser();
			
			if (!chooser.wasCancelled()) {
				T newValue = chooser.getResultantObject();
				field.set(object, newValue);
				setObject(newValue, oldValue, labelLink, needsRepaint ? viewer : null);
				
				if (newValue != oldValue) {
					viewer.getEditor().addCommandAction(new ComponentValueAction<T>(oldValue, newValue, new ComponentValueListener<T>() {

						@Override
						public void valueChanged(T value) {
							try {
								Object previousValue = field.get(object);
								field.set(object, value);
								setObject(value, previousValue, labelLink, needsRepaint ? viewer : null);
							} catch (IllegalArgumentException | IllegalAccessException e) {
								e.printStackTrace();
							}
						}
						
					}));
				}
			}
			
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		
	}
	
	public static <T extends SPUIComponent> void showChooserAction(
			final ComponentChooserCallback<T> callback, ComponentChooser<? extends T> chooser, final JLabelLink labelLink, final SPUIViewer viewer) {
		
		T oldValue = callback.getValue();
		
		if (oldValue == null) {
			chooser.setChosenType(CHOOSER_NONE);
		}
		else {
			chooser.setChosenType(CHOOSER_EXISTING);
			chooser.setSelectedObject(oldValue.getHierarchyTreePath());
		}
		
		chooser.showChooser();
		
		if (!chooser.wasCancelled()) {
			T newValue = chooser.getResultantObject();
			callback.valueChanged(newValue);
			setLabelLink(labelLink, newValue);
			
			if (newValue != oldValue) {
				viewer.getEditor().addCommandAction(new ComponentValueAction<T>(oldValue, newValue, new ComponentValueListener<T>() {

					@Override
					public void valueChanged(T value) {
						callback.valueChanged(value);
						setLabelLink(labelLink, value);
					}
					
				}));
			}
		}
		
	}
	
	public static <T extends SPUIComponent> ComponentChooser<T> getChooser(SPUIViewer viewer, Class<T> clazz) {
		List<Class<? extends T>> components = new ArrayList<Class<? extends T>>();
		components.add(clazz);
		
		return new ComponentChooser<T>(viewer.getEditor(), 
				"Choose a " + clazz.getSimpleName(), true, components, viewer.getEditor());
	}
	
	public static interface ComponentChooserCallback<T> {
		public T getValue();
		public void valueChanged(T value);
	}

}
