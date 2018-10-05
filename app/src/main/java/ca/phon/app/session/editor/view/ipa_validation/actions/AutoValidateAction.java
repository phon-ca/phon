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
package ca.phon.app.session.editor.view.ipa_validation.actions;

import java.awt.event.ActionEvent;

import javax.swing.undo.UndoableEdit;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.ipa_validation.AutoValidateDialog;
import ca.phon.app.session.editor.view.ipa_validation.AutoValidateDialog.AutoValidateReturnValue;
import ca.phon.app.session.editor.view.ipa_validation.AutoValidateTask;
import ca.phon.app.session.editor.view.ipa_validation.ValidationEditorView;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTask.TaskStatus;
import ca.phon.worker.PhonTaskListener;
import ca.phon.worker.PhonWorker;

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
