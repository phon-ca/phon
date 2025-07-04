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
package ca.phon.app.session.editor.actions;

import ca.phon.app.hooks.ActionHook;
import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.project.LocalProject;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.util.PrefHelper;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatterBuilder;

public class BackupCommandHook implements ActionHook<SaveSessionAction>, IPluginExtensionPoint<ActionHook<SaveSessionAction>> {

	@Override
	public Class<? extends SaveSessionAction> getActionType() {
		return SaveSessionAction.class;
	}

	private void backupSession(Project project, Session session) 
		throws IOException, ZipException {
		if(project instanceof LocalProject localProject) {
			// save current session to backup zip
			final String zipFilePath = localProject.getLocation() + File.separator + "backups.zip";
			// create backup zip if necessary
			final ZipFile zipFile = new ZipFile(zipFilePath);

			final LocalDateTime dateTime = LocalDateTime.now();
			final DateTimeFormatterBuilder formatterBuilder = new DateTimeFormatterBuilder();
			final String dateSuffix = formatterBuilder.appendPattern("yyyy").appendLiteral("-").appendPattern("MM").appendLiteral("-")
					.appendPattern("dd").appendLiteral("_").appendPattern("HH").appendLiteral("-")
					.appendPattern("mm").appendLiteral("-").appendPattern("ss").toFormatter().format(dateTime);

			final String zipName =
					session.getName() + "_" + dateSuffix + ".xml";

			final File sessionFile = new File(localProject.getLocation(),
					session.getCorpus() + File.separator + session.getName() + ".xml");

			if (sessionFile.exists()) {
				if (!zipFile.getFile().exists()) {
					ZipParameters parameters = new ZipParameters();

					parameters.setCompressionMethod(CompressionMethod.DEFLATE);
					parameters.setCompressionLevel(CompressionLevel.NORMAL);

					// add .phonproj file (if exists)
					final File projectFile = new File(localProject.getLocation(), project.getName() + ".phonproj");
					if (projectFile.exists()) {
						zipFile.addFile(projectFile, parameters);
					}
				}
				// add to zip file
				ZipParameters parameters = new ZipParameters();
				parameters.setCompressionMethod(CompressionMethod.DEFLATE);
				parameters.setCompressionLevel(CompressionLevel.NORMAL);
				parameters.setFileNameInZip(session.getCorpus() + File.separator + zipName);

				FileInputStream fin = null;
				try {
					fin = new FileInputStream(sessionFile);
					zipFile.addStream(fin, parameters);
				} catch (IOException e) {
					LogUtil.warning(e);

				} finally {
					if (fin != null) fin.close();
				}
			}
		} else {
			throw new IOException("Project is not a local project");
		}
	}
	
	@Override
	public boolean beforeAction(SaveSessionAction action, ActionEvent ae) {
		if(PrefHelper.getBoolean(SessionEditor.BACKUP_WHEN_SAVING, Boolean.TRUE)) {
			try {
				backupSession(action.getEditor().getProject(), action.getEditor().getSession());
			} catch (IOException e) {
				LogUtil.warning(e);
			}
		}
		return false;
	}

	@Override
	public void afterAction(SaveSessionAction action, ActionEvent ae) {
	}

	@Override
	public Class<?> getExtensionType() {
		return ActionHook.class;
	}

	@Override
	public IPluginExtensionFactory<ActionHook<SaveSessionAction>> getFactory() {
		return factory;
	}
	
	private final IPluginExtensionFactory<ActionHook<SaveSessionAction>> factory = new IPluginExtensionFactory<ActionHook<SaveSessionAction>>() {
		
		@Override
		public ActionHook<SaveSessionAction> createObject(Object... args) {
			return BackupCommandHook.this;
		}
	};

}
