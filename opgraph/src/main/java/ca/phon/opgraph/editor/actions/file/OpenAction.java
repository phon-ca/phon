package ca.phon.opgraph.editor.actions.file;

import java.awt.event.ActionEvent;

import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;

public class OpenAction extends OpgraphEditorAction {

	private static final long serialVersionUID = 1416397464535529114L;
	
	private final static String TXT = "Open...";
	
	private final static String DESC = "Open graph";

	public OpenAction(OpgraphEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
	}

}
