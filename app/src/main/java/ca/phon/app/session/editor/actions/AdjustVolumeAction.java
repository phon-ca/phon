package ca.phon.app.session.editor.actions;

import ca.phon.app.session.editor.SessionEditor;

import java.awt.event.ActionEvent;

public class AdjustVolumeAction extends SessionEditorAction {

	public final static String TXT_FORMAT = "%d%%";

	public final static String DESC_FORMAT = "Set volume to %d%%";

	private final float volumeLevel;

	public AdjustVolumeAction(SessionEditor editor, float volumeLevel) {
		super(editor);

		this.volumeLevel = volumeLevel;

		int volSetting = (int)Math.round(volumeLevel * 100.0f);
		putValue(NAME, String.format(TXT_FORMAT, volSetting));
		putValue(SHORT_DESCRIPTION, String.format(DESC_FORMAT, volSetting));
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		getEditor().getMediaModel().getVolumeModel().setVolumeLevel(this.volumeLevel);
	}

}
