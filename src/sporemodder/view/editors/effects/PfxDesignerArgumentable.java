package sporemodder.view.editors.effects;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for any ArgScript object that can take arguments. 
 */
public abstract class PfxDesignerArgumentable {
	protected String name;
	protected final List<PfxDesignerArgument> arguments = new ArrayList<>();
	
	public List<PfxDesignerArgument> getArguments() {
		return arguments;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSingleArgument() {
		return arguments.size() == 1;
	}
	
	public int getArgumentCount() {
		return arguments.size();
	}
	
	/**
	 * Returns the index of the first optional argument. For any index greater or equal than this
	 * the argument is assumed to be optional. Returns 'arguments.size()' if no argument is optional.
	 * @return
	 */
	public int getOptionalIndex() {
		for (int i = 0; i < arguments.size(); ++i) {
			if (arguments.get(i).isOptional()) return i;
		}
		return arguments.size();
	}
}
