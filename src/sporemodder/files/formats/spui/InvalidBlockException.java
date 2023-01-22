package sporemodder.files.formats.spui;

public class InvalidBlockException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7988981141036016557L;
	
	public SPUIBlock block;
	public SPUISection section;

	public InvalidBlockException(String arg0, SPUIBlock block, SPUISection section) {
		super(arg0);
		this.block = block;
		this.section = section;
	}

	public InvalidBlockException(String arg0, SPUISection section) {
		super(arg0);
		this.section = section;
	}
	
	public InvalidBlockException(String arg0) {
		super(arg0);
	}
}
