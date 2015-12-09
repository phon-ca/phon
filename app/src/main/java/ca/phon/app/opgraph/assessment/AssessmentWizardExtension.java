package ca.phon.app.opgraph.assessment;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.Processor;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.WizardExtension;

public class AssessmentWizardExtension extends WizardExtension {
	
	public AssessmentWizardExtension(OpGraph graph) {
		super(graph);
	}

	@Override
	public NodeWizard createWizard(Processor processor) {
		return new AssessmentWizard("Assessment", processor, super.getGraph());
	}

	
	
}
