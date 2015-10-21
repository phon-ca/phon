package ca.phon.app.opgraph.assessment;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.phon.opgraph.editor.DefaultOpgraphEditorModel;

public class AssessmentOpGraphEditorModel extends DefaultOpgraphEditorModel {

	public AssessmentOpGraphEditorModel() {
		super();
	}

	public AssessmentOpGraphEditorModel(OpGraph opgraph) {
		super(opgraph);
	}
	
	@Override
	public String getDefaultFolder() {
		return super.getDefaultFolder();
	}

	@Override
	public boolean validate() {
		return super.validate();
	}

	@Override
	public void setupContext(OpContext context) {
		super.setupContext(context);
	}
	
}
