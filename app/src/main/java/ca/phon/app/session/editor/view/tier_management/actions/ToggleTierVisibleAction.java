package ca.phon.app.session.editor.view.tier_management.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierViewItemEdit;
import ca.phon.app.session.editor.view.tier_management.TierOrderingEditorView;
import ca.phon.session.SessionFactory;
import ca.phon.session.TierViewItem;

public class ToggleTierVisibleAction extends TierManagementAction {

	private static final long serialVersionUID = -4743364905920496226L;
	
	private final static String CMD_NAME = "Toggle tier visible";
	
	private final static String SHORT_DESC = "";
	
	private final TierViewItem item;

	public ToggleTierVisibleAction(SessionEditor editor,
			TierOrderingEditorView view, TierViewItem tierViewItem) {
		super(editor, view);
		this.item = tierViewItem;
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final SessionFactory factory = SessionFactory.newFactory();
		final TierViewItem newItem = factory.createTierViewItem(item.getTierName(), !item.isVisible(), item.isTierLocked());
		
		final TierViewItemEdit edit = new TierViewItemEdit(getEditor(), item, newItem);
		getEditor().getUndoSupport().postEdit(edit);
	}

}
