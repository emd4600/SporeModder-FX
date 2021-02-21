package sporemodder.file.filestructures.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.StructureEndian;

class ListFieldMetadata extends FieldMetadata {
	
	/** The attribute that controls the length of the array. It is necessary. */
	FieldLength lengthAttribute;
	
	/** The underlying metadata that is used to read the individual values of the array. */
	SimpleFieldMetadata valuesMetadata;
	
	/** The constructor used to create new objects of the values contained in the array. This is only used for non-basic types. */
	StructureConstructor valuesConstructor;
	/** The metadata used to read/write the values of the array, in case those are structures. */
	StructureMetadata<Object> valuesStructureMetadata;

	
	@SuppressWarnings("unlikely-arg-type")
	ListFieldMetadata(Field field, Class<?> clazz, List<Annotation> annotations, StructureEndian endian) throws Exception {
		super(field, clazz, annotations);
		
		// Get the length attribute
		for (Annotation annotation : annotations) {
			lengthAttribute = FieldLength.process(annotation, field, clazz, endian);
			if (lengthAttribute != null) {
				// Remove it from the list and stop searching
				annotations.remove(lengthAttribute);
				break;
			}
		}
		
		// Now error checking:
		
		// Has the user specified a length attribute?
		if (lengthAttribute == null) {
			throw new Exception("Structure error: A fixed-length attribute must be specified for the "
					+ "array field'" + field.getName() + "'.");
		}
		
		ParameterizedType parameter = (ParameterizedType) field.getGenericType();
		Class<?> listType = (Class<?>) parameter.getActualTypeArguments()[0];
		
		FieldMetadata metadata = StructureMetadata.getMetadata(
				field, listType, clazz, annotations, endian);
		
		if (metadata instanceof SimpleFieldMetadata) {
			valuesMetadata = (SimpleFieldMetadata) metadata;
		}
		else if (metadata instanceof StructureFieldMetadata) {
			valuesConstructor = new StructureConstructor(clazz, listType);
			valuesStructureMetadata = ((StructureFieldMetadata) metadata).metadata;
		}
		else {
			throw new Exception("Structure error: Invalid type for array '" + field.getName() + "'. Only numeric types, Strings and"
					+ " other structures are supported.");
		}
		
	}

	@Override
	void read(Object structure, StreamReader in) throws Exception {
		// 1. Get the length of the list
		int length = lengthAttribute.getLength(structure, in);
		
		// 2. Get the list object from the field
		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>) field.get(structure);
		
		// 3. Reset it to ensure there are no elements
		list.clear();
		
		// 4. Iterate and read all the items
		for (int i = 0; i < length; i++) {
			
			Object value = null;
			
			// 4.1 Is the field a structure?
			if (valuesStructureMetadata != null) {
				
				// We create a new instance of the structure and we read it using its metadata
				value = valuesConstructor.create(structure);
				valuesStructureMetadata.read(value, in);
			}
			else {
				// It's a basic type
				value = valuesMetadata.readValue(structure, in);
			}
			
			list.add(value);
		}
		
	}

	@Override
	void write(Object structure, StreamWriter out) throws Exception {
		// 1. Get the list object from the field
		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>) field.get(structure);
		
		// 2. Write the length if necessary
		lengthAttribute.writeLength(structure, out, list.size());
		
		// 3. Iterate and write all the items
		for (Object value : list) {
			
			// 3.1 Is the field a structure?
			if (valuesStructureMetadata != null) {
				valuesStructureMetadata.write(value, out);
			}
			else {
				// It's a basic type
				valuesMetadata.writeValue(structure, out, value);
			}
		}
	}

	/**
	 * Tells whether the given type is supported by this metadata class. Only List<> types
	 * are supported.
	 */
	static boolean isSupported(Class<?> clazz) {
		return List.class.isAssignableFrom(clazz);
	}
}
