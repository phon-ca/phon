/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.util.resources;

import java.io.*;
import java.util.*;

/**
 * Scans a folder for files.  Scanning can be recursive and
 * use a filter.
 * 
 * 
 */
public abstract class FolderHandler<T> extends FileHandler<T> {

	/**
	 * Folder to scan
	 */
	private final File folder;
	
	/**
	 * Recursive?
	 */
	private boolean recursive = false;
	
	/**
	 * Filter
	 */
	private FileFilter fileFilter;
	
	/**
	 * Constructor
	 * 
	 * @param folder the folder to scan
	 */
	public FolderHandler(File folder) {
		this.folder = folder;
	}
	
	/**
	 * Is scanning recursive
	 * 
	 * @return boolean
	 */
	public boolean getRecursive() {
		return this.recursive;
	}
	
	/**
	 * Set recursive scanning
	 * 
	 * @param recursive
	 */
	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}
	
	/**
	 * Get the current file filter
	 * 
	 * @return FileFilter
	 */
	public FileFilter getFileFilter() {
		return this.fileFilter;
	}
	
	/**
	 * Set file filter
	 * 
	 * @param filter
	 */
	public void setFileFilter(FileFilter filter) {
		this.fileFilter = filter;
	}
	
	/**
	 * Scan folder for files.
	 */
	private void scanFolder() {
		if(!folder.exists() || !folder.isDirectory()) return;
		if(!getRecursive()) {
			File[] fileList = new File[0];
			if(getFileFilter() != null) {
				fileList = folder.listFiles(getFileFilter());
			} else {
				fileList = folder.listFiles();
			}
			
			for(File f:fileList) addFile(f);
		} else {
			scanFolderRecursive(folder);
		}
	}

	/**
	 * Recursively scans the given folder for files.
	 * 
	 * @param f
	 */
	private void scanFolderRecursive(File f) {
		if(f.isDirectory()) {
			File[] allFiles = f.listFiles();
			
			for(File currentFile:allFiles) {
				// check for a match first - directories could be
				// accepted as a match
				if(getFileFilter() != null) {
					if(getFileFilter().accept(currentFile)) {
						addFile(currentFile);
					} else {
						if(currentFile.isDirectory()) {
							scanFolderRecursive(currentFile);
						}
					}
				} else {
					if(currentFile.isFile()) {
						addFile(currentFile);
					} else if(currentFile.isDirectory()) {
						scanFolderRecursive(currentFile);
					}
				}
			}
		}
	}
	
	@Override
	public Iterator<T> iterator() {
		super.getFiles().clear();
		scanFolder();
		return super.iterator();
	}
}
