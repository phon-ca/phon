package ca.phon.app.session.editor.view.find_and_replace.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.find_and_replace.FindAndReplaceEditorView;

public class ReplaceAllAction extends FindAndReplaceAction {

	private static final long serialVersionUID = 4752402371749797345L;

	private final static String CMD_NAME = "Replace all";
	
	private final static String SHORT_DESC = "Replace all occurrences";
	
	public ReplaceAllAction(SessionEditor editor, FindAndReplaceEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		getView().replaceAll();
	}

}
