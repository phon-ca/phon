/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.awt.Toolkit;
import java.awt.event.*;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.DeleteRecordEdit;
import ca.phon.ui.nativedialogs.*;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.*;

/**
 * Delete the current record.
 */
public class DeleteRecordAction extends SessionEditorAction {

	private static final long serialVersionUID = -6995854542145591135L;
	
	private final static String CMD_NAME = "Delete record";
	
	private final static String SHORT_DESC = "Delete current record";
	
	private final static String ICON = "misc/record-delete";
	
	private final static KeyStroke KS = 
			KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	
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
