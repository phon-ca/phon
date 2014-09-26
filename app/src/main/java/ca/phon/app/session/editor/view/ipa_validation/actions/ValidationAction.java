package ca.phon.app.session.editor.view.ipa_validation.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.SessionEditorAction;
import ca.phon.app.session.editor.view.ipa_validation.ValidationEditorView;

public abstract class ValidationAction extends SessionEditorAction {

	private static final long serialVersionUID = 5230460874454441290L;

	private final ValidationEditorView view;
	
	public ValidationAction(SessionEditor editor, ValidationEditorView view) {
		super(editor);
		this.view = view;
	}

	public ValidationEditorView getView() {
		return this.view;
	}

}
