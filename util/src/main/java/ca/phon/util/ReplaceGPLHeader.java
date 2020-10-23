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
import java.nio.*;


/**
 * Utility class to help replace the first comment block
 * in java files for a directory.
 * 
 */
public class ReplaceGPLHeader {
	
	private static StringBuffer headerContents = new StringBuffer();
	
	public static void main(String[] args) {
		if(args.length != 1) {
			System.err.println("Need a directory.");
			return;
		}
		
		String scanDir = args[0];
		
		// recursively scan directory, replacing block comment header
		// at top of all java files
		readNewHeader();
		replaceHeaders(new File(scanDir));
	}
	
	private static void readNewHeader() {
		try {
			File headerFile = new File("data/gplheader.txt");
			BufferedReader in = new BufferedReader(
					new InputStreamReader(new FileInputStream(headerFile), "UTF-8"));
			CharBuffer buf = CharBuffer.allocate((int)headerFile.length());
			in.read(buf);
			in.close();
			
			String s = new String(buf.array());
			headerContents = new StringBuffer(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void replaceHeaders(File dirFile) {
		// get a file listing of the directory
//		File dirFile = new File(dir);
		if(!dirFile.isDirectory()) return; // bail if not a directory
		
		File[] dirContents = dirFile.listFiles();
		for(File f:dirContents) {
			if(f.isDirectory())
				replaceHeaders(f);
			else {
				if(f.getName().endsWith(".java")) {
					replaceHeader(f);
				}
			}
		}
	}
	
	public static void replaceHeader(File f) {
		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(new FileInputStream(f), "UTF-8"));
			
			char[] cBuff = new char[256];
			StringBuffer sBuff = new StringBuffer();
			long readBytes = 0;
			int lastReadBytes = -1;
//			String line = null;
			while((lastReadBytes = in.read(cBuff)) != -1) {
				sBuff.append(cBuff, 0, lastReadBytes);
//				readBytes++;
				readBytes += lastReadBytes;
			}
			in.close();
			
			if(readBytes != f.length()) {
				System.err.println("Warning read " + readBytes + " bytes but file is " + f.length() + " bytes.");
//				in.close();
//				return;
			}
//			in.close();
			
//			String s = new String(buffer.array());
//			StringBuffer sBuff = new StringBuffer(s);
			
			// get the first index of /* - it should be 0
			int commentStart = sBuff.indexOf("/*");
			if(commentStart != 0) {
				System.err.println("Skipping: " + f + " - invalid header block.");
				return;
			}
			
			// get the first index of */\n
			String searchString = "*/";
			int commentEnd = sBuff.indexOf(searchString);
			
			int replaceEnd = commentEnd + searchString.length() + 1;
			sBuff.replace(commentStart, replaceEnd, headerContents.toString());
			
			// write file back to disk
			BufferedWriter out = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
			out.write(sBuff.toString());
			out.flush();
			out.close();
			
			System.out.println("Replaced header on file: " + f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
