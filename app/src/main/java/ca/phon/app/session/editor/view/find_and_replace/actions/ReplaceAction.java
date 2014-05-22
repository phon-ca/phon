package ca.phon.app.session.editor.view.find_and_replace.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.find_and_replace.FindAndReplaceEditorView;

public class ReplaceAction extends FindAndReplaceAction {

	private static final long serialVersionUID = 2574281425626924879L;
	
	private final static String CMD_NAME = "Replace";
	
	private final static String SHORT_DESC = "Replace";
	
	private boolean andFind = false;
	
	public ReplaceAction(SessionEditor editor, FindAndReplaceEditorView view, boolean andFind) {
		super(editor, view);
		
		this.andFind = andFind;
		
		putValue(NAME, CMD_NAME + (andFind ? " and find" : ""));
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

}
