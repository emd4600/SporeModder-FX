package sporemodder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import sporemodder.file.Converter;
import sporemodder.file.ResourceKey;
import sporemodder.file.dbpf.DBPFPackingTask;
import sporemodder.file.dbpf.DBPFUnpackingTask;
import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.MemoryStream;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.simulator.SimulatorClass;
import sporemodder.file.spui.SporeUserInterface;
import sporemodder.util.NameRegistry;
import sporemodder.util.Project;

public class Launcher {
	
	public static final String VERSION = Optional.ofNullable(Launcher.class.getPackage().getImplementationVersion()).orElse("1.0.0");

	public static void main(String[] args) throws InterruptedException {
		if (args.length == 0) {
			MainApp.main(args);
		}
		else {
			MainApp.testInit();
			int exitCode = new CommandLine(new SMFXCommand()).execute(args);
	        System.exit(exitCode);
		}
	}
	
	public static class IdConverter implements ITypeConverter<Integer> {
	    @Override public Integer convert(String value) throws Exception {
	    	return HashManager.get().int32(value);
	    }
	}

	private static class SMFXVersionProvider implements CommandLine.IVersionProvider {
		@Override
		public String[] getVersion() throws Exception {
			return new String[] { VERSION };
		}
	}
	
	@Command(name = "sporemodderfx", mixinStandardHelpOptions = true, versionProvider = SMFXVersionProvider.class,
			subcommands = {
					NameToIdCommand.class, 
					IdToNameCommand.class, 
					DecodeCommand.class,
					EncodeCommand.class,
					UnpackCommand.class,
					PackCommand.class,
					FindSpuiForControlIdCommand.class,
					ScanSimulatorCommand.class
			})
	public static class SMFXCommand implements Callable<Integer> {

		@Override
		public Integer call() {
			return 0;
		}
	}
	
	@Command(name = "name-to-id", description = "Calculates the hexadecimal ID of a name", mixinStandardHelpOptions = true)
	public static class NameToIdCommand implements Callable<Integer> {
		public static enum RegistryType {
			file,
			property,
			type,
			simulator,
			fnv,
			all
		}
		
		@Parameters(index = "0", description = "The string to calculate its ID.")
		private String name;
		
		@Option(names = {"--registry"}, description = "The registry to look for the hash: [all, fnv, file, property, type, simulator]. Default is 'all', which finds the first name that matches in any registry.", defaultValue = "all")
		private RegistryType registry = RegistryType.all;

		@Override
		public Integer call() {
			HashManager hasher = HashManager.get();
			int id;
			switch (registry) {
			case file:
				id = hasher.getFileHash(name);
				break;
			case type:
				id = hasher.getTypeHash(name);
				break;
			case property:
				id = hasher.getPropHash(name);
				break;
			case simulator:
				id = hasher.getSimulatorHash(name);
				break;
			case fnv:
				id = hasher.fnvHash(name);
				break;
			case all:
			default:
				if (name.endsWith("~")) {
					// Special case, only file and project registry available
					NameRegistry reg = hasher.getFileRegistry();
					Integer value = hasher.getFileRegistry().getHash(name);
					if (value == null) {
						reg = hasher.getProjectRegistry();
						value = reg.getHash(name);
					}
					if (value == null) {
						System.err.println("Alias (using ~) not found in file registry");
						return -1;
					} else {
						id = value;
					}
				}
				else {
					Integer value;
					value = hasher.getTypeRegistry().getHash(name);
					if (value == null) {
						value = hasher.getSimulatorRegistry().getHash(name);
						if (value == null) {
							value = hasher.getPropRegistry().getHash(name);
							if (value == null) {
								value = hasher.getFileRegistry().getHash(name);
								if (value == null) {
									value = hasher.fnvHash(name);
								}
							}
						}
					}
					id = value;
				}
				break;
			}
			System.out.println(hasher.hexToString(id));
			return 0;
		}
	}
	
	@Command(name = "id-to-name", description = "Searches the registries to find the name corresponding to the given hexadecimal ID", mixinStandardHelpOptions = true)
	public static class IdToNameCommand implements Callable<Integer> {
		public static enum RegistryType {
			file,
			property,
			type,
			simulator,
			all
		}
		
		@Parameters(index = "0", description = "The ID to calculate its name.", converter = IdConverter.class)
		private int id;
		
