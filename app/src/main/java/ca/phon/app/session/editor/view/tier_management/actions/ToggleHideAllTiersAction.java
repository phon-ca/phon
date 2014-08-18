package ca.phon.app.session.editor.view.tier_management.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierViewEdit;
import ca.phon.app.session.editor.view.tier_management.TierOrderingEditorView;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.TierViewItem;

public class ToggleHideAllTiersAction extends TierManagementAction {

	private static final long serialVersionUID = 7113354516634976428L;

	public ToggleHideAllTiersAction(SessionEditor editor,
			TierOrderingEditorView view) {
		super(editor, view);
		
		final boolean allVisible = areAllVisible();
		putValue(NAME, (allVisible ? "Hide " : "Show ") + "all tiers");
	}
	
	private boolean areAllVisible() {
		boolean retVal = true;
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		final List<TierViewItem> view = session.getTierView();
		for(TierViewItem tvi:view) {
			retVal &= tvi.isVisible();
		}
		
		return retVal;
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		final List<TierViewItem> view = session.getTierView();
		final List<TierViewItem> newView = new ArrayList<TierViewItem>();
		final SessionFactory factory = SessionFactory.newFactory();
		
		final boolean allVisible = areAllVisible();
		for(TierViewItem oldItem:view) {
			final TierViewItem newItem = factory.createTierViewItem(oldItem.getTierName(), !allVisible,
					oldItem.getTierFont(), oldItem.isTierLocked());
			newView.add(newItem);
		}
		
		final TierViewEdit edit = new TierViewEdit(getEditor(), view, newView);
		editor.getUndoSupport().postEdit(edit);
		
		putValue(NAME, (!allVisible ? "Hide " : "Show ") + "all tiers");
	}

}
