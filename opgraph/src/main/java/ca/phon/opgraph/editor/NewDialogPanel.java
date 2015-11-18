package ca.phon.opgraph.editor;

import java.awt.Dimension;

import javax.swing.JPanel;

public abstract class NewDialogPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	public NewDialogPanel() {
		super();
	}
	
	public abstract String getTitle();
	
	public abstract OpgraphEditorModel createModel();
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(500, 400);
	}
	
}
