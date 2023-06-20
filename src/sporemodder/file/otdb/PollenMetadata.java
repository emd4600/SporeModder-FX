package sporemodder.file.otdb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import sporemodder.HashManager;
import sporemodder.file.ResourceKey;
import sporemodder.file.argscript.ArgScriptArguments;
import sporemodder.file.argscript.ArgScriptParser;
import sporemodder.file.argscript.ArgScriptStream;
import sporemodder.file.argscript.ArgScriptWriter;
import sporemodder.file.effects.ResourceID;
import sporemodder.file.filestructures.Stream.StringEncoding;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

public class PollenMetadata {
	public int metadataVersion;
	public long assetID = -1;
	public final ResourceKey assetKey = new ResourceKey();
	public final ResourceKey parentAssetKey = new ResourceKey();
	public long parentAssetID = -1;
	public long originalParentAssetID = -1;
	public long timeCreated = -1;
	public long timeDownloaded = -1;
	public String authorName;
	public ResourceID authorNameLocale;
	public long authorID = -1;
	public int useLocale;
	public boolean isShareable;
	public String name;
	public ResourceID nameLocale;
	public String description;
	public ResourceID descriptionLocale;
	public final List<String> authors = new ArrayList<>();
	public final List<String> tags = new ArrayList<>();
	public ResourceID tagsLocale;
	public final List<Integer> consequenceTraits = new ArrayList<>();
	
	public void clear() {
		metadataVersion = 0;
		assetID = -1;
		originalParentAssetID = -1;
		timeCreated = -1;
		timeDownloaded = -1;
		timeDownloaded = -1;
		authorID = -1;
		useLocale = 0;
		isShareable = false;
		authors.clear();
		tags.clear();
		consequenceTraits.clear();
		authorNameLocale = null;
		nameLocale = null;
		descriptionLocale = null;
		tagsLocale = null;
		name = null;
		description = null;
		authorName = null;
	}
	
	private static long readLong(StreamReader stream) throws IOException {
		long value1 = stream.readUInt();
		long value2 = stream.readUInt();
		return (value1 << 32) | (value2 & 0xFFFFFFFFL);
	}
	private static void writeLong(StreamWriter stream, long value) throws IOException {
		stream.writeUInt((value >> 32) & 0xFFFFFFFFL);
		stream.writeUInt(value & 0xFFFFFFFFL);
	}
	
	private static void readReorderedKey(StreamReader stream, ResourceKey key) throws IOException {
		key.setTypeID(stream.readInt());
		key.setGroupID(stream.readInt());
		key.setInstanceID(stream.readInt());
	}
	private static void writeReorderedKey(StreamWriter stream, ResourceKey key) throws IOException {
		stream.writeInt(key.getTypeID());
		stream.writeInt(key.getGroupID());
		stream.writeInt(key.getInstanceID());
	}
	
	private static String removeSpecialCharacters(String str) {
		return str.replace("\n", "\\n").replace("\t", "\\t").replace("\r", "\\r").replace("\"", "\\\"");
	}
	private static String insertSpecialCharacters(String str) {
		return str.replace("\\n", "\n").replace("\\t", "\t").replace("\\r", "\r").replace("\\\"", "\"");
	}

