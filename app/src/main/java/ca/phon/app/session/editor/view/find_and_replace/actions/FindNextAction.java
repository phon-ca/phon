package ca.phon.app.session.editor.view.find_and_replace.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.find_and_replace.FindAndReplaceEditorView;

public class FindNextAction extends FindAndReplaceAction {

	private static final long serialVersionUID = -4650188145086839956L;
	
	private final static String CMD_NAME = "Find next";
	
	private final static String SHORT_DESC = "Find next";

	public FindNextAction(SessionEditor editor, FindAndReplaceEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
	}

}
