package sporemodder.files.formats.spui;

public interface SPUIObject {
	
	public int getBlockIndex();
	public SPUIMain getParent();
	public void setParent(SPUIMain parent);
	public int getObjectType();
	public String getTypeString();
	
	public abstract class SPUIDefaultObject implements SPUIObject {
		
		protected SPUIMain parent;

		public SPUIDefaultObject() {
		}

		public abstract int getBlockIndex();
		
		public SPUIMain getParent() {
			return parent;
		}
		
		public void setParent(SPUIMain parent) {
			this.parent = parent;
		}
	}
}
