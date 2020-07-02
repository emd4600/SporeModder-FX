package sporemodder.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;

import sporemodder.file.UnicodeBOMInputStream.BOM;

public class TextReader {

	private String text;
	private boolean isValidText;
	
	public TextReader() {
		
	}
	
	public String getText() {
		return text;
	}
	
	public boolean isValidText() {
		return isValidText;
	}
	
	public TextReader read(InputStream inputStream) throws NullPointerException, IOException {
		byte[] data = null;
		
		try (UnicodeBOMInputStream bomInput = new UnicodeBOMInputStream(inputStream);
	    		ByteArrayOutputStream result = new ByteArrayOutputStream()) 
	    {
	    	bomInput.skipBOM();
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = bomInput.read(buffer)) != -1) {
	            result.write(buffer, 0, length);
	        }
	        
	        data = result.toByteArray();
	        
	        BOM bom = bomInput.getBOM() == BOM.NONE ? BOM.UTF_8 : bomInput.getBOM();
	        text = bom.getCharset().newDecoder().decode(ByteBuffer.wrap(data)).toString();
	        isValidText = true;
	    }
		catch (CharacterCodingException e) {
			text = new String(data);
			isValidText = false;
		}
		
		return this;
	}
	
}