		@Option(names = {"--registry"}, description = "The registry twhere names are checked: [all, file, property, type, simulator]. Default is 'all'.", defaultValue = "all")
		private RegistryType registry = RegistryType.all;

		@Override
		public Integer call() {
			HashManager hasher = HashManager.get();
			String name;
			switch (registry) {
			case file:
				name = hasher.getFileName(id);
				break;
			case type:
				name = hasher.getTypeName(id);
				break;
			case property:
				name = hasher.getPropName(id);
				break;
			case simulator:
				name = hasher.getSimulatorName(id);
				break;
			case all:
			default:
				name = hasher.getTypeRegistry().getName(id);
				if (name == null) {
					name = hasher.getSimulatorRegistry().getName(id);
					if (name == null) {
						name = hasher.getPropRegistry().getName(id);
						if (name == null) {
							name = hasher.getFileRegistry().getName(id);
						}
					}
				}
				break;
			}
			System.out.println(name);
			return 0;
		}
	}
	
	@Command(name = "decode", description = "Decodes Spore files into usable formats.", mixinStandardHelpOptions = true)
	public static class DecodeCommand implements Callable<Integer> {
		
		@Parameters(index = "0", description = "The input file to decode.")
		private File input;
		
		@Option(names = {"--output-name"}, description = "Name of the output file, if not specified the same file name is used")
		private String outputName;

		@Override
		public Integer call() throws Exception {
			String name;
			input = input.getAbsoluteFile();
			if (input.getParentFile().getName() != null) {
				name = input.getParentFile().getName() + "!" + input.getName();
			} else {
				name = input.getName();
			}
			HashManager.get().setUpdateProjectRegistry(true);
			ResourceKey key = new ResourceKey();
			key.parse(name);
			
			Converter converter = FormatManager.get().getDecoder(key);
			if (converter == null) {
				System.err.println("The input file is not in any recognizable format");
				return -1;
			}
			else {
				if (outputName != null) {
					HashManager.get().getProjectRegistry().add(outputName, HashManager.get().fnvHash(outputName));
				}
				try (StreamReader stream = new FileStream(input, "r")) {
					if (!converter.decode(stream, input.getParentFile(), key)) {
						System.err.println("Trying to convert format '" + converter.getName() + "' failed.");
						return -1;
					}
					else {
						System.out.println("Successfully converted format '" + converter.getName() + "'.");
					}
					return 0;
				}
			}
		}
	}
	
	@Command(name = "encode", description = "Encodes files into Spore formats.", mixinStandardHelpOptions = true)
	public static class EncodeCommand implements Callable<Integer> {

		@Parameters(index = "0", description = "The input file to encode.")
		private File input;
		
		@Parameters(index = "1", description = "The output file.")
		private File output;
		
		@Override
		public Integer call() throws Exception {
			input = input.getAbsoluteFile();
			output = output.getAbsoluteFile();
			
			Converter converter = FormatManager.get().getEncoder(input);
			if (converter == null) {
				System.err.println("The input file is not in any recognizable format");
				return -1;
			}
			else {
				try (StreamWriter stream = new FileStream(output, "rw")) {
					if (!converter.encode(input, stream)) {
						System.err.println("Trying to convert format '" + converter.getName() + "' failed.");
						return -1;
					}
					else {
						System.out.println("Successfully converted format '" + converter.getName() + "'.");
					}
					return 0;
				}
			}
		}
	}
	
	private static long startTime = 0;
	private static final int MAX_PROGRESS_CHARS = 100;
	private static Consumer<Double> PROGRESS_BAR_LISTENER = progress -> {
		try {
			String text = "\r|";
			int progressChars;
			if (progress == 1.0) {
				progressChars = MAX_PROGRESS_CHARS;
				text += "=".repeat(progressChars);
			} else {
				progressChars = (int)Math.floor((MAX_PROGRESS_CHARS * progress));
				progressChars = Math.min(progressChars, MAX_PROGRESS_CHARS-1);
				text += "=".repeat(progressChars) + ">" + " ".repeat(MAX_PROGRESS_CHARS - progressChars + 1);
			}
			text += "|   ";
			
			long elapsedTime = System.currentTimeMillis() - startTime;
			long seconds = (elapsedTime / 1000);
			long minutes = seconds / 60;
			long seconds60 = seconds % 60;
			
			text += String.format("%02d:%02d", minutes, seconds60);
			
			System.out.write(text.getBytes());
		} catch (IOException e1) {
		}
	};
	
