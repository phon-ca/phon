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
package ca.phon.app.session.editor.actions;

import java.awt.event.ActionEvent;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.ImageIcon;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.io.SessionIO;
import ca.phon.session.io.SessionOutputFactory;
import ca.phon.session.io.SessionWriter;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Display save as... dialog for Sessions
 *
 */
public class SaveAsAction extends SessionEditorAction {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(SaveAsAction.class.getName());

	private static final long serialVersionUID = -168218633218148720L;

	private final static String TXT = "Save as ";
	
	private final static String DESC = "Save session as ";
	
	private final static ImageIcon ICON =
			IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL);
	
	private final SessionIO sessionIO;
	
	public SaveAsAction(SessionEditor editor, SessionIO sessionIO) {
		super(editor);
		
		this.sessionIO = sessionIO;
		
		putValue(NAME, TXT + sessionIO.name());
		putValue(SHORT_DESCRIPTION, DESC + sessionIO.name());
		putValue(SMALL_ICON, ICON);
	}

	public SessionIO getSessionIO() {
		return this.sessionIO;
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		// ask for a filename
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setParentWindow(getEditor());
		final FileFilter filter = new FileFilter(sessionIO.name(), sessionIO.extension());
		props.setFileFilter(filter);
		props.setCanCreateDirectories(true);
		props.setListener( (e) -> {
			if(e.getDialogResult() == NativeDialogEvent.OK_OPTION
					&& e.getDialogData() != null) {
				saveSessionToFile((String)e.getDialogData());
			}
		});
		props.setNameFieldLabel(TXT + sessionIO.name());
		props.setMessage("Save session to file");
		props.setTitle("Save Session as " + sessionIO.name());
		NativeDialogs.showSaveDialog(props);
	}
	
	private void saveSessionToFile(String filename) {
		// get session writer
		final SessionOutputFactory factory = new SessionOutputFactory();
		final SessionWriter writer = factory.createWriter(sessionIO);
		
		try {
			writer.writeSession(getEditor().getSession(), new FileOutputStream(filename));
		} catch (IOException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
			getEditor().showErrorMessage("Save session failed: " + e.getLocalizedMessage());
		}
	}
}
