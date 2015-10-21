package ca.phon.app.opgraph.assessment;

import ca.phon.opgraph.editor.NewDialogPanel;
import ca.phon.opgraph.editor.OpgraphEditorModel;

public class AssessmentNewPanel extends NewDialogPanel {

	private static final long serialVersionUID = 8397066948261210026L;

	@Override
	public String getTitle() {
		return "Assessment";
	}

	@Override
	public OpgraphEditorModel createModel() {
		return new AssessmentOpGraphEditorModel();
	}

}
