package sporemodder.file.filestructures.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

class StructureFieldMetadata extends FieldMetadata {
	
	/** The metadata used to read the structure. */
	StructureMetadata<Object> metadata;

	@SuppressWarnings("unchecked")
	StructureFieldMetadata(Field field,  Class<?> type, Class<?> clazz, List<Annotation> annotations) throws Exception {
		super(field, clazz, annotations);
		
		metadata = (StructureMetadata<Object>) StructureMetadata.getStructureMetadata(type);
		
		if (metadata == null) {
			throw new Exception("Structure error: Unsupported type in field '" + field.getName()
					+ "'. Only numeric types, Strings, arrays, lists and other structures are supported.");
		}
	}

	@Override
	void read(Object structure, StreamReader in) throws Exception {
		Object value = field.get(structure);
		if (value == null) {
			throw new Exception("Unable to read structure field: The field '" + field.getName() + "' has not been initalized.");
		}
		metadata.read(value, in);
	}

	@Override
	void write(Object structure, StreamWriter out) throws Exception {
		Object value = field.get(structure);
		if (value == null) {
			throw new Exception("Unable to write structure field: The field '" + field.getName() + "' has not been initalized.");
		}
		metadata.write(value, out);
	}

}
