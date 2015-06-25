/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.util.resources;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;

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
