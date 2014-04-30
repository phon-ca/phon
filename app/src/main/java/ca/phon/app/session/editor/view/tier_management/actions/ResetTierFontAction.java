package ca.phon.app.session.editor.view.tier_management.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierViewItemEdit;
import ca.phon.app.session.editor.view.tier_management.TierOrderingEditorView;
import ca.phon.session.SessionFactory;
import ca.phon.session.TierViewItem;

public class ResetTierFontAction extends TierManagementAction {

	private static final long serialVersionUID = -8360846647372033809L;
	
	private final static String CMD_NAME = "Reset font";
	
	private final static String SHORT_DESC = "Reset tier font to default";
	
	private final TierViewItem item;

	public ResetTierFontAction(SessionEditor editor, TierOrderingEditorView view,
			TierViewItem item) {
		super(editor, view);
		this.item = item;
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final SessionFactory factory = SessionFactory.newFactory();
		final TierViewItem newItem = factory.createTierViewItem(item.getTierName(), item.isVisible(), "default", item.isTierLocked());
		
		final TierViewItemEdit edit = new TierViewItemEdit(getEditor(), item, newItem);
		getEditor().getUndoSupport().postEdit(edit);
	}

}
