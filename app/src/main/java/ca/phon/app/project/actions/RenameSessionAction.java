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
package ca.phon.app.project.actions;

import ca.phon.app.log.LogUtil;
import ca.phon.app.project.*;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.session.io.OriginalFormat;
import ca.phon.session.io.SessionIO;
import ca.phon.session.io.SessionOutputFactory;
import ca.phon.session.io.SessionWriter;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.CollatorFactory;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class RenameSessionAction extends ProjectWindowAction {
	
	public RenameSessionAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "Rename session");
		putValue(SHORT_DESCRIPTION, "Rename selected session");
	}

	/**
	 * Backup existing session file before upgrading.  Copies file to the backupFolderName directory
	 * which will be created if it does not exist.  Inner corpus path of the backup file will be the same
	 * as the original file.
	 *
	 * @param project
	 * @param session
	 * @param backupFolderName should be a relative path to the project location
	 *
	 * @throws IOException
	 *
	 */
	private void createUpgradeBackup(Project project, Session session, String backupFolderName) throws IOException {
		final File sessionFile = new File(project.getSessionPath(session));
		if(!sessionFile.exists()) {
			throw new IOException("Session file does not exist");
		}

		final File backupsFolder = new File(project.getLocation(), backupFolderName);
		if(!backupsFolder.exists()) {
			backupsFolder.mkdirs();
		}

		// copy file to backup folder, using relative path from project location
		// as the inner corpus path
		final String corpusPath = session.getCorpus();
		final String backupPath = backupFolderName + File.separator + corpusPath;
		final File backupFolder = new File(project.getLocation(), backupPath);
		if(!backupFolder.exists()) {
			backupFolder.mkdirs();
		}

		final File backupFile = new File(backupFolder, sessionFile.getName());
		if(backupFile.exists()) {
			backupFile.delete();
		}

		// copy file to backup folder
		FileUtils.copyFile(sessionFile, backupFile);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final Project project = getWindow().getProject();
		final String selectedCorpus = getWindow().getSelectedCorpus();
		final String selectedSession = getWindow().getSelectedSessionName();
		
		if(selectedCorpus == null || selectedSession == null) {
			ToastFactory.makeToast("Please select a session").start(getWindow().getSessionList());
			return;
		}
		
		final RenameSessionDialog dialog = new RenameSessionDialog(project, selectedCorpus, selectedSession);
		dialog.setModal(true);
		dialog.pack();
		dialog.setLocationRelativeTo(getWindow());
		dialog.setVisible(true);
		
		if(!dialog.wasCanceled()) {
			final String corpusName = dialog.getCorpus();
			final String sessionName = dialog.getSessionName();
			final String newSessionName = dialog.getNewSessionName();
			
			if (newSessionName == null || newSessionName.length() == 0) {
				showMessage("Rename Session", "Please enter session name.");
				return;
			}

			// Run through the sessions to see if the corpus specified exists, and
			// and also make sure that the new name isn't the name of an existing
			// corpus
			if (project.getCorpusSessions(corpusName).contains(newSessionName)) {
				showMessage("Rename Session", "A session with that name already exists.");
				return;
			}
			
			// Transfer XML data to the new session name
			Session session = null;
			try {
				session = project.openSession(corpusName, sessionName);
			} catch(Exception e) {
				LogUtil.warning(e);
				showMessage("Rename Session", e.getLocalizedMessage());
				return;
			}
			
			UUID writeLock = null;
			try {
				writeLock = project.getSessionWriteLock(corpusName, newSessionName);

				// determine if the session requires conversion into Phon 4.x format
				// if so, ask the user if they want to do that
				final OriginalFormat originalFormat = session.getExtension(OriginalFormat.class);
				boolean convert = originalFormat == null;
				SessionWriter writer = null;
				if(originalFormat != null) {
					// try to find writer for that format
					final SessionIO originalSessionIO = originalFormat.getSessionIO();
					final SessionOutputFactory sessionOutputFactory = new SessionOutputFactory();
					writer = sessionOutputFactory.createWriter(originalSessionIO);

					if(writer == null) {
						writer = sessionOutputFactory.createWriter();
						final SessionIO currentFormat = writer.getClass().getAnnotation(SessionIO.class);
						final MessageDialogProperties props = new MessageDialogProperties();
						props.setParentWindow(CommonModuleFrame.getCurrentFrame());
						props.setRunAsync(false);
						props.setTitle("Rename session");
						String formatName = currentFormat.name();
						// remove " (.ext)" from end of name
						if(formatName.endsWith(" (.xml)")) {
							formatName = formatName.substring(0, formatName.length()-7);
						}
						props.setHeader("Upgrade transcript for " + formatName + "?");

						final String backupFolderName = "__v" + originalFormat.getSessionIO().version().replaceAll("\\.", "_") + "-backups__";
						props.setMessage("A backup file will be created at: " + project.getLocation() + File.separator + backupFolderName +
								". After upgrading, the current transcript will not open in previous versions of Phon.");
						props.setOptions(MessageDialogProperties.okCancelOptions);

						final int retVal = NativeDialogs.showMessageDialog(props);
						if(retVal == 0) {
							createUpgradeBackup(project, session, backupFolderName);
						} else {
							return;
						}
					}
				}

				session.setName(newSessionName);
				if(writer != null) {
					project.saveSession(corpusName, newSessionName, session, writer, writeLock);
				} else {
					project.saveSession(corpusName, newSessionName, session, writeLock);
				}
			} catch (Exception e) {
				LogUtil.warning(e);
				showMessage("Rename Session", e.getLocalizedMessage());
			} finally {
				if(writeLock != null) {
					try {
						project.releaseSessionWriteLock(corpusName, newSessionName, writeLock);
					} catch (IOException e) {
						LogUtil.warning(e);
					}
					writeLock = null;
				}
			}
			
			try {
				writeLock = project.getSessionWriteLock(corpusName, sessionName);
				project.removeSession(corpusName, sessionName, writeLock);
			} catch (Exception e) {
				LogUtil.warning(e);
				showMessage("Rename Session", e.getLocalizedMessage());
			} finally {
				if(writeLock != null) {
					try {
						project.releaseSessionWriteLock(corpusName, sessionName, writeLock);
					} catch (IOException e) {
						LogUtil.warning(e);
					}
				}
			}
			
			// select new session
			final List<String> sessionNames = project.getCorpusSessions(corpusName);
			Collections.sort(sessionNames, CollatorFactory.defaultCollator());
			int idx = sessionNames.indexOf(newSessionName);
			if(idx >= 0) {
				getWindow().getSessionList().setSelectedIndex(idx);
			}
		}
	}

}
