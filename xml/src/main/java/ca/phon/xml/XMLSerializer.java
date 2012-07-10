package ca.phon.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * XML Serializer.
 * 
 * 
 */
public interface XMLSerializer {
	
	/**
	 * Read an object of type {@link #declaredType()} from
	 * the given {@link InputStream}.
	 * 
	 * @param input
	 * @return the read object
	 * 
	 * @throws IOException if an error occurs during
	 *  read
	 */
	public <T> T read(Class<T> type, InputStream input) throws IOException;
	
	/**
	 * Write an object of type {@link #declaredType()} to
	 * the given {@link OutputStream} as xml data.
	 * 
	 * @param output
	 * 
	 * @throws IOException if an error occurs during
	 *  write
	 */
	public <T> void write(Class<T> type, T obj, OutputStream output) throws IOException;
	
	/**
	 * The type handled by this serializer.
	 * 
	 * @return
	 */
	public Class<?> declaredType();

}
