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
package ca.phon.app.session.editor.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatterBuilder;

import ca.phon.app.hooks.ActionHook;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.util.PrefHelper;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class BackupCommandHook implements ActionHook<SaveSessionAction>, IPluginExtensionPoint<ActionHook<SaveSessionAction>> {

	private static final Logger LOGGER = Logger
			.getLogger(BackupCommandHook.class.getName());
	
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
		
        final DateTime dateTime = DateTime.now();
        final DateTimeFormatterBuilder formatterBuilder = new DateTimeFormatterBuilder();
        final String dateSuffix = formatterBuilder.appendYear(4, 4).appendLiteral("-").appendMonthOfYear(2).appendLiteral("-")
            .appendDayOfMonth(2).appendLiteral("_").appendHourOfDay(2).appendLiteral(".")
            .appendMinuteOfHour(2).appendLiteral(".").appendSecondOfMinute(2).toFormatter().print(dateTime);

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
				LOGGER
						.log(Level.SEVERE, e.getLocalizedMessage(), e);
				
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
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
