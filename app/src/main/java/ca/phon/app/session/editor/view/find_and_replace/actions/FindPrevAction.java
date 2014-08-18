package ca.phon.app.session.editor.view.find_and_replace.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.find_and_replace.FindAndReplaceEditorView;

public class FindPrevAction extends FindAndReplaceAction {

	private static final long serialVersionUID = -3610764173181048977L;
	
	private final static String CMD_NAME = "Find previous";
	
	private final static String SHORT_DESC = "Find previous";

	public FindPrevAction(SessionEditor editor, FindAndReplaceEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		getView().findPrev();
	}

}
