package sporemodder.extras.spuieditor.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import sporemodder.extras.spuieditor.ComponentChooser;
import sporemodder.extras.spuieditor.InsertComponentAction;
import sporemodder.extras.spuieditor.SPUIEditor;
import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.extras.spuieditor.uidesigner.DesignerClass;
import sporemodder.extras.spuieditor.uidesigner.UIDesigner;

public class ComponentFactory {
	
	@SuppressWarnings("unchecked")
	public static List<Class<? extends SPUIComponent>> getClasses(String interfaceName) {
		try {
			List<Class<? extends SPUIComponent>> list = new ArrayList<Class<? extends SPUIComponent>>();
			
			String packageName = ComponentFactory.class.getPackage().getName();
			
			List<DesignerClass> classes = UIDesigner.Designer.getClasses();
	    	for (DesignerClass clazz : classes) {
	    		if (clazz.IsImplementingInterface(interfaceName) && clazz.getClassName() != null) {
	    			
						list.add((Class<? extends SPUIComponent>) Class.forName(packageName + "." + clazz.getClassName()));
	    		}
	    	}
	    	return list;
	    	
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static ComponentChooser<SPUIComponent> getComponentChooser(String interfaceName, SPUIViewer viewer) {
		// probably not a clean way, but it works
		boolean allowsExisting = false;
		if (interfaceName.contains("Drawable")) {
			allowsExisting = true;
		}
		
		return new ComponentChooser<SPUIComponent>(viewer.getEditor(), 
				"Choose a drawable", allowsExisting, getClasses(interfaceName), viewer.getEditor());
	}
	
	public static void fillInsertMenu(JMenu menu, final SPUIEditor editor, String interfaceName) {
		
		List<Class<? extends SPUIComponent>> list = getClasses(interfaceName);
		
		for (final Class<? extends SPUIComponent> supportedComponent : list) {
			
			JMenuItem menuItem = new JMenuItem(supportedComponent.getSimpleName());
			
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						
						InsertComponentAction action = new InsertComponentAction(
								editor.getSelectedComponentContainer(), 
								supportedComponent.getConstructor(SPUIViewer.class).newInstance(editor.getSPUIViewer()),
								editor.getSPUIViewer().getActiveComponent(),
								editor,
								-1);
						
						action.redo();
						
						editor.addCommandAction(action);
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			
			menu.add(menuItem);
		}
	}
	
    public static void main(String[] args) {
    	
//    	List<String> classNames = new ArrayList<String>();
//    	
//    	List<DesignerClass> classes = UIDesigner.Designer.getClasses();
//    	for (DesignerClass clazz : classes) {
//    		String className = clazz.getClassName();
//    		if (className != null) {
//    			classNames.add(className);
//    		}
//    	}
//    	
//    	Collections.sort(classNames);
//    	
//    	for (String s : classNames) {
//    		System.out.println(s);
//    	}
    	
    	List<Class<? extends SPUIComponent>> list = getClasses("IScrollbarDrawable");
    	for (Class<? extends SPUIComponent> s : list) {
    		System.out.println(s.getName());
    	}
    }
}
