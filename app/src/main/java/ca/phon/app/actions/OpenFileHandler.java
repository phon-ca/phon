package ca.phon.app.actions;

import java.io.*;
import java.util.*;

/**
 * Service interface for open file handlers.
 */
public interface OpenFileHandler {
	
	/**
	 * 
	 * @return Set of supported file extensions (without '.')
	 */
	public Set<String> supportedExtensions();
	
	/**
	 * Can this handler open this file?
	 * 
	 * @param file
	 * @throws IOException
	 */
	public boolean canOpen(File file) throws IOException;
	
	/**
	 * Open the file in the appropriate editor/viewer.
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void openFile(File file) throws IOException;
	
}
