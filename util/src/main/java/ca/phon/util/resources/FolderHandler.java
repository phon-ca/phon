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
