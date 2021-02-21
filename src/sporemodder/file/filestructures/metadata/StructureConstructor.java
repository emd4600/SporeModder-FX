package sporemodder.file.filestructures.metadata;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A class used to store a reference to the constructor of a structure class and call it when necessary.
 */
class StructureConstructor {
	/** A constructor that takes the parent structure as parameter, optional constructor. */
	private Constructor<?> advancedConstructor;
	/** The basic constructor. */
	private Constructor<?> basicConstructor;

	/**
	 * Creates a new instance that defines the constructor for a certain type contained in a structure.
	 * @param clazz The structure class that contains the field.
	 * @param fieldType The type of the field whose constructor will be used.
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	StructureConstructor(Class<?> clazz, Class<?> fieldType) throws NoSuchMethodException, SecurityException {
		
		// First try to get a constructor that takes the parent structure
		try {
			advancedConstructor = fieldType.getConstructor(clazz);
		} catch (NoSuchMethodException | SecurityException e) {

			// If not, get the default constructor
			advancedConstructor = null;
			basicConstructor = fieldType.getConstructor();
		}
	}
	
	/**
	 * Creates a new instance of the object with this constructor. It will try to use a constructor that
	 * takes the parent structure as argument and, if that does not exist, it will use the default constructor instead.
	 * @param parentStructure The parent structure that contains the object that is being created.
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	Object create(Object parentStructure) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (advancedConstructor != null) {
			return advancedConstructor.newInstance(parentStructure);
		}
		else {
			return basicConstructor.newInstance();
		}
	}
}
