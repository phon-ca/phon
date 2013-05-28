/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.app.workspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.NativeDialogListener;
import ca.phon.worker.PhonTask;

/** 
 * Task to archive projects.
 */
public class ProjectArchiveTask extends PhonTask {
	
	private final static Logger LOGGER = Logger.getLogger(ProjectArchiveTask.class.getName());

	private Project project;
	
	private boolean includeResources = false;
	
	private boolean includeMedia = false;
	
	private File destFile;
	
	public ProjectArchiveTask(Project project, File destFile) {
		this(project, destFile, true, true);
	}
	
	public ProjectArchiveTask(Project project, File destFile, boolean includeResources, boolean includeMedia) {
		this.project = project;
		this.destFile = destFile;
		this.includeResources = includeResources;
		this.includeMedia = includeMedia;
	}
	
	private List<File> buildFileList() {
		List<File> retVal = new ArrayList<File>();
		
		File projectRoot = new File(project.getLocation());
		File projectXmlFile = new File(projectRoot, "project.xml");
		retVal.add(projectXmlFile);
		
		// scan corpus directories
		for(File f:projectRoot.listFiles()) {
			if(f.isDirectory()
					&& !f.getName().startsWith("__") && !f.getName().startsWith("~")
					&& !f.isHidden()) {
				for(File cf:f.listFiles()) {
					if(!cf.isHidden() && cf.isFile()) {
						if(cf.getName().startsWith("__")) {
							if(cf.getName().equals("__sessiontemplate.xml")) {
								// add file
								retVal.add(cf);
							}
						} else if(!cf.getName().startsWith("~")) {
							retVal.add(cf);
						}
					}
				}
			}
		}
		
		if(includeResources) {
			File resDir = new File(projectRoot, "__res");
			if(resDir.exists()) {
				List<File> resFiles = listFilesRecursive(resDir);
				retVal.addAll(resFiles);
			}
		}
		
		return retVal;
	}
	
	private List<File> listFilesRecursive(File f) {
		List<File> retVal = new ArrayList<File>();
		
		if(f.isDirectory()) {
			for(File sf:f.listFiles()) {
				if(sf.isHidden()) continue;
				
				if(sf.isFile()) {
					retVal.add(sf);
				} else {
					List<File> subVals = listFilesRecursive(sf);
					retVal.addAll(subVals);
				}
			}
		} else {
			retVal.add(f);
		}
		
		return retVal;
	}
	
	@Override
	public void performTask() {
		super.setStatus(TaskStatus.RUNNING);
		
		super.setProperty(STATUS_PROP, "Writing to file " + destFile.getAbsolutePath());
		if(destFile.exists()) {
			// print warning and delete file
			LOGGER.warning("Overwriting file '" + destFile.getAbsolutePath() + "'");
			if(!destFile.delete()) {
				LOGGER.warning("Could not delete file '" + destFile.getAbsolutePath() + "'");
			}
		}
		
		try {
			FileOutputStream fos = new FileOutputStream(destFile);
			ZipOutputStream zos = new ZipOutputStream(fos);
			
			byte[] buffer = new byte[1024];
			int rlen = 0;
			
			// create a zip entry for each project file
			File projectRoot = new File(project.getLocation());
			
			super.setProperty(STATUS_PROP, "Building file list");
			List<File> projectFiles = buildFileList();
			for(File projectFile:projectFiles) {
				
				if(isShutdown()) {
					zos.flush();
					zos.close();
					
					setProperty(STATUS_PROP, "User terminated");
					
					return;
				}
				
				// setup zip entry name
				int projectPathLen = projectRoot.getPath().length();
				String entryName =
					project.getName() + 
					projectFile.getPath().substring(projectPathLen);
				
				if(File.separatorChar != '/') {
					entryName = entryName.replace(File.separatorChar, '/');
				}
				
				ZipEntry entry = new ZipEntry(entryName);
				zos.putNextEntry(entry);
				

				setProperty(STATUS_PROP, "Archiving: " + entryName);
				
				FileInputStream fis = new FileInputStream(projectFile);
				rlen = 0;
				while((rlen = fis.read(buffer)) > 0) {
					zos.write(buffer, 0, rlen);
				}
				fis.close();
				
				zos.closeEntry();
			}
			
			setProperty(STATUS_PROP, "Flushing data");
			zos.flush();
			zos.close();
			
		} catch (IOException e) {
			LOGGER.severe(e.toString());
			super.err = e;
			super.setStatus(TaskStatus.ERROR);
			return;
		}
		
		String msg1 = "Archive created";
		String msg2 = "Archive of " + project.getName() + " created at " + 
			destFile.getAbsolutePath();
		NativeDialogs.showMessageDialog(CommonModuleFrame.getCurrentFrame(), new NativeDialogListener() {
			
			@Override
			public void nativeDialogEvent(NativeDialogEvent event) {
				// TODO Auto-generated method stub
				
			}
		}, null, msg1, msg2);
		
		super.setProperty(STATUS_PROP, "Finished");
		super.setStatus(TaskStatus.FINISHED);
	}
	
}