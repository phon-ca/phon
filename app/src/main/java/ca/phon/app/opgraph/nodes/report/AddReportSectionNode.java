package ca.phon.app.opgraph.nodes.report;

import ca.gedge.opgraph.*;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.opgraph.report.tree.ReportTreeNode;

public class AddReportSectionNode extends OpNode {

	private InputField parentNodeInput = 
			new InputField("parent", "Parent report tree node", false, true, ReportTreeNode.class);
	
	private OutputField parentNodeOutput =
			new OutputField("parent", "Parent report tree node", true, ReportTreeNode.class);
	
	private OutputField sectionNodeOutput = 
			new OutputField("section", "Section report node created", true, ReportTreeNode.class);
	
	public AddReportSectionNode() {
		super();
		
		putField(parentNodeInput);
		
		putField(parentNodeOutput);
		putField(sectionNodeOutput);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		// TODO Auto-generated method stub
		
	}

}
