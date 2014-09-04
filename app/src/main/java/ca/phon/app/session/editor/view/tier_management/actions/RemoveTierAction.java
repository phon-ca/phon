package ca.phon.app.session.editor.view.tier_management.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.RemoveTierEdit;
import ca.phon.app.session.editor.view.tier_management.TierOrderingEditorView;
import ca.phon.session.TierDescription;
import ca.phon.session.TierViewItem;

public class RemoveTierAction extends TierManagementAction {

	private static final long serialVersionUID = 2530259863724783877L;

	private TierDescription td;
	
	private TierViewItem tvi;
	
	public RemoveTierAction(SessionEditor editor, TierOrderingEditorView view,
			TierDescription td, TierViewItem tvi) {
		super(editor, view);
		this.td = td;
		this.tvi = tvi;
		
		putValue(NAME, "Delete tier " + td.getName());
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final RemoveTierEdit edit = new RemoveTierEdit(getEditor(), td, tvi);
		getEditor().getUndoSupport().postEdit(edit);
	}

}
