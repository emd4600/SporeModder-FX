package sporemodder.extras.spuieditor;

public interface UndoableEditor {

	public void addCommandAction(CommandAction action);
	public void undo();
	public void redo();
}
