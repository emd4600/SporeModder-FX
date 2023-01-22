package sporemodder.utilities.names;

import sporemodder.utilities.Hasher;

public class Condition {
	private static enum CompareFunc 
	{
		EQUAL {
			protected boolean compare(Object a, Object b) {
				return a.equals(b);
			}
		}, 
		NOT_EQUAL {
			protected boolean compare(Object a, Object b) {
				return !a.equals(b);
			}
		};
		
		protected abstract boolean compare(Object a, Object b);
	};
	
	private static Object object;
	
	public String left;
	public CompareFunc compareFunc;
	public Object right;
	
	public Condition(String str) {
		parse(str);
	}
	
	private boolean parse(String str) {
		// LEFT_IDENTIFIER compareFunc RIGHT_IDENTIFIER
		String[] splits = str.split(" ");
		if (splits.length != 3) {
			System.err.println("Wrong number of tokens");
			return false;
		}
		
		left = splits[0];
		String s = splits[2];
		//TODO support for strings?
		right = Hasher.decodeInt(s);
		
		if (splits[1].equals("==")) {
			compareFunc = CompareFunc.EQUAL;
		}
		else if (splits[1].equals("!=")) {
			compareFunc = CompareFunc.NOT_EQUAL;
		}
		else {
			System.err.println("Unsupported compare function");
			return false;
		}
		
		return true;
	}
	
	protected boolean evaluate() {
		if (object != null) {
			return compareFunc.compare(object, right);
		}
		else {
			return false;
		}
	}
	
	public static void setObject(Object obj) {
		object = obj;
	}
}
