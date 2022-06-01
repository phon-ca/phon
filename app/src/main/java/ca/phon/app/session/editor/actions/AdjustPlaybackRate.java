package ca.phon.app.session.editor.actions;

import ca.phon.app.session.editor.SessionEditor;

import java.awt.event.ActionEvent;

public class AdjustPlaybackRate extends SessionEditorAction {

	public final static String TXT_FORMAT = "%d%%";

	public final static String DESC_FORMAT = "Set playback rate to %d%%";

	private final float playbackRate;

	public AdjustPlaybackRate(SessionEditor editor, float playbackRate) {
		super(editor);

		this.playbackRate = playbackRate;

		int rate = (int)Math.round(playbackRate * 100.0f);
		putValue(NAME, String.format(TXT_FORMAT, rate));
		putValue(SHORT_DESCRIPTION, String.format(DESC_FORMAT, rate));
		putValue(SELECTED_KEY, editor.getMediaModel().getPlaybackRate() == playbackRate);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		getEditor().getMediaModel().setPlaybackRate(this.playbackRate);
	}

}
