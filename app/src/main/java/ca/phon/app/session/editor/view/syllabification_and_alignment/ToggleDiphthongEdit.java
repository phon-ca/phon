package ca.phon.app.session.editor.view.syllabification_and_alignment;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.SessionEditorUndoableEdit;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.syllable.SyllabificationInfo;

public class ToggleDiphthongEdit extends SessionEditorUndoableEdit {
	
	private static final long serialVersionUID = -4921206917410378793L;

	private final IPATranscript transcript;
	
	private final int index;
	
	public ToggleDiphthongEdit(SessionEditor editor, IPATranscript transcript, int index) {
		super(editor);
		this.transcript = transcript;
		this.index = index;
	}
	
	@Override
	public void undo() {
		super.redo();
	}

	@Override
	public void doIt() {
		if(index >= 0 && index < transcript.length()) {
			final IPAElement ele = transcript.elementAt(index);
			final SyllabificationInfo info = ele.getExtension(SyllabificationInfo.class);
			info.setDiphthongMember(!info.isDiphthongMember());
			
			final EditorEvent ee = new EditorEvent(SyllabificationAlignmentEditorView.SC_EDIT, getSource(), transcript);
			getEditor().getEventManager().queueEvent(ee);
		}
	}

}
