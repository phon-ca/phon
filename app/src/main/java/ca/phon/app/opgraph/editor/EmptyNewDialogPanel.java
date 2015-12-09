package ca.phon.app.opgraph.editor;

import java.awt.BorderLayout;

public class EmptyNewDialogPanel extends NewDialogPanel {

	private static final long serialVersionUID = 6434102209995368562L;

	public EmptyNewDialogPanel() {
		super();
		
		init();
	}
	
	protected void init() {
		setLayout(new BorderLayout());
		
	}
	
	@Override
	public String getTitle() {
		return "Empty";
	}

	@Override
	public OpgraphEditorModel createModel() {
		return new DefaultOpgraphEditorModel();
	}

}