	@Command(name = "unpack", description = "Unpack the contents of a Spore DBPF package.", mixinStandardHelpOptions = true)
	public static class UnpackCommand implements Callable<Integer> {

		@Parameters(index = "0", description = "The input DBPF file to unpack.")
		private File input;
		
		@Parameters(index = "1", description = "The output folder where the contents will be unpacked. The folder must be empty.")
		private File output;
		
		@Option(names = {"--converters"}, description = "List of converters class names to use, such as 'PropConverter'. If not specified, the default converters will be used.")
		private final List<String> converters = new ArrayList<>();
		
		@Override
		public Integer call() throws Exception {
			input = input.getAbsoluteFile();
			output = output.getAbsoluteFile();
			
			List<Converter> convertersList = new ArrayList<>();
			for (Converter converter : FormatManager.get().getConverters()) {
				if (converters.isEmpty()) {
					if (converters.contains(converter.getClass().getName())) {
						convertersList.add(converter);
					}
				}
				else if (converter.isEnabledByDefault()) {
					convertersList.add(converter);
				}
			}
			
			
			if (!output.exists()) {
				output.mkdirs();
			}
			startTime = System.currentTimeMillis();
			final DBPFUnpackingTask task = new DBPFUnpackingTask(input, output, null, convertersList);
			task.setNoJavaFX();
			task.setNoJavaFXProgressListener(PROGRESS_BAR_LISTENER);
			
			System.out.println("Unpacking " + input.getName() + " ...");
			System.out.write(("|" + " ".repeat(MAX_PROGRESS_CHARS) + "|\r").getBytes());
			Exception e = task.call();
			System.out.println();
			if (e != null) {
				e.printStackTrace();
				return -1;
			}
			else {
				return 0;
			}
		}
	}
	
	@Command(name = "pack", description = "Pack the contents of a folder into a Spore DBPF package.", mixinStandardHelpOptions = true)
	public static class PackCommand implements Callable<Integer> {

		@Parameters(index = "0", description = "The input folder to pack.")
		private File input;

		@Parameters(index = "1", description = "The output DBPF file to generate. If the output is a folder, the input must be a project, and the output DBPF name will be taken from the project settings.")
		private File output;

		@Option(names = {"--compress"}, description = "[Experimental] Compress files bigger than N bytes")
		private int compressThreshold = -1;
		
		@Override
		public Integer call() throws Exception {
			input = input.getAbsoluteFile();
			output = output.getAbsoluteFile();

			if (!input.exists() || !input.isDirectory()) {
				System.err.println("Cannot pack: input directory '" + input.getPath() + "' does not exist");
				return 1;
			}

			Project project = new Project(input.getName(), input, null);
			project.loadSettings();

			if (output.isDirectory()) {
				output = new File(output, project.getPackageName());
			}
			
			startTime = System.currentTimeMillis();
			final DBPFPackingTask task = new DBPFPackingTask(project, output);
			task.setCompressThreshold(compressThreshold);
			task.setNoJavaFX();
			task.setNoJavaFXProgressListener(PROGRESS_BAR_LISTENER);
			
			System.out.println("Packing " + input.getName() + " ...");
			System.out.write(("|" + " ".repeat(MAX_PROGRESS_CHARS) + "|\r").getBytes());
			task.call();
			System.out.println();
			task.call();
			
			return 0;
		}
	}

	@Command(name = "find-spui", description = "Find all SPUIs that have a specific control ID", mixinStandardHelpOptions = true)
	public static class FindSpuiForControlIdCommand implements Callable<Integer> {
		@Parameters(index = "0", description = "The program will look all .spui files in this folder and subfolders")
		private File inputFolder;

		@Parameters(index = "1", description = "The control ID to find in SPUIs", converter = IdConverter.class)
		private int controlId;

		@Override
		public Integer call() throws Exception {
			SporeUserInterface.findSpuisWithControlId(inputFolder, controlId, true);

			return 0;
		}
	}

	@Command(name = "scan-simulator", description = "Scan file offsets of attributes in simulator data files", mixinStandardHelpOptions = true)
	public static class ScanSimulatorCommand implements Callable<Integer> {
		@Parameters(index = "0", description = "Input simulator data file")
		private File inputFile;

		@Override
		public Integer call() throws Exception {
			try (StreamReader stream = new MemoryStream(Files.readAllBytes(inputFile.toPath()))) {
				SimulatorClass.scanClasses(stream);
			}

			return 0;
		}
	}
}
