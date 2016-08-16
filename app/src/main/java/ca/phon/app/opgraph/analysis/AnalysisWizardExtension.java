package ca.phon.app.opgraph.analysis;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.Processor;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.WizardExtension;

public class AnalysisWizardExtension extends WizardExtension {
	
	public AnalysisWizardExtension(OpGraph graph) {
		super(graph);
	}

	@Override
	public NodeWizard createWizard(Processor processor) {
		
		return new AnalysisWizard(
				"Analysis : " + (getWizardTitle() != null ? getWizardTitle() : "Unknown"), processor, super.getGraph());
	}

}
