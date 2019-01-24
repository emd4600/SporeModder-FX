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
import java.util.function.Predicate;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TreeItem;

public class FilterableTreeItem<T> extends TreeItem<T> {
	private final ObservableList<TreeItem<T>> sourceList = FXCollections.observableArrayList();
	private final FilteredList<TreeItem<T>> filteredList = new FilteredList<>(sourceList);
	private final ReadOnlyObjectWrapper<TreeItemPredicate<T>> predicate = new ReadOnlyObjectWrapper<>();

	public FilterableTreeItem(T item) {
		super(item);

		this.filteredList.predicateProperty().bind(Bindings.createObjectBinding(() -> {
			return child -> {
				// Set the predicate of child items to force filtering
				if (child instanceof FilterableTreeItem) {
					FilterableTreeItem<T> filterableChild = (FilterableTreeItem<T>) child;
					filterableChild.setPredicate(this.predicate.get());
				}
				// If there is no predicate, keep this tree item
				if (this.predicate.get() == null)
					return true;
				// If there are children, keep this tree item
				if (child.getChildren().size() > 0)
					return true;
				// Otherwise ask the TreeItemPredicate
				return this.predicate.get().test(this, child.getValue());
			};
		}, this.predicate));

		setHiddenFieldChildren(this.filteredList);
	}

	@SuppressWarnings("unchecked")
	protected void setHiddenFieldChildren(ObservableList<TreeItem<T>> list) {
		try {
			Field childrenField = TreeItem.class.getDeclaredField("children"); //$NON-NLS-1$
			childrenField.setAccessible(true);
			childrenField.set(this, list);

			Field declaredField = TreeItem.class.getDeclaredField("childrenListener"); //$NON-NLS-1$
			declaredField.setAccessible(true);
			list.addListener((ListChangeListener<? super TreeItem<T>>) declaredField.get(this));
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException("Could not set TreeItem.children", e); //$NON-NLS-1$
		}
	}

	public ObservableList<TreeItem<T>> getInternalChildren() {
		return sourceList;
	}
	
	/**
     * Set the predicate
     * @param predicate the predicate
     */
    public final void setPredicate(TreeItemPredicate<T> predicate) {
    	this.predicate.set(predicate);
    }

	public final ObjectProperty<TreeItemPredicate<T>> predicateProperty() {
		return predicate;
	}

	@FunctionalInterface
	public interface TreeItemPredicate<T> {

		boolean test(TreeItem<T> parent, T value);

		static <T> TreeItemPredicate<T> create(Predicate<T> predicate) {
			return (parent, value) -> predicate.test(value);
		}

	}
}
