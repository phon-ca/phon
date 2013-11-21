package ca.phon.app.session.editor.undo;

import java.lang.ref.WeakReference;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoManager;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Session;

/**
 * Base class for {@link SessionEditor} changes.  All changes to the
 * {@link Session} should go through the {@link UndoManager}.
 *
 */
public abstract class SessionEditorUndoableEdit extends AbstractUndoableEdit {

	private static final long serialVersionUID = 7922388747133546800L;

	/**
	 * Reference to the session editor
	 */
	private final WeakReference<SessionEditor> editorRef;
	
	/**
	 * Optional 'source' for edit.
	 */
	private Object source;
	
	/**
	 * Constructor
	 * 
	 * @param editor
	 */
	public SessionEditorUndoableEdit(SessionEditor editor) {
		super();
		this.editorRef = new WeakReference<SessionEditor>(editor);
	}
	
	public SessionEditor getEditor() {
		return editorRef.get();
	}
	
	/**
	 * Helper method for firing events
	 * 
	 * @param eventName
	 * @param eventData
	 */
	public void queueEvent(String eventName, Object src, Object eventData) {
		final SessionEditor editor = getEditor();
		final EditorEventManager eventManager = editor.getEventManager();
		final EditorEvent event = new EditorEvent(eventName, src, eventData);
		eventManager.queueEvent(event);
	}
	
	/**
	 * Get the source for the edit.  This value is given to the resulting
	 * EditorEvents.  
	 * 
	 * During undo/redo operations, the source is always the value of
	 * getEditor().getUndoSupport().
	 * 
	 * @return the source
	 */
	public Object getSource() {
		return this.source;
	}
	
	/**
	 * Set the source for the edit.  This is the source passed to the
	 * initial execution of the edit.  Undo/redo operations always use
	 * the editor's undo support as the source.
	 * 
	 * @param the edit source
	 */
	public void setSource(Object source) {
		this.source = source;
	}
	
	@Override
	public void redo() {
		final Object oldSource = getSource();
		setSource(getEditor().getUndoSupport());
		doIt();
		setSource(oldSource);
	}
	
	/**
	 * 'Do' the specified action.  The method is called by the
	 * EditorUnsoSupport when new edits are posted.
	 * 
	 * 
	 */
	public abstract void doIt();

	@Override
	public boolean canRedo() {
		return true;
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public boolean isSignificant() {
		return true;
	}
	
}
