/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
