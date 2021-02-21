package sporemodder.file.filestructures.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.StructureEndian;
import sporemodder.file.filestructures.StructureLength;

class ArrayFieldMetadata extends FieldMetadata {
	
	@FunctionalInterface
	private static interface ArrayGetter {
		Object get(Object array, int index);
	}
	
	@FunctionalInterface
	private static interface ArraySetter {
		void set(Object array, int index, Object value);
	}
	
	/** The underlying metadata that is used to read the individual values of the array. */
	SimpleFieldMetadata valuesMetadata;
	
	/** The constructor used to create new objects of the values contained in the array. This is only used for non-basic types. */
	StructureConstructor valuesConstructor;
	/** The metadata used to read/write the values of the array, in case those are structures. */
	StructureMetadata<Object> valuesStructureMetadata;
	
	/** The function used to get a value from the array. We use a different one for each type as arrays can't
	 *  be casted. */
	ArrayGetter getter;
	/** The function used to set a value to the array. We use a different one for each type as arrays can't
	 *  be casted. */
	ArraySetter setter;
	
	/**
	 * Generates the metadata for a fixed-length array variable. This means that the array must be 
	 * initialized and created before reading/writing it, and it can (and must) only use the 
	 * {@link StructureLength.Fixed} annotation to specify the length of the array.
	 * @param field The field that is being modified.
	 * @param clazz The structure class that contains the field.
	 * @param annotations The annotations that modify the field.
	 * @param endian The endian order of the structure.
	 * @throws Exception
	 */
	ArrayFieldMetadata(Field field, Class<?> clazz, List<Annotation> annotations, StructureEndian endian) throws Exception {
		super(field, clazz, annotations);
		
		Class<?> arrayType = field.getType().getComponentType();
		
		FieldMetadata metadata = StructureMetadata.getMetadata(field, arrayType, clazz, annotations, endian);
		
		if (metadata instanceof SimpleFieldMetadata) {
			valuesMetadata = (SimpleFieldMetadata) metadata;
			generateMethods(arrayType);
		}
		else if (metadata instanceof StructureFieldMetadata) {
			valuesConstructor = new StructureConstructor(clazz, arrayType);
			valuesStructureMetadata = ((StructureFieldMetadata) metadata).metadata;
		}
		else {
			throw new Exception("Structure error: Invalid type for array '" + field.getName() + "'. Only numeric types, Strings and"
					+ " other structures are supported.");
		}
	}

	private void generateMethods(Class<?> arrayType) {
		
		if (NumericFieldMetadata.isByte(arrayType)) {
			getter = (array, index) -> ((byte[]) array)[index];
			setter = (array, index, value) -> {((byte[]) array)[index] = ((Number)value).byteValue();};
		}
		else if (NumericFieldMetadata.isShort(arrayType)) {
			getter = (array, index) -> ((short[]) array)[index];
			setter = (array, index, value) -> {((short[]) array)[index] = ((Number)value).shortValue();};
		}
		else if (NumericFieldMetadata.isInt(arrayType)) {
			getter = (array, index) -> ((int[]) array)[index];
			setter = (array, index, value) -> {((int[]) array)[index] = ((Number)value).intValue();};
		}
		else if (NumericFieldMetadata.isLong(arrayType)) {
			getter = (array, index) -> ((long[]) array)[index];
			setter = (array, index, value) -> {((long[]) array)[index] = ((Number)value).longValue();};
		}
		else if (NumericFieldMetadata.isFloat(arrayType)) {
			getter = (array, index) -> ((float[]) array)[index];
			setter = (array, index, value) -> {((float[]) array)[index] = ((Number)value).floatValue();};
		}
		else if (NumericFieldMetadata.isDouble(arrayType)) {
			getter = (array, index) -> ((double[]) array)[index];
			setter = (array, index, value) -> {((double[]) array)[index] = ((Number)value).doubleValue();};
		}
		else {
			getter = (array, index) -> ((Object[]) array)[index];
			setter = (array, index, value) -> {((Object[]) array)[index] = (Object)value;};
		}
	}

	@Override
	void read(Object structure, StreamReader in) throws Exception {
		// 1. Get the array object from the field
		Object array = field.get(structure);
		
		// 2. Get the length of the array
		int length = Array.getLength(array);
		
		// 3. Iterate through all the items
		for (int i = 0; i < length; i++) {
			
			// 3.1 Is it a structure type?
			if (valuesStructureMetadata != null) {
				
				// Create a new instance and read it using its metadata
				Object value = valuesConstructor.create(structure);
				valuesStructureMetadata.read(value, in);
				((Object[]) array)[i] = value;
			}
			else {
				// It's a basic type, so just read the value
				setter.set(array, i, valuesMetadata.readValue(structure, in));
			}
		}
	}


	@Override
	void write(Object structure, StreamWriter out) throws Exception {
		// 1. Get the array object from the field
		Object array = field.get(structure);
		
		// 2. Get the length of the array
		int length = Array.getLength(array);
		
		// 3. Iterate through all the items
		for (int i = 0; i < length; i++) {
			// 3.1 Is it a structure type?
			if (valuesStructureMetadata != null) {
				valuesStructureMetadata.write(((Object[]) array)[i], out);
			}
			else {
				valuesMetadata.writeValue(structure, out, getter.get(array, i));
			}
		}
	}
	
	
	/**
	 * Tells whether the given type is supported by this metadata class. Only array types
	 * are supported.
	 */
	static boolean isSupported(Class<?> clazz) {
		return clazz.isArray();
	}
}
