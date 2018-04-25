package ca.phon.app.opgraph.nodes.report;

import ca.phon.app.opgraph.report.tree.*;
import ca.phon.opgraph.*;

@OpNodeInfo(name="Section Header", category="Report", description="New section header node", showInLibrary=true)
public class ReportSectionHeaderNode extends ReportSectionNode {

	public ReportSectionHeaderNode() {
		super();
	}

	@Override
	protected ReportTreeNode createReportSectionNode(OpContext context) {
		final String sectionName =
				(context.get(sectionNameInput) != null ? context.get(sectionNameInput).toString() : "");

		return new SectionHeaderNode(sectionName);
	}

}
