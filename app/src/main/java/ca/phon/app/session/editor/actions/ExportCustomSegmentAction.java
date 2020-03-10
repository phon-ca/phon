package ca.phon.app.session.editor.actions;

import ca.phon.app.session.editor.SessionEditor;

public class ExportCustomSegmentAction extends ExportSegmentAction {

	private final static String CMD_NAME = "Export custom segment...";
	
	private final static String SHORT_DESC = "";

	public ExportCustomSegmentAction(SessionEditor editor) {
		super(editor, SegmentType.CUSTOM);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}
	
}
