package ca.phon.app.session.editor.actions;

import ca.phon.app.session.editor.*;

public class ExportSpeechTurnAction extends ExportSegmentAction {

	private final static String CMD_NAME = "Export current speaker turn...";
	
	private final static String SHORT_DESC = "";
	
	public ExportSpeechTurnAction(SessionEditor editor) {
		super(editor, SegmentType.SPEAKER_TURN);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}
	
}
