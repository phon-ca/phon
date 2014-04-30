package ca.phon.app.session.editor.view.syllabification_and_alignment;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.SessionEditorUndoableEdit;
import ca.phon.ipa.IPATranscript;
import ca.phon.syllable.SyllableConstituentType;

public class ScTypeEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = -4921206917410378793L;

	private final IPATranscript transcript;
	
	private final int index;
	
	private final SyllableConstituentType scType;
	
	private SyllableConstituentType prevScType;
	
	public ScTypeEdit(SessionEditor editor, IPATranscript transcript, int index, SyllableConstituentType scType) {
		super(editor);
		this.transcript = transcript;
		this.index = index;
		this.scType = scType;
	}
	
	@Override
	public void undo() {
		if(prevScType != null && index >= 0 && index < transcript.length()) {
			transcript.elementAt(index).setScType(prevScType);
		
			final EditorEvent ee = new EditorEvent(SyllabificationAlignmentEditorView.SC_EDIT, getEditor().getUndoSupport(), transcript);
			getEditor().getEventManager().queueEvent(ee);
		}
	}
	
	@Override
	public void doIt() {
		if(index >= 0 && index < transcript.length()) {
			prevScType = transcript.elementAt(index).getScType();
			transcript.elementAt(index).setScType(scType);
		
			final EditorEvent ee = new EditorEvent(SyllabificationAlignmentEditorView.SC_EDIT, getSource(), transcript);
			getEditor().getEventManager().queueEvent(ee);
		}
	}

}
