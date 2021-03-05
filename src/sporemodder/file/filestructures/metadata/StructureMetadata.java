package sporemodder.file.filestructures.metadata;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.Structure;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.StructureFieldMethod;
import sporemodder.file.filestructures.StructureIgnore;

public class StructureMetadata<T extends Object> {
	
	/** The superclass that will be read/written before the object structure itself. */
	private StructureMetadata<Object> superclassMetadata;
	/** All the fields that need to be read/written in the structure. */
	private final List<FieldMetadata> fields = new ArrayList<FieldMetadata>();
	
	// We don't want it to be used
	private StructureMetadata() {};
	
	/**
	 * Reads an object with this structure from the data stream provided.
	 * @param structure The object that will receive the values that are read.
	 * @param in The data stream reader.
	 * @throws IOException If there's an error while reading the file.
	 */
	public void read(T structure, StreamReader in) throws IOException {
		try {
			// First read superclass if present
			if (superclassMetadata != null) {
				superclassMetadata.read(structure, in);
			}
			
			for (FieldMetadata field : fields) {
				if (field.conditionAttribute.evaluate(structure, in)) {
					field.read(structure, in);
				}
			}
		}
		catch (Exception e) {
			if (e instanceof IOException) {
				throw (IOException) e;
			}
			else {
				throw new IOException("Error while reading structure.", e);
			}
		}
	}
	
	
	public void write(T structure, StreamWriter out) throws IOException {
		try {
			// First read superclass if present
			if (superclassMetadata != null) {
				superclassMetadata.write(structure, out);
			}
			
			for (FieldMetadata field : fields) {
				if (field.conditionAttribute.evaluate(structure, out)) {
					field.write(structure, out);
				}
			}
		}
		catch (Exception e) {
			if (e instanceof IOException) {
				throw (IOException) e;
			}
			else {
				throw new IOException("Error while writing structure.", e);
			}
		}
	}
	
	
//	/**
//	 * Prints all the structure values (recognised in this metadata) of the given object.
//	 * @param structure The object whose values will be printed.
//	 */
//	public void print(T structure) {
//	//superclassMetadata?
//		try {
//			print(structure, "");
//		} catch (Exception e) {
//			throw new IllegalArgumentException(e);
//		}
//	}
//	
//	private void print(T structure, String indentLevel) throws Exception {
//		for (FieldMetadata field : fields) {
//			// First check if it's a structure
//			@SuppressWarnings("unchecked")
//			StructureMetadata<Object> metadata = (StructureMetadata<Object>) getMetadata(field.field.getType());
//			
//			if (metadata != null) {
//				// It's an structure, use the structure print with increased indentation.
//				System.out.println(indentLevel + field.field.getName() + ":");
//				metadata.print(field.field.get(structure), indentLevel + "\t");
//			}
//			else {
//				// Just print the value
//				System.out.println(indentLevel + field.field.getName() + ":\t" + field.field.get(structure).toString());
//			}
//		}
//	}
	
	
	/** 
	 * Returns the STRUCTURE_METADATA field of the given class, only if the class uses the {@link Structure} annotation.
	 * If the class does use it but it does not have a field, it throws an exception.
	 * @param clazz
	 * @return
	 * @throws Exception If the class given is a structure but does not have a field called 'STRUCTURE_METADATA'.
	 */
	static StructureMetadata<?> getStructureMetadata(Class<?> clazz) throws Exception {
		
		if (clazz.getAnnotation(Structure.class) != null) {
			try {
				return (StructureMetadata<?>) clazz.getDeclaredField("STRUCTURE_METADATA").get(null);
			} catch (SecurityException | NoSuchFieldException e) {
				throw new Exception("The class '" + clazz.getName() + "' does not have STRUCTURE_METADATA or it is not public.");
			}
		}
		else {
			return null;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private void generateInternal(Class<T> structureType) throws Exception {
		Structure structInfo = structureType.getAnnotation(Structure.class);
		if (structInfo == null) {
			throw new Exception("Unsupported class for structure metadata: the @Structure annotation is missing.");
		}
		
		StructureEndian endian = structInfo.value();
		
		// First, check if there's a superclass and get its structure first.
		Class<?> superclass = structureType.getSuperclass();
		if (superclass != null) {
			superclassMetadata = (StructureMetadata<Object>) StructureMetadata.getStructureMetadata(superclass);
		}
		
		// Iterate through all the declared fields (this does not include inherited fields)
		for (Field field : structureType.getDeclaredFields()) {
			// Ignore static fields and fields with 'StructureIgnore' annotation
			if (Modifier.isStatic(field.getModifiers()) ||
					field.getAnnotation(StructureIgnore.class) != null) {
				continue;
			}
			
			// Make the field accessible even if it is not public
			field.setAccessible(true);
			
			List<Annotation> annotations = new ArrayList<Annotation>(Arrays.asList(field.getAnnotations()));
			fields.add(StructureMetadata.getMetadata(
					field, field.getType(), structureType, annotations, endian));
		}
	}
	
	public static <T> StructureMetadata<T> generate(Class<T> structureType) {
		try {
			
			StructureMetadata<T> metadata = new StructureMetadata<T>();
			
			metadata.generateInternal(structureType);
			
			return metadata;
			
		} 
		catch (Exception e) {
			// We wrap it into an illegal argument exception because this does not require any change when 
			// declaring the field.
			throw new IllegalArgumentException(e);
		}
	}
	
	
	/**
	 * Returns a new instance of the field metadata that corresponds to the given type.
	 * @param field The field whose metadata will be generated.
	 * @param type The type of the field.
	 * @param clazz The class of the structure that contains the child.
	 * @param annotations The annotations that modify the field.
	 * @param endian The endian order used by the structure.
	 * @return
	 * @throws Exception 
	 */
	static FieldMetadata getMetadata(Field field, Class<?> type, Class<?> clazz, List<Annotation> annotations, StructureEndian endian) throws Exception {
		
		for (Annotation annotation : annotations) {
			if (annotation instanceof StructureFieldMethod) {
				return new CustomFieldMetadata((StructureFieldMethod) annotation, field, clazz, annotations);
			}
		}
		
		if (ArrayFieldMetadata.isSupported(type)) {
			return new ArrayFieldMetadata(field, clazz, annotations, endian);
		}
		else if (ListFieldMetadata.isSupported(type)) {
			return new ListFieldMetadata(field, clazz, annotations, endian);
		}
		else if (NumericFieldMetadata.isSupported(type)) {
			return new NumericFieldMetadata(field, type, clazz, annotations, endian);
		}
		else if (BooleanFieldMetadata.isSupported(type)) {
			return new BooleanFieldMetadata(field, clazz, annotations);
		}
		else if (StringFieldMetadata.isSupported(type)) {
			return new StringFieldMetadata(field, clazz, annotations, endian);
		}
		else {
			// Try an structure. It will throw an error if it is not a structure.
			return new StructureFieldMetadata(field, type, clazz, annotations);
		}
	}
}
