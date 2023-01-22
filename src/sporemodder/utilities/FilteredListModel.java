package sporemodder.utilities;

import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class FilteredListModel<T> extends AbstractListModel<T> {
    public static interface Filter {
        boolean accept(Object element);
    }

    private final ListModel<T> _source;
    private Filter _filter;
    private final ArrayList<Integer> _indices = new ArrayList<Integer>();

    public FilteredListModel(ListModel<T> source) {
        if (source == null)
            throw new IllegalArgumentException("Source is null");
        _source = source;
        _source.addListDataListener(new ListDataListener() {
            public void intervalRemoved(ListDataEvent e) {
                doFilter();
            }

            public void intervalAdded(ListDataEvent e) {
                doFilter();
            }

            public void contentsChanged(ListDataEvent e) {
                doFilter();
            }
        });
    }

    public void setFilter(Filter f) {
        _filter = f;
        doFilter();
    }
    
    public void doFilter() {
        _indices.clear();

        Filter f = _filter;
        if (f != null) {
            int count = _source.getSize();
            for (int i = 0; i < count; i++) {
                Object element = _source.getElementAt(i);
                if (f.accept(element)) {
                    _indices.add(i);
                }
            }
            fireContentsChanged(this, 0, getSize() - 1);
        }
    }

    public int getSize() {
        return (_filter != null) ? _indices.size() : _source.getSize();
    }

    public T getElementAt(int index) {
        return (_filter != null) ? _source.getElementAt(_indices.get(index)) : _source.getElementAt(index);
    }
}
