package ca.phon.app.opgraph.nodes.report;

import ca.phon.app.opgraph.report.tree.ReportTreeNode;
import ca.phon.opgraph.InputField;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.OutputField;
import ca.phon.opgraph.exceptions.ProcessingException;

public abstract class ReportSectionNode extends OpNode {

	public final InputField sectionNameInput = 
			new InputField("sectionName", "Title of element in ToC", true, true, String.class);
	
	public final InputField parentNodeInput = 
			new InputField("parent", "Parent report tree node", true, true, ReportTreeNode.class);
	
	public final InputField addToBeginningInput =
			new InputField("addToBeginning", "Add new section node to start of child list", true, true, Boolean.class);
	
	public final OutputField parentNodeOutput =
			new OutputField("parent", "Parent report tree node", true, ReportTreeNode.class);
	
	public final OutputField sectionNodeOutput = 
			new OutputField("section", "Section report node created", true, ReportTreeNode.class);
	
	public ReportSectionNode() {
		super();
		
		putField(sectionNameInput);
		putField(parentNodeInput);
		putField(addToBeginningInput);
		
		putField(parentNodeOutput);
		putField(sectionNodeOutput);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final ReportTreeNode parentNode = 
				(context.containsKey(parentNodeInput) ? (ReportTreeNode)context.get(parentNodeInput) : null);
		final Boolean addToBeginning = 
				(context.get(addToBeginningInput) != null ? (Boolean)context.get(addToBeginningInput) : false);
		
		final ReportTreeNode sectionNode = createReportSectionNode(context);
		
		if(parentNode != null) {
			if(addToBeginning)
				parentNode.add(0, sectionNode);
			else
				parentNode.add(sectionNode);
		}
				
		context.put(parentNodeOutput, parentNode);
		context.put(sectionNodeOutput, sectionNode);
	}

	protected abstract ReportTreeNode createReportSectionNode(OpContext context);

}
