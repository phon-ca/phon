package ca.phon.app.session.editor.undo;

import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.CannotUndoException;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Session;
import ca.phon.session.TierDescription;
import ca.phon.session.TierViewItem;

public class AddTierEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = 5600095287675463984L;

	protected final TierDescription tierDescription;
	
	protected final TierViewItem tierViewItem;
	
	public AddTierEdit(SessionEditor editor, TierDescription tierDesc, TierViewItem tvi) {
		super(editor);
		this.tierDescription = tierDesc;
		this.tierViewItem = tvi;
	}
	
	@Override
	public String getRedoPresentationName() {
		return "Redo add tier " + tierDescription.getName();
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo add tier " + tierDescription.getName();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		final Object oldSource = getSource();
		setSource(editor.getUndoSupport());
		
		session.removeUserTier(tierDescription);
		
		final List<TierViewItem> tierView = session.getTierView();
		final List<TierViewItem> newView = new ArrayList<TierViewItem>(tierView);
		newView.remove(this.tierViewItem);
		session.setTierView(newView);
		
		queueEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, getSource(), newView);
		
		setSource(oldSource);
	}

	@Override
	public void doIt() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		session.addUserTier(tierDescription);
		
		final List<TierViewItem> tierView = session.getTierView();
		final List<TierViewItem> newView = new ArrayList<TierViewItem>(tierView);
		newView.add(this.tierViewItem);
		session.setTierView(newView);
		
		queueEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, getSource(), newView);
	}

}
