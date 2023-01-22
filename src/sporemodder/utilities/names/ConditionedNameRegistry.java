package sporemodder.utilities.names;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ConditionedNameRegistry extends NameRegistry {
	
	private Condition condition;
	private NameRegistry registry;
	/**
	 * NameRegistry to be used if <code>condition</code> evaluates to false.
	 */
	private NameRegistry elseRegistry;

	public ConditionedNameRegistry(BufferedReader in, String conditionStr) throws IOException {
		super(in);
		condition = new Condition(conditionStr);
	}

	@Override
	public void read(BufferedReader in) throws IOException {
		if (registry == null) {
			registry = new NameRegistry();
			elseRegistry = new NameRegistry();
		}
		
		String line;
		boolean insideElse = false;
		while ((line = in.readLine()) != null) {
			
			String str = line.split("//")[0].trim();
			
			if (str.length() == 0) continue;
			
			// Special case, tokens
			if (str.startsWith("#")) {
				String tokenStr = str.substring(1);
				
				if (tokenStr.startsWith(TOKEN_IF)) {
					ConditionedNameRegistry reg = new ConditionedNameRegistry(in, tokenStr.substring(2).trim());
					if (!insideElse) {
						registry.subregs.add(reg);
					} else {
						elseRegistry.subregs.add(reg);
					}
				}
				else if (tokenStr.startsWith(TOKEN_END)) {
					// Stop reading this name registry.
					break;
				}
			}
			else {
//				System.out.println(registry + "\t" + elseRegistry);
				if (!insideElse) {
					registry.parseEntry(str);
				} else {
					elseRegistry.parseEntry(str);
				}
			}
		}
	}
	
	@Override
	public String getName(int hash) {
		if (condition.evaluate()) {
			return registry.getName(hash);
		}
		else {
			if (elseRegistry != null) {
				return elseRegistry.getName(hash);
			} else {
				return null;
			}
		}
	}
	
	@Override
	public int getHash(String name) {
		if (condition.evaluate()) {
			Integer result = registry.getHash(name);
			if (result == null) return -1;
			else return result;
		}
		else {
			if (elseRegistry != null) {
				Integer result = elseRegistry.getHash(name);
				if (result == null) return -1;
				else return result;
			} else {
				return -1;
			}
		}
	}
	
	@Override
	public List<String> getNames() {
		List<String> result = new ArrayList<String>(registry.getNames());
		result.addAll(elseRegistry.getNames());
		return result;
	}
}
