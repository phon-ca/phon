package ca.phon.app.opgraph.report;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.Processor;
import ca.phon.app.opgraph.analysis.AnalysisWizard;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.WizardExtension;

public class ReportWizardExtension extends WizardExtension {

	public ReportWizardExtension(OpGraph graph) {
		super(graph);
	}

	@Override
	public NodeWizard createWizard(Processor processor) {
		return new ReportWizard(
				"Report : " + (getWizardTitle() != null ? getWizardTitle() : "Unknown"), processor, super.getGraph());
		
	}

}
