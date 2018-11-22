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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.DeleteRecordEdit;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Delete the current record.
 */
public class DeleteRecordAction extends SessionEditorAction {

	private static final long serialVersionUID = -6995854542145591135L;
	
	private final static String CMD_NAME = "Delete record";
	
	private final static String SHORT_DESC = "Delete current record";
	
	private final static String ICON = "misc/record-delete";
	
	private final static KeyStroke KS = 
			KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
	
	public static final String CONFIRM_DELETE_RECORD_PROP =
			DeleteRecordAction.class.getName() + ".confirm";
	public static final boolean DEFAULT_CONFIRM_DELETE_RECORD = true;
	private boolean confirm = PrefHelper.getBoolean(CONFIRM_DELETE_RECORD_PROP, DEFAULT_CONFIRM_DELETE_RECORD);

	public DeleteRecordAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(ACCELERATOR_KEY, KS);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		// display confirmation dialog
		if(confirm) {
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setRunAsync(false);
			props.setParentWindow(getEditor());
			props.setTitle("Delete record");
			props.setHeader("Confirm delete record");
			props.setMessage("Delete record " + (getEditor().getCurrentRecordIndex()+1) + "?");
			props.setOptions(MessageDialogProperties.okCancelOptions);
			int retVal = NativeDialogs.showMessageDialog(props);
			if(retVal == 1) return;
		}
		final DeleteRecordEdit edit = new DeleteRecordEdit(getEditor());
		getEditor().getUndoSupport().postEdit(edit);
	}

}
