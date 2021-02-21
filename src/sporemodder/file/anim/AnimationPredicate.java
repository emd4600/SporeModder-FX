package sporemodder.file.anim;

import java.io.IOException;
import java.io.Writer;

import emord.filestructures.StreamReader;
import emord.filestructures.StreamWriter;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptEnum;
import sporemodder.file.argscript.ArgScriptLine;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;

// Different predicates (checks) on the creature that can be used to invalidate an animation (like when you have more than one animation in a TLSA) or an event
public class AnimationPredicate {
	public static final int FLAG_UPRIGHT_SPINE = 1;
	public static final int FLAG_HAS_GRASPERS = 2;
	public static final int FLAG_HAS_FEET = 4;
	
	public static final ArgScriptEnum ENUM_PREDICATE = new ArgScriptEnum();
	static {
		ENUM_PREDICATE.add(FLAG_UPRIGHT_SPINE, "uprightSpine");
		ENUM_PREDICATE.add(FLAG_HAS_GRASPERS, "hasGraspers");
		ENUM_PREDICATE.add(FLAG_HAS_FEET, "hasFeet");
	}
	
	public int flags1;
	public int flags2;
	
	public void read(StreamReader stream) throws IOException {
		flags1 = stream.readLEInt();
		flags2 = stream.readLEInt();
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeLEInt(flags1);
		stream.writeLEInt(flags2);
	}
	
	public boolean isDefault() {
		return flags1 == 0 && flags2 == 0;
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		if ((flags1 & FLAG_UPRIGHT_SPINE) != 0) {
			writer.arguments(ENUM_PREDICATE.get(FLAG_UPRIGHT_SPINE), (flags2 & FLAG_UPRIGHT_SPINE) != 0);
		}
		if ((flags1 & FLAG_HAS_GRASPERS) != 0) {
			writer.arguments(ENUM_PREDICATE.get(FLAG_HAS_GRASPERS), (flags2 & FLAG_HAS_GRASPERS) != 0);
		}
		if ((flags1 & FLAG_HAS_FEET) != 0) {
			writer.arguments(ENUM_PREDICATE.get(FLAG_HAS_FEET), (flags2 & FLAG_HAS_FEET) != 0);
		}
	}
	
	public void parse(ArgScriptArguments args, ArgScriptStream<SPAnimation> stream) {
		for (int i = 0; i < args.size(); i += 2) {
			int flag = ENUM_PREDICATE.get(args, i);
			boolean b = stream.parseBoolean(args, i + 1);
			
			flags1 |= flag;
			if (b) flags2 |= flag;
			else flags2 &= ~flag;
		}
	}
	
	public static void main(String[] args) {
		
		for (int i = 0; i < 0xFF; ++i) {
			System.out.println("0x" + Integer.toHexString(i) + "\t0x" + Integer.toHexString(i & (-i)));
		}
	}
}
