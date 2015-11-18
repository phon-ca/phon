package ca.phon.opgraph.editor.actions.view;

import java.awt.event.ActionEvent;

import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;

public class ToggleViewAction extends OpgraphEditorAction {

	private static final long serialVersionUID = 2681118382414781020L;

	public final static String DESC = "Toggle view";
	
	public ToggleViewAction(OpgraphEditor editor, String viewName) {
		super(editor);
		
		putValue(NAME, viewName);
		putValue(SHORT_DESCRIPTION, DESC + " " + viewName);
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		final String viewName = (String)getValue(NAME);
		if(getEditor().isViewVisible(viewName)) {
			getEditor().hideView(viewName);
		} else {
			getEditor().showView(viewName);
		}
	}

}
