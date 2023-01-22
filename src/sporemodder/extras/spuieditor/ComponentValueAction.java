package sporemodder.extras.spuieditor;

public class ComponentValueAction<T> implements CommandAction {
	
	public interface ComponentValueListener<T> {
		public void valueChanged(T value);
	}
	
	private T originalValue;
	private T value;
	private ComponentValueListener<T> valueListener;
	
	public ComponentValueAction(T originalValue, T value, ComponentValueListener<T> valueListener) {
		assert(valueListener != null);
		this.originalValue = originalValue;
		this.value = value;
		this.valueListener = valueListener;
	}

	@Override
	public void undo() {
		valueListener.valueChanged(originalValue);
	}

	@Override
	public void redo() {
		valueListener.valueChanged(value);
	}

	@Override
	public boolean isSignificant() {
		return true;
	}
}