	public void read(StreamReader stream) throws IOException {
		metadataVersion = stream.readInt();
		if (metadataVersion > 13)
			throw new IOException("Unsupported .pollen_metadata version " + metadataVersion);
		
		assetID = readLong(stream);
		readReorderedKey(stream, assetKey);
		readReorderedKey(stream, parentAssetKey);
		
		if (metadataVersion >= 10) {
			parentAssetID = readLong(stream);
		}
		if (metadataVersion >= 12) {
			originalParentAssetID = readLong(stream);
		}
		
		timeCreated = readLong(stream);
		
		if (metadataVersion >= 9) {
			if (metadataVersion == 10) {
				long value1 = stream.readUInt();
				if (value1 != 0xFFFFFFFFL) {
					long value2 = stream.readUInt();
					timeDownloaded = (value1 << 32) | (value2 & 0xFFFFFFFFL);
				}
			}
			else {
				timeDownloaded = readLong(stream);
			}
		}
		
		if (stream.readInt() == 0) {
			authorID = readLong(stream);
			authorName = stream.readString(StringEncoding.UTF16LE, stream.readInt());
			useLocale = -1;
			name = stream.readString(StringEncoding.UTF16LE, stream.readInt());
			description = stream.readString(StringEncoding.UTF16LE, stream.readInt());
		}
		else {
			useLocale = 1;
			
			int tableID = stream.readInt();
			int instanceID = stream.readInt();
			authorNameLocale = new ResourceID(tableID, instanceID);
			
			instanceID = stream.readInt();
			nameLocale = new ResourceID(tableID, instanceID);
			
			instanceID = stream.readInt();
			descriptionLocale = new ResourceID(tableID, instanceID);
		}
		
		if (metadataVersion >= 1) {
			if (metadataVersion >= 2) {
				int count = stream.readInt();
				for (int i = 0; i < count; i++) {
					authors.add(stream.readString(StringEncoding.ASCII, stream.readInt()));
				}
			}
			else {
				authors.add(stream.readString(StringEncoding.ASCII, stream.readInt()));
			}
		}
		
		int value = 0;
		if (metadataVersion >= 13) {
			value = stream.readInt();
		}
		if (value == 0) {
			int count = stream.readInt();
			for (int i = 0; i < count; i++) {
				tags.add(stream.readString(StringEncoding.UTF16LE, stream.readInt()));
			}
		}
		else {
			int tableID = stream.readInt();
			int instanceID = stream.readInt();
			tagsLocale = new ResourceID(tableID, instanceID);
		}
		
		if (metadataVersion >= 8) {
			isShareable = stream.readInt() != 0;
			int count = stream.readInt();
			for (int i = 0; i < count; i++) {
				consequenceTraits.add(stream.readInt());
			}
		}
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeInt(metadataVersion);
		
		writeLong(stream, assetID);
		writeReorderedKey(stream, assetKey);
		writeReorderedKey(stream, parentAssetKey);
		
		if (metadataVersion >= 10) {
			writeLong(stream, parentAssetID);
		}
		if (metadataVersion >= 12) {
			writeLong(stream, originalParentAssetID);
		}
		
		writeLong(stream, timeCreated);
		
		if (metadataVersion >= 9) {
			if (metadataVersion == 10) {
				if (timeDownloaded == 0) {
					stream.writeInt(-1);
				}
				else {
					writeLong(stream, timeDownloaded);
				}
			}
			else {
				writeLong(stream, timeDownloaded);
			}
		}
		
		if (useLocale == 1) {
			stream.writeInt(-1);
			stream.writeInt(authorNameLocale.getGroupID());
			stream.writeInt(authorNameLocale.getInstanceID());
			stream.writeInt(nameLocale.getInstanceID());
			stream.writeInt(descriptionLocale.getInstanceID());
		}
		else {
			stream.writeInt(0);
			writeLong(stream, authorID);
			stream.writeInt(authorName.length());
			stream.writeString(authorName, StringEncoding.UTF16LE);
			stream.writeInt(name.length());
			stream.writeString(name, StringEncoding.UTF16LE);
			stream.writeInt(description.length());
			stream.writeString(description, StringEncoding.UTF16LE);
		}
		
		if (metadataVersion >= 1) {
			if (metadataVersion >= 2) {
				stream.writeInt(authors.size());
				for (String text : authors) {
					stream.writeInt(text.length());
					stream.writeString(text, StringEncoding.ASCII);
				}
			}
			else {
				if (authors.isEmpty()) stream.writeInt(0);
				else {
					stream.writeInt(authors.get(0).length());
					stream.writeString(authors.get(0), StringEncoding.ASCII);
				}
			}
		}
		
		if (tagsLocale != null && metadataVersion >= 13) {
			stream.writeInt(-1);
			stream.writeInt(tagsLocale.getGroupID());
			stream.writeInt(tagsLocale.getInstanceID());
		}
		else {
			if (metadataVersion >= 13) stream.writeInt(0);
			stream.writeInt(tags.size());
			for (String text : tags) {
				stream.writeInt(text.length());
				stream.writeString(text, StringEncoding.UTF16LE);
			}
		}
		
		if (metadataVersion >= 8) {
			stream.writeInt(isShareable ? -1 : 0);
			stream.writeInt(consequenceTraits.size());
			for (int value : consequenceTraits) {
				stream.writeInt(value);
			}
		}
	}
	
	public ArgScriptWriter toArgScript() {
		ArgScriptWriter writer = new ArgScriptWriter();
		toArgScript(writer);
		return writer;
	}
	
