package sporemodder.file.filestructures.metadata;

import java.io.IOException;

import sporemodder.file.filestructures.StreamWriter;

/**
 * A function that writes the value of a field into a stream.
 */
@FunctionalInterface
interface FieldWriter {
	void write(StreamWriter out, Object value) throws IOException;
}
