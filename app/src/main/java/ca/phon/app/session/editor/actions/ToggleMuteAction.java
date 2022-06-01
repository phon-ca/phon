package ca.phon.app.session.editor.actions;

import ca.phon.app.session.editor.SessionEditor;

import java.awt.event.ActionEvent;

public class ToggleMuteAction extends SessionEditorAction {

	public final static String TXT = "Mute";

	public final static String DESC = "Toggle mute";

	public ToggleMuteAction(SessionEditor editor) {
		super(editor);

		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(SELECTED_KEY, Boolean.valueOf(editor.getMediaModel().getVolumeModel().isMuted()));
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		getEditor().getMediaModel().getVolumeModel().setMuted(!getEditor().getMediaModel().getVolumeModel().isMuted());
	}

}
