package ca.phon.opgraph.editor.actions.file;

import java.awt.event.ActionEvent;

import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;

/**
 * 
 */
public class NewAction extends OpgraphEditorAction {

	private static final long serialVersionUID = -6534242210546640918L;
	
	private final static String TXT = "New...";
	
	private final static String DESC = "New graph";
	
	public NewAction(OpgraphEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
	}

}
