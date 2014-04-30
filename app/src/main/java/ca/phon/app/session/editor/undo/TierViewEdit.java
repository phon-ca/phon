package ca.phon.app.session.editor.undo;

import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.CannotUndoException;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Session;
import ca.phon.session.TierViewItem;

/**
 * Changes to the tier view including order, visibility and locking.
 *
 */
public class TierViewEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = 8337753863840703579L;

	/**
	 * Old view
	 */
	private final List<TierViewItem> oldView;
	
	/**
	 * New view
	 */
	private final List<TierViewItem> newView;
	
	public TierViewEdit(SessionEditor editor, List<TierViewItem> oldView, List<TierViewItem> newView) {
		super(editor);
		this.oldView = new ArrayList<TierViewItem>(oldView);
		this.newView = newView;
	}
	
	@Override
	public boolean canRedo() {
		return true;
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo set tier order";
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo set tier order";
	}
	
	@Override
	public void undo() throws CannotUndoException {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		session.setTierView(oldView);
		
		super.queueEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, editor.getUndoSupport(), oldView);
	}

	@Override
	public void doIt() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		session.setTierView(newView);
		
		super.queueEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, getSource(), newView);
	}
	
}
