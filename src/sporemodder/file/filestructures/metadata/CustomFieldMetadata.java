package sporemodder.file.filestructures.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.StructureFieldMethod;

/**
 * A field that is read/written using a custom method specified by the user.
 */
class CustomFieldMetadata extends FieldMetadata {
	
	/** A method specified by the user used to read the field. */
	Method readMethod;
	/** A method specified by the user used to write the field. */
	Method writeMethod;

	CustomFieldMetadata(StructureFieldMethod annotation, Field field, Class<?> clazz, List<Annotation> annotations) throws Exception {
		super(field, clazz, annotations);

		this.readMethod = clazz.getDeclaredMethod(annotation.read(), String.class, StreamReader.class);
		this.writeMethod = clazz.getDeclaredMethod(annotation.write(), String.class, StreamWriter.class, Object.class);
		
		// Make them accessible if they are not public
		readMethod.setAccessible(true);
		writeMethod.setAccessible(true);
	}

	@Override
	void read(Object structure, StreamReader in) throws Exception {
		readMethod.invoke(structure, field.getName(), in);
	}

	@Override
	void write(Object structure, StreamWriter out) throws Exception {
		writeMethod.invoke(structure, field.getName(), out, field.get(structure));
	}

}
