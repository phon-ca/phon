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

import java.awt.event.*;
import java.io.*;
import java.time.*;
import java.time.format.*;

import ca.phon.app.hooks.*;
import ca.phon.app.session.editor.*;
import ca.phon.plugin.*;
import ca.phon.project.*;
import ca.phon.session.*;
import ca.phon.util.*;
import net.lingala.zip4j.core.*;
import net.lingala.zip4j.exception.*;
import net.lingala.zip4j.model.*;
import net.lingala.zip4j.util.*;

public class BackupCommandHook implements ActionHook<SaveSessionAction>, IPluginExtensionPoint<ActionHook<SaveSessionAction>> {

	private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(BackupCommandHook.class.getName());
	
	@Override
	public Class<? extends SaveSessionAction> getActionType() {
		return SaveSessionAction.class;
	}

	private void backupSession(Project project, Session session) 
		throws IOException, ZipException {
		// save current session to backup zip
		final String zipFilePath = project.getLocation() + File.separator + "backups.zip";
		// create backup zip if necessary
		final ZipFile zipFile = new ZipFile(zipFilePath);
		
        final LocalDateTime dateTime = LocalDateTime.now();
        final DateTimeFormatterBuilder formatterBuilder = new DateTimeFormatterBuilder();
        final String dateSuffix = formatterBuilder.appendPattern("yyyy").appendLiteral("-").appendPattern("MM").appendLiteral("-")
            .appendPattern("dd").appendLiteral("_").appendPattern("HH").appendLiteral(".")
            .appendPattern("mm").appendLiteral(".").appendPattern("ss").toFormatter().format(dateTime);

        final String zipName =
        		session.getName() + "_" + dateSuffix + ".xml";
      
        final File sessionFile = new File(project.getLocation(), 
        	session.getCorpus() + File.separator + session.getName() + ".xml");
    
        if(sessionFile.exists()) {
        	if(!zipFile.getFile().exists()) {
        		ZipParameters parameters = new ZipParameters();
    			
    			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
    			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
    			
    			zipFile.createZipFile(new File(project.getLocation() + File.separator + "project.xml"), parameters);
        	}
        	// add to zip file
    		ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			
			parameters.setFileNameInZip(session.getCorpus() + File.separator + zipName);
			parameters.setSourceExternalStream(true);
			
			FileInputStream fin = null;
			try {
				fin = new FileInputStream(sessionFile);
				zipFile.addStream(fin, parameters);
			} catch (IOException e) {
				LOGGER.error(e.getLocalizedMessage(), e);
				
			} finally {
				if(fin != null) fin.close();
			}
        }
	}
	
	@Override
	public boolean beforeAction(SaveSessionAction action, ActionEvent ae) {
		if(PrefHelper.getBoolean(SessionEditor.BACKUP_WHEN_SAVING, Boolean.TRUE)) {
			try {
				backupSession(action.getEditor().getProject(), action.getEditor().getSession());
			} catch (ZipException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			} catch (IOException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
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
