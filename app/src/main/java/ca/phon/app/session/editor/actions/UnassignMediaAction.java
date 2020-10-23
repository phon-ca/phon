package ca.phon.app.session.editor.actions;

import java.awt.event.*;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.*;

public class UnassignMediaAction extends SessionEditorAction {

	private static final String TXT = "Unassign session media";
	private static final String DESC = "Clear media selection for this session";
	
	public UnassignMediaAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		MediaLocationEdit edit = new MediaLocationEdit(getEditor(), null);
		getEditor().getUndoSupport().postEdit(edit);
	}

}
