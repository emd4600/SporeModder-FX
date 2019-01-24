/****************************************************************************
* Copyright (C) 2019 Eric Mor
*
* This file is part of SporeModder FX.
*
* SporeModder FX is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
****************************************************************************/
package sporemodder.view;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.function.Predicate;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TreeItem;
import sporemodder.ProjectManager;
import sporemodder.util.ProjectItem;

public class ProjectTreeItem extends TreeItem<ProjectItem> {

	private boolean isLoaded;
	private final ObservableList<ProjectTreeItem> sourceList = FXCollections.observableArrayList();
	private final FilteredList<ProjectTreeItem> filteredList = new FilteredList<>(sourceList);
	private final ReadOnlyObjectWrapper<TreeItemPredicate> predicate = new ReadOnlyObjectWrapper<>();
	
	private boolean matchesSearch = false;

	public ProjectTreeItem(ProjectItem item) {
		super(item);
		item.setTreeItem(this);
		
//		sourceList.addListener((ListChangeListener<? super ProjectTreeItem>) c -> {
//			while (c.next()) {
//				if (c.wasAdded()) {
//					for (ProjectTreeItem item : c.getAddedSubList()) {
//						item.parent
//					}
//				}
//			}
//		});
		
		setHiddenFieldChildren(this.filteredList);
	}

	@SuppressWarnings("unchecked")
	protected void setHiddenFieldChildren(ObservableList<ProjectTreeItem> list) {
		try {
			Field childrenField = TreeItem.class.getDeclaredField("children"); //$NON-NLS-1$
			childrenField.setAccessible(true);
			childrenField.set(this, list);

			Field declaredField = TreeItem.class.getDeclaredField("childrenListener"); //$NON-NLS-1$
			declaredField.setAccessible(true);
			list.addListener((ListChangeListener<? super TreeItem<ProjectItem>>) declaredField.get(this));
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException("Could not set TreeItem.children", e); //$NON-NLS-1$
		}
	}
	
	@Override
	public boolean isLeaf() {
		if (getValue() == null) {
			return super.isLeaf();
		} else {
			return !getValue().isFolder();
		}
	}

	@Override
	public ObservableList<TreeItem<ProjectItem>> getChildren() {
		if (!isLoaded) {
			isLoaded = true;
			ProjectManager.get().loadItemFolder(this);
		}
		return super.getChildren();
	}

	public boolean isLoaded() {
		return isLoaded;
	}

	public void setLoadedChildren(Collection<ProjectTreeItem> children) {
		isLoaded = true;
		sourceList.setAll(children);
	}

	public void requestReload() {
		isLoaded = false;
	}

	public ObservableList<ProjectTreeItem> getInternalChildren() {
		return this.sourceList;
	}
	
	/**
     * Set the predicate
     * @param predicate the predicate
     */
    public final void setPredicate(TreeItemPredicate predicate, Observable ... dependencies) {
    	this.predicate.set(predicate);
    	
    	filteredList.predicateProperty().bind(Bindings.createObjectBinding(() -> {
            return child -> {
            	
                // Set the predicate of child items to force filtering
                child.setPredicate(this.predicate.get());
                
                // If there is no predicate, keep this tree item
                if (this.predicate.get() == null)
                    return true;

                
                // Otherwise ask the TreeItemPredicate
                return this.predicate.get().test(this, child.getValue());
            };
        }, dependencies));
    }
    
    public final ObjectProperty<Predicate<? super ProjectTreeItem>> predicateProperty() {
    	return filteredList.predicateProperty();
    }
    
    public final boolean getMatchesSearch() {
    	return matchesSearch;
    }
    
    public final void setMatchesSearch(boolean matchesSearch) {
    	this.matchesSearch = matchesSearch;
    }
	
	@FunctionalInterface
	public interface TreeItemPredicate {
	 
	    boolean test(ProjectTreeItem parent, ProjectItem value);
	 
	    static TreeItemPredicate create(Predicate<ProjectItem> predicate) {
	        return (parent, value) -> predicate.test(value);
	    }
	 
	}

	public void propagateMatchesSearch(boolean matches) {
		this.matchesSearch = matches;
		for (ProjectTreeItem item : sourceList) item.propagateMatchesSearch(matches);
	}
}
