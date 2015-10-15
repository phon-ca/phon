package ca.phon.opgraph.editor;

import javax.swing.JPanel;

public abstract class NewDialogPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	public abstract String getTitle();
	
	public abstract OpgraphEditorModel createModel();
	
}
