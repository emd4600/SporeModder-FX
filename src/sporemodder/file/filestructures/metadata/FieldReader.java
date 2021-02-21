package sporemodder.file.filestructures.metadata;

import java.io.IOException;

import sporemodder.file.filestructures.StreamReader;

/**
 * A function that reads the value of a field.
 */
@FunctionalInterface
interface FieldReader {
	Object read(StreamReader in) throws IOException;
}
