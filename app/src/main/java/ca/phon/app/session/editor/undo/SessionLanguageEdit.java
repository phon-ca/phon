package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;

public class SessionLanguageEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = 2753425148054627013L;

	private final String newLang;
	
	private String oldLang;
	
	public SessionLanguageEdit(SessionEditor editor, String newLang) {
		super(editor);
		this.newLang = newLang;
	}

	@Override
	public void undo() {
		getEditor().getSession().setLanguage(oldLang);
		
		final EditorEvent ee = new EditorEvent(EditorEventType.SESSION_LANG_CHANGED, getEditor().getUndoSupport(), oldLang);
		getEditor().getEventManager().queueEvent(ee);
	}
	
	@Override
	public void doIt() {
		oldLang = getEditor().getSession().getLanguage();
		getEditor().getSession().setLanguage(newLang);
		
		final EditorEvent ee = new EditorEvent(EditorEventType.SESSION_LANG_CHANGED, getSource(), newLang);
		getEditor().getEventManager().queueEvent(ee);
	}

}
