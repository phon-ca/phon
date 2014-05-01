package ca.phon.app.session.editor.view.tier_management.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.SessionEditorAction;
import ca.phon.app.session.editor.view.tier_management.TierOrderingEditorView;

public abstract class TierManagementAction extends SessionEditorAction {

	private static final long serialVersionUID = -4527188790604848801L;

	private TierOrderingEditorView view;
	
	public TierManagementAction(SessionEditor editor, TierOrderingEditorView view) {
		super(editor);
		this.view = view;
	}
	
	public TierOrderingEditorView getView() {
		return this.view;
	}

}
