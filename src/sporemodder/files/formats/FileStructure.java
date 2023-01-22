package sporemodder.files.formats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.files.InputStreamAccessor;

public abstract class FileStructure {
	private List<FileStructureError> errors = new ArrayList<FileStructureError>();
	
	protected static enum CompareFunc 
	{
		EQUAL {
			protected boolean compare(int a, int b) {
				return a == b;
			}
		}, 
		NOT_EQUAL {
			protected boolean compare(int a, int b) {
				return a != b;
			}
		}, 
		GREATER {
			protected boolean compare(int a, int b) {
				return a > b;
			}
		}, 
		GREATER_EQUAL {
			protected boolean compare(int a, int b) {
				return a >= b;
			}
		}, 
		LESS {
			protected boolean compare(int a, int b) {
				return a < b;
			}
		}, 
		LESS_EQUAL {
			protected boolean compare(int a, int b) {
				return a <= b;
			}
		};
		
		protected abstract boolean compare(int a, int b);
	};
	
	
	// Mind you, usually the position is passed after the value is read, so the error is just X bytes before it...
	public boolean expect(int value, int expectedValue, String errorCode, int position) {
		if (value != expectedValue) {
			errors.add(new FileStructureError(errorCode, position));
			return false;
		}
		return true;
	}
	
	public boolean expect(int value, int expectedValue, CompareFunc compareMethod, String errorCode, int position) {
		if (!compareMethod.compare(value, expectedValue)) {
			errors.add(new FileStructureError(errorCode, position));
			return false;
		}
		return true;
	}
	
	public boolean expect(int[] values, int[] expectedValues, String errorCode, int position) {
		if (values.length != expectedValues.length) 
		{
			errors.add(new FileStructureError(errorCode, position));
			return false;
		}
		for (int i = 0; i < values.length; i++) 
		{
			if (values[i] != expectedValues[i]) {
				errors.add(new FileStructureError(errorCode, position));
				return false;
			}
		}
		return true;
	}
	
	public boolean expect(int[] values, int[] expectedValues, CompareFunc compareMethod, String errorCode, int position) {
		if (values.length != expectedValues.length) 
		{
			errors.add(new FileStructureError(errorCode, position));
			return false;
		}
		for (int i = 0; i < values.length; i++) 
		{
			if (!compareMethod.compare(values[i], expectedValues[i])) {
				errors.add(new FileStructureError(errorCode, position));
				return false;
			}
		}
		return true;
	}

	//TODO is there any more efficient way to do this?
	public boolean expect(byte[] values, byte[] expectedValues, String errorCode, int position) {
		if (values.length != expectedValues.length) 
		{
			errors.add(new FileStructureError(errorCode, position));
			return false;
		}
		for (int i = 0; i < values.length; i++) 
		{
			if (values[i] != expectedValues[i]) {
				errors.add(new FileStructureError(errorCode, position));
				return false;
			}
		}
		return true;
	}
	
	public boolean expect(byte[] values, byte[] expectedValues, CompareFunc compareMethod, String errorCode, int position) {
		if (values.length != expectedValues.length) 
		{
			errors.add(new FileStructureError(errorCode, position));
			return false;
		}
		for (int i = 0; i < values.length; i++) 
		{
			if (!compareMethod.compare(values[i], expectedValues[i])) {
				errors.add(new FileStructureError(errorCode, position));
				return false;
			}
		}
		return true;
	}
	
	public boolean expectPadding(InputStreamAccessor in, int len, String errorCode, int position) throws IOException {
		byte[] values = new byte[len];
		in.read(values);
		for (int i = 0; i < len; i++) {
			if (values[i] != 0) {
				errors.add(new FileStructureError(errorCode, position));
				return false;
			}
		}
		return true;
	}
	
	public void addError(String errorCode, int position) {
		errors.add(new FileStructureError(errorCode, position));
	}

	public List<FileStructureError> getErrors() {
		return errors;
	}
	
	public void printErrors() {
		for (FileStructureError error : errors) {
			System.err.println(error.toString());
		}
	}
}
