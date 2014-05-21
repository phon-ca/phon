package ca.phon.app.session.editor.view.find_and_replace.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.SessionEditorAction;
import ca.phon.app.session.editor.view.find_and_replace.FindAndReplaceEditorView;

public abstract class FindAndReplaceAction extends SessionEditorAction {

	private static final long serialVersionUID = 9160680400724132644L;
	
	private final FindAndReplaceEditorView view;
	
	public FindAndReplaceAction(SessionEditor editor, FindAndReplaceEditorView view) {
		super(editor);
		this.view = view;
	}
	
	public FindAndReplaceEditorView getView() {
		return this.view;
	}

}
