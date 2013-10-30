package ca.phon.app.session.editor.undo;

import javax.swing.undo.CannotUndoException;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Session;

public class MediaLocationEdit extends SessionEditorUndoableEdit {

	private final String mediaLocation;
	
	private final String oldLocation;
	
	public MediaLocationEdit(SessionEditor editor, String mediaLocation, String oldLocation) {
		super(editor);
		this.mediaLocation = mediaLocation;
		this.oldLocation = oldLocation;
	}
	
	public String getMediaLocation() {
		return this.mediaLocation;
	}
	
	public String getOldLocation() {
		return this.oldLocation;
	}
	
	@Override
	public boolean canRedo() {
		// TODO Auto-generated method stub
		return super.canRedo();
	}

	@Override
	public boolean canUndo() {
		// TODO Auto-generated method stub
		return super.canUndo();
	}

	@Override
	public String getRedoPresentationName() {
		// TODO Auto-generated method stub
		return super.getRedoPresentationName();
	}

	@Override
	public String getUndoPresentationName() {
		// TODO Auto-generated method stub
		return super.getUndoPresentationName();
	}

	@Override
	public boolean isSignificant() {
		// TODO Auto-generated method stub
		return super.isSignificant();
	}

	@Override
	public void undo() throws CannotUndoException {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		session.setMediaLocation(getMediaLocation());
		
		queueEvent(EditorEventType.SESSION_MEDIA_CHANGED, editor.getUndoSupport(), getOldLocation());
	}

	@Override
	public void doIt() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		session.setMediaLocation(getMediaLocation());
		
		queueEvent(EditorEventType.SESSION_MEDIA_CHANGED, getSource(), getMediaLocation());
	}

}
