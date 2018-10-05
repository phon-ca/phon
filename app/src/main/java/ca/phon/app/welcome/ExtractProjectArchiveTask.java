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
package ca.phon.app.welcome;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;

import ca.phon.app.workspace.Workspace;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogListener;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.Tuple;
import ca.phon.worker.PhonTask;

/**
 * Task to extract a .phon or .zip project archive
 * into the current workspace.
 * 
 * The property <code>PhonTask.PROGRESS_PROP</code>
 * can be watched extraction progress.
 */
public class ExtractProjectArchiveTask extends PhonTask {
	
	// check if we should upgrade the project after extraction
	private boolean checkForUpgrade = true;
	
	private File archiveFile;
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(ExtractProjectArchiveTask.class.getName());
	
	private enum ZippedProjectType {
		NOT_A_PROJECT,	// used when not project.xml is found
		PROJECT_BASE_INCLUDED,
		PROJECT_BASE_NOT_INCLUDED
	};
	
	private File destDir;
	
	// cache variable for zip detection
	private Tuple<ZippedProjectType, String> zipDetect = null;
	
	public ExtractProjectArchiveTask(File archive) {
		this.archiveFile = archive;
	}
	
	private Tuple<ZippedProjectType, String> getZipDetect() {
		Tuple<ZippedProjectType, String> retVal = zipDetect;
		
		if(retVal == null) {
			zipDetect = detectZippedProjectType(archiveFile);
			retVal = zipDetect;
		}
		
		return retVal;
	}
	
	public File getDestDir() {
		if(destDir == null) {
			File destBaseDir = Workspace.userWorkspaceFolder();
			
			/*
			 *  we will support two types of project structure:
			 *  
			 *  1) Project base included
			 *  	e.g., 
			 *  	English-Smith/
			 *  		project.xml
			 *  		Corpus1/
			 *  		Corpus2/
			 *  		etc.
			 *  
			 *  2) Project base not included
			 *  	e.g.,
			 *  	project.xml
			 *  	Corpus1/
			 *  	Corpus2/
			 *  	etc.
			 *  
			 *  In the later case, the name of the archive file
			 *  (without extension) is used as the new project 
			 *  folder name.
			 */
			Tuple<ZippedProjectType, String> zipDetect = getZipDetect();
			ZippedProjectType zippedProjectType = zipDetect.getObj1();
			
//			if(zippedProjectType == ZippedProjectType.NOT_A_PROJECT) {
//				IllegalArgumentException ex
//				 	= new IllegalArgumentException("'" + archiveFile.getAbsolutePath() + "' does not contain a Phon project.");
//				super.err = ex;
//				super.setStatus(TaskStatus.ERROR);
//				return;
			if(zippedProjectType != ZippedProjectType.NOT_A_PROJECT) {
				destDir = destBaseDir;
				if(zippedProjectType == ZippedProjectType.PROJECT_BASE_NOT_INCLUDED) {
					String archiveName = archiveFile.getName();
					int lastDotIdx = archiveName.lastIndexOf('.');
					String destName = 
						(lastDotIdx >= 0 ? archiveName.substring(0, lastDotIdx) : archiveName);
					destDir = new File(destBaseDir, destName);
				} else if(zippedProjectType == ZippedProjectType.PROJECT_BASE_INCLUDED) {
					destDir = new File(destBaseDir, zipDetect.getObj2());
				}
			}
			
			// increment name if necessary
			int fileNum = 1;
			String baseDestDir = destDir.getAbsolutePath();
			while(destDir.exists()) {
				String newDestDir = baseDestDir + "(" +  (fileNum++) + ")";
				destDir = new File(newDestDir);
			}
		}
		return destDir;
	}
	
