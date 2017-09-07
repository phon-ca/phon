/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.util;

import java.io.*;
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
