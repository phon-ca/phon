package ca.phon.app.session.editor.view.syllabification_and_alignment.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.SessionEditorAction;
import ca.phon.app.session.editor.view.syllabification_and_alignment.SyllabificationAlignmentEditorView;

public abstract class SyllabificationAlignmentCommand extends SessionEditorAction {

	private static final long serialVersionUID = -335734821467176021L;

	private final SyllabificationAlignmentEditorView view;
	
	public SyllabificationAlignmentCommand(SessionEditor editor, SyllabificationAlignmentEditorView view) {
		super(editor);
		this.view = view;
	}
	
	public SyllabificationAlignmentEditorView getView() {
		return this.view;
	}

}
