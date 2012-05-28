package ca.phon.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Collection of helper methods for
 * working with the filesystem.
 *
 */
public class FileUtil {
	
	/**
	 * Copy a file from one location to another.  Can
	 * handle large files.
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static void copyFile(File in, File out) 
	    throws IOException
	{
	    FileChannel inChannel = new
	        FileInputStream(in).getChannel();
	    FileChannel outChannel = new
	        FileOutputStream(out).getChannel();
	    
	    long dataTransferred = 0;
	    long blockSize = 1024 * 1024; // 1MB
	    try {
	    	while(dataTransferred < inChannel.size()) {
	    		
	    		long dataLeft = inChannel.size() - dataTransferred;
	    		long toSend = 
	    			(dataLeft < blockSize ? dataLeft : blockSize);
	    		
		        dataTransferred += inChannel.transferTo(dataTransferred, toSend,
		                outChannel);
	    	}
	    	
	    	if(dataTransferred != inChannel.size()) {
	    		throw new IOException("Data transfer not complete.  Souce size:" +
	    				inChannel.size() + " bytes transferred: " + dataTransferred);
	    	}
	    } 
	    catch (IOException e) {
	        throw e;
	    }
	    finally {
	        if (inChannel != null) inChannel.close();
	        if (outChannel != null) outChannel.close();
	    }
	}
}
