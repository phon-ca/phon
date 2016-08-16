package ca.phon.app.opgraph.report;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.Processor;
import ca.phon.app.opgraph.wizard.NodeWizard;

public class ReportWizard extends NodeWizard {

	private static final long serialVersionUID = 3616649077398530316L;

	public ReportWizard(String title, Processor processor, OpGraph graph) {
		super(title, processor, graph);
	}
	
}