	@Override
	public void performTask() {
		super.setStatus(TaskStatus.RUNNING);
		
		try {
			extractArchive(archiveFile, getDestDir());
			
			// check if archive is in phon workspace
			File archiveParent = archiveFile.getParentFile();
			File phonWorkspace = Workspace.userWorkspaceFolder();
			File backupLoc = new File(phonWorkspace, "backups");
			if(!backupLoc.exists()) {
				backupLoc.mkdirs();
			}
			if(phonWorkspace.getAbsolutePath().equals(archiveParent.getAbsolutePath())) {
				// move archive file to ${workspace}/backups/
				File backupFile = new File(backupLoc, archiveFile.getName());
				
				FileUtils.copyFile(archiveFile, backupFile);
				archiveFile.delete();
				
				NativeDialogs.showMessageDialog(CommonModuleFrame.getCurrentFrame(), 
						new NativeDialogListener() {
							
							@Override
							public void nativeDialogEvent(NativeDialogEvent event) {
								
							}
						}, null, "File moved", "Moved " + archiveFile.getName() + " to " + 
							backupFile.getParentFile().getAbsolutePath());
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			super.err = e;
			super.setStatus(TaskStatus.ERROR);
			return;
		}
		
		super.setStatus(TaskStatus.FINISHED);
	}
	
	/**
	 * Get zipped project type.
	 * 
	 * @param archive
	 * @return the detected archive type and the project root folder name
	 * in the zip file - only valid if detected type is ZippedProjectType.PROJECT_BASE_INCLUDED
	 */
	private Tuple<ZippedProjectType, String> detectZippedProjectType(File archive) {
		ZippedProjectType projType = ZippedProjectType.NOT_A_PROJECT;
		String projRoot = null;
		
		try(ZipFile zip = new ZipFile(archive)) {
			ZipEntry entry = null;
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while(entries.hasMoreElements()) {
				entry = entries.nextElement();
				
				if(entry.getName().contains("project.xml")) {
					if(entry.getName().equals("project.xml")) {
						projType = ZippedProjectType.PROJECT_BASE_NOT_INCLUDED;
						projRoot = "";
					} else {
						projType = ZippedProjectType.PROJECT_BASE_INCLUDED;
						
						File f = new File(entry.getName());
						File rootFile = f.getParentFile();
						projRoot = rootFile.getName();
					}
					break;
				}
			}
		} catch (IOException e) {
			LOGGER.warn(e.getMessage());
		}
		
		return new Tuple<ZippedProjectType, String>(projType, projRoot);
	}
	
	/**
	 * Extract zip file
	 * 
	 * @param archive
	 * @param outdir
	 */
	private final static int ZIP_BUFFER = 2048;
	/**
	 * Extract contents of a zip file to the destination directory.
	 */
	private void extractArchive(File archive, File destDir)
		throws IOException {

		BufferedOutputStream out = null;
		BufferedInputStream in = null;
		
		try(ZipFile zip = new ZipFile(archive)) {
			// create output directory if it does not exist
			if(destDir.exists() && !destDir.isDirectory()) {
				throw new IOException("'" + destDir.getAbsolutePath() + "' is not a directory.");
			}
	
			if(!destDir.exists()) {
				setProperty(STATUS_PROP, "Creating output directory");
				destDir.mkdirs();
			}
	
			Tuple<ZippedProjectType, String> zipDetect = getZipDetect();
			ZipEntry entry = null;
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while(entries.hasMoreElements()) {
				
				if(isShutdown()) {
					return;
				}
				
				entry = entries.nextElement();
				
				String entryName = entry.getName();
				File outFile = null;
				if(zipDetect.getObj1() == ZippedProjectType.PROJECT_BASE_INCLUDED) {
					// dest dir has already b
					outFile = new File(destDir, entryName.replaceFirst(zipDetect.getObj2(), ""));
				} else {
					outFile = new File(destDir, entryName);
				}
	
				if(entry.isDirectory()) {
					if(!outFile.exists())
						outFile.mkdirs();
				} else {
					LOGGER.info("Extracting: " + entry);
					setProperty(STATUS_PROP, "Extracting: " + entry);
	
					in = new BufferedInputStream(zip.getInputStream(entry));
					int count = -1;
					byte data[] = new byte[ZIP_BUFFER];
	
					if(outFile.exists()) {
						LOGGER.warn("Overwriting file '" + outFile.getAbsolutePath() + "'");
					}
	
					File parentFile = outFile.getParentFile();
	
					if(!parentFile.exists())
						parentFile.mkdirs();
	
					FileOutputStream fos = new FileOutputStream(outFile);
					out = new BufferedOutputStream(fos, ZIP_BUFFER);
					while((count = in.read(data, 0, ZIP_BUFFER)) != -1) {
						out.write(data, 0, count);
					}
					out.flush();
					out.close();
					in.close();
				}
			}
		} catch (IOException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
			throw e;
		}
		
		setProperty(STATUS_PROP, "Finished");
	}
	
}
