package ca.phon.app.session.editor.view.ipa_validation.actions;

import java.awt.event.ActionEvent;

import javax.swing.undo.UndoableEdit;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.ipa_validation.AutoValidateDialog;
import ca.phon.app.session.editor.view.ipa_validation.AutoValidateTask;
import ca.phon.app.session.editor.view.ipa_validation.AutoValidateDialog.AutoValidateReturnValue;
import ca.phon.app.session.editor.view.ipa_validation.ValidationEditorView;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTaskListener;
import ca.phon.worker.PhonWorker;
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
	public void actionPerformed(ActionEvent e) {
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
