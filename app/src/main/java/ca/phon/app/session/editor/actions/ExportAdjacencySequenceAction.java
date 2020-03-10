package ca.phon.app.session.editor.actions;

import ca.phon.app.session.editor.SessionEditor;

public class ExportAdjacencySequenceAction extends ExportSegmentAction {

	private final static String CMD_NAME = "Export adjacency sequence...";
	
	private final static String SHORT_DESC = "";
	
	public ExportAdjacencySequenceAction(SessionEditor editor) {
		super(editor, SegmentType.CONVERSATION_PERIOD);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}
	
}
