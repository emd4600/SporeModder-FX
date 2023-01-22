package sporemodder.extras.spuieditor;

public interface CommandAction {

	public void undo();
	public void redo();
	
	/**
	 * Must return true if the action was significant (and therefore, has to be saved). Return false if this doesn't modify the SPUI at all.
	 * @return
	 */
	public boolean isSignificant();
}
