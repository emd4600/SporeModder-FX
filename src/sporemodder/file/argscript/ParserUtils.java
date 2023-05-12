package sporemodder.file.argscript;


public class ParserUtils {
	
	@FunctionalInterface
	public static interface QuickFloatParser {
		public void setValue(float value);
	}
	public static <T> void createFloatParser(String keyword, ArgScriptStream<T> stream, QuickFloatParser parser) {
		stream.addParser(keyword, ArgScriptParser.create((parser_, line) -> {
			Number value = null;
			ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 1) && (value = stream.parseFloat(args, 0)) != null) {
				parser.setValue(value.floatValue());
			}
		})); 
	}
	public static <T> void createFloatParser(String keyword, ArgScriptBlock<T> block, QuickFloatParser parser) {
		block.addParser(keyword, ArgScriptParser.create((parser_, line) -> {
			Number value = null;
			ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 1) && (value = block.stream.parseFloat(args, 0)) != null) {
				parser.setValue(value.floatValue());
			}
		})); 
	}
	
	@FunctionalInterface
	public static interface QuickIntParser {
		public void setValue(int value);
	}
	public static <T> void createIntParser(String keyword, ArgScriptStream<T> stream, QuickIntParser parser) {
		stream.addParser(keyword, ArgScriptParser.create((parser_, line) -> {
			Number value = null;
			ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 1) && (value = stream.parseInt(args, 0)) != null) {
				parser.setValue(value.intValue());
			}
		})); 
	}
	public static <T> void createIntParser(String keyword, ArgScriptBlock<T> block, QuickIntParser parser) {
		block.addParser(keyword, ArgScriptParser.create((parser_, line) -> {
			Number value = null;
			ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 1) && (value = block.stream.parseInt(args, 0)) != null) {
				parser.setValue(value.intValue());
			}
		})); 
	}
	public static <T> void createFileIDParser(String keyword, ArgScriptStream<T> stream, QuickIntParser parser) {
		stream.addParser(keyword, ArgScriptParser.create((parser_, line) -> {
			Number value = null;
			ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 1) && (value = stream.parseFileID(args, 0)) != null) {
				parser.setValue(value.intValue());
			}
		})); 
	}
	public static <T> void createFileIDParser(String keyword, ArgScriptBlock<T> block, QuickIntParser parser) {
		block.addParser(keyword, ArgScriptParser.create((parser_, line) -> {
			Number value = null;
			ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 1) && (value = block.stream.parseFileID(args, 0)) != null) {
				parser.setValue(value.intValue());
			}
		})); 
	}
	
	@FunctionalInterface
	public static interface QuickBooleanParser {
		public void setValue(boolean value);
	}
	public static <T> void createBooleanParser(String keyword, ArgScriptStream<T> stream, QuickBooleanParser parser) {
		stream.addParser(keyword, ArgScriptParser.create((parser_, line) -> {
			Boolean value;
			ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 1) && (value = stream.parseBoolean(args, 0)) != null) {
				parser.setValue(value.booleanValue());
			}
		})); 
	}
	public static <T> void createBooleanParser(String keyword, ArgScriptBlock<T> block, QuickBooleanParser parser) {
		block.addParser(keyword, ArgScriptParser.create((parser_, line) -> {
			Boolean value;
			ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 1) && (value = block.stream.parseBoolean(args, 0)) != null) {
				parser.setValue(value.booleanValue());
			}
		})); 
	}
	
	@FunctionalInterface
	public static interface QuickEnumParser {
		public void setValue(int value);
	}
	public static <T> void createEnumParser(String keyword, ArgScriptStream<T> stream, ArgScriptEnum enum_, QuickEnumParser parser) {
		stream.addParser(keyword, ArgScriptParser.create((parser_, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 1)) {
				parser.setValue(enum_.get(args, 0));
			}
		})); 
	}
	public static <T> void createEnumParser(String keyword, ArgScriptBlock<T> block, ArgScriptEnum enum_, QuickEnumParser parser) {
		block.addParser(keyword, ArgScriptParser.create((parser_, line) -> {
			ArgScriptArguments args = new ArgScriptArguments();
			if (line.getArguments(args, 1)) {
				parser.setValue(enum_.get(args, 0));
			}
		})); 
	}
}
