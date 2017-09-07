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
package ca.phon.app.session.editor.view.ipa_validation.actions;

import java.awt.event.ActionEvent;

import javax.swing.undo.UndoableEdit;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.ipa_validation.*;
import ca.phon.app.session.editor.view.ipa_validation.AutoValidateDialog.AutoValidateReturnValue;
import ca.phon.worker.*;
import ca.phon.worker.PhonTask.TaskStatus;

public class AutoValidateAction extends ValidationAction {

	private static final long serialVersionUID = -1323276411622657672L;
	
	private final static String CMD_NAME = "Auto validate";
	
	private final static String SHORT_DESC = "Auto validate session...";
	
	public AutoValidateAction(SessionEditor editor, ValidationEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		// show dialog
		final AutoValidateDialog dialog = new AutoValidateDialog(getEditor().getProject(), getEditor().getSession());
		final AutoValidateReturnValue retVal = dialog.showModalDialog();
		
		if(retVal == AutoValidateReturnValue.OK) {
			final AutoValidateTask task = dialog.getTask();
			task.addTaskListener(new PhonTaskListener() {
				
				@Override
				public void statusChanged(PhonTask t, TaskStatus oldStatus,
						TaskStatus newStatus) {
					if(newStatus == TaskStatus.FINISHED) {
						final UndoableEdit edit = task.getUndoableEdit();
						getEditor().getUndoSupport().postEdit(edit);
					}
				}
				
				@Override
				public void propertyChanged(PhonTask task, String property,
						Object oldValue, Object newValue) {
				}
			});
			PhonWorker.getInstance().invokeLater(task);
		}
	}

}