	public void toArgScript(ArgScriptWriter writer) {
		writer.command("metadataVersion").ints(metadataVersion);
		writer.blankLine();
		writer.command("assetID").arguments(assetID);
		writer.command("assetKey").arguments(assetKey);
		writer.command("parentAssetKey").arguments(parentAssetKey);
		if (metadataVersion >= 10) writer.command("parentAssetID").arguments(parentAssetID);
		if (metadataVersion >= 12) writer.command("originalParentAssetID").arguments(originalParentAssetID);
		writer.command("timeCreated").arguments(timeCreated);
		writer.command("timeDownloaded").arguments(timeDownloaded);
		
		if (useLocale == -1) {
			writer.command("authorID").arguments(authorID);
			writer.command("authorName").literal(removeSpecialCharacters(authorName));
			writer.command("name").literal(removeSpecialCharacters(name));
			writer.command("description").literal(removeSpecialCharacters(description));
		}
		else {
			writer.command("authorNameLocale").arguments(authorNameLocale);
			writer.command("nameLocale").arguments(nameLocale);
			writer.command("descriptionLocale").arguments(descriptionLocale);
		}
		
		writer.command("authors");
		for (String value : authors) {
			writer.literal(removeSpecialCharacters(value));
		}
		if (tagsLocale != null) {
			writer.command("tagsLocale").arguments(tagsLocale);
		}
		else {
			writer.command("tags");
			for (String value : tags) {
				writer.literal(removeSpecialCharacters(value));
			}
		}
		if (metadataVersion >= 8) {
			writer.command("isShareable").arguments(isShareable);
			writer.command("consequenceTraits");
			for (int value : consequenceTraits) {
				writer.arguments(HashManager.get().getFileName(value));
			}
		}
	}
	
	public ArgScriptStream<PollenMetadata> generateStream() {
		ArgScriptStream<PollenMetadata> stream = new ArgScriptStream<>();
		stream.setData(this);
		stream.addDefaultParsers();
		
		final ArgScriptArguments args = new ArgScriptArguments();
		
		stream.addParser("metadataVersion", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				metadataVersion = Optional.ofNullable(stream.parseInt(args, 0)).orElse(0);
			}
		}));
		
		stream.addParser("assetID", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				assetID = Optional.ofNullable(stream.parseLong(args, 0)).orElse(0L);
			}
		}));
		stream.addParser("assetKey", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				String[] originals = new String[3];
				assetKey.parse(args, 0, originals);
				line.addHyperlinkForArgument("key", originals, 0);
			}
		}));
		stream.addParser("parentAssetKey", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				String[] originals = new String[3];
				parentAssetKey.parse(args, 0, originals);
				line.addHyperlinkForArgument("key", originals, 0);
			}
		}));
		stream.addParser("parentAssetID", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				parentAssetID = Optional.ofNullable(stream.parseLong(args, 0)).orElse(0L);
			}
		}));
		stream.addParser("originalParentAssetID", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				originalParentAssetID = Optional.ofNullable(stream.parseLong(args, 0)).orElse(0L);
			}
		}));
		stream.addParser("originalParentAssetID", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				originalParentAssetID = Optional.ofNullable(stream.parseLong(args, 0)).orElse(0L);
			}
		}));
		stream.addParser("timeCreated", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				timeCreated = Optional.ofNullable(stream.parseLong(args, 0)).orElse(0L);
			}
		}));
		stream.addParser("timeDownloaded", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				timeDownloaded = Optional.ofNullable(stream.parseLong(args, 0)).orElse(0L);
			}
		}));
		
		stream.addParser("authorID", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				authorID = Optional.ofNullable(stream.parseLong(args, 0)).orElse(0L);
			}
		}));
		stream.addParser("authorName", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				authorName = insertSpecialCharacters(args.get(0));
			}
		}));
		stream.addParser("name", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				name = insertSpecialCharacters(args.get(0));
			}
		}));
		stream.addParser("description", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				description = insertSpecialCharacters(args.get(0));
			}
		}));
		
		stream.addParser("authorNameLocale", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				authorNameLocale = new ResourceID();
				authorNameLocale.parse(args, 0);
			}
		}));
		stream.addParser("nameLocale", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				nameLocale = new ResourceID();
				nameLocale.parse(args, 0);
			}
		}));
		stream.addParser("descriptionLocale", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				descriptionLocale = new ResourceID();
				descriptionLocale.parse(args, 0);
			}
		}));
		
		stream.addParser("authors", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 0, Integer.MAX_VALUE)) {
				for (int i = 0; i < args.size(); i++) {
					authors.add(insertSpecialCharacters(args.get(i)));
				}
			}
		}));
		
		stream.addParser("tagsLocale", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				tagsLocale = new ResourceID();
				tagsLocale.parse(args, 0);
			}
		}));
		stream.addParser("tags", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 0, Integer.MAX_VALUE)) {
				for (int i = 0; i < args.size(); i++) {
					tags.add(insertSpecialCharacters(args.get(i)));
				}
			}
		}));
		
		stream.addParser("isShareable", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 1)) {
				isShareable = Optional.ofNullable(stream.parseBoolean(args, 0)).orElse(false);
			}
		}));
		
		stream.addParser("consequenceTraits", ArgScriptParser.create((parser, line) -> {
			if (line.getArguments(args, 0, Integer.MAX_VALUE)) {
				for (int i = 0; i < args.size(); i++) {
					consequenceTraits.add(Optional.ofNullable(stream.parseFileID(args, i)).orElse(0));
				}
			}
		}));
		
		return stream;
	}
}
