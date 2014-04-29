package ca.phon.app.session.editor.undo;

import javax.swing.undo.CannotUndoException;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Session;

public class MediaLocationEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = 7934882593502356426L;

	private final String mediaLocation;
	
	private String oldLocation;
	
	public MediaLocationEdit(SessionEditor editor, String mediaLocation) {
		super(editor);
		this.mediaLocation = mediaLocation;
	}
	
	public String getMediaLocation() {
		return this.mediaLocation;
	}
	
	public String getOldLocation() {
		return this.oldLocation;
	}

	@Override
	public void undo() throws CannotUndoException {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		session.setMediaLocation(getOldLocation());
		
		queueEvent(EditorEventType.SESSION_MEDIA_CHANGED, editor.getUndoSupport(), getOldLocation());
	}

	@Override
	public void doIt() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		oldLocation = session.getMediaLocation();
		
		session.setMediaLocation(getMediaLocation());
		
		queueEvent(EditorEventType.SESSION_MEDIA_CHANGED, getSource(), getMediaLocation());
	}

}
