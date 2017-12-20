package ca.phon.app.opgraph.nodes.report;

import ca.gedge.opgraph.*;
import ca.phon.app.opgraph.report.tree.ReportTreeNode;

@OpNodeInfo(name="Add Report Section", category="Report Tree", description="Add section to report tree", showInLibrary=true)
public class AddReportSectionNode extends ReportSectionNode {
	
	private InputField existingSectionInput = 
			new InputField("child", "Child node to be added", false, true, ReportTreeNode.class);

	public AddReportSectionNode() {
		super();
		
		putField(existingSectionInput);
	}

	@Override
	protected ReportTreeNode createReportSectionNode(OpContext context) {
		return (ReportTreeNode)context.get(existingSectionInput);
	}

}
