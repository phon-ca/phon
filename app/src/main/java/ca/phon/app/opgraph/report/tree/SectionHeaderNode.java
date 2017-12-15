package ca.phon.app.opgraph.report.tree;

/**
 * Node used as parents to sections.
 * 
 */
public class SectionHeaderNode extends ReportTreeNode {

	public SectionHeaderNode(String title) {
		super(title);
	}

	@Override
	public String getReportTemplateBlock() {
		return String.format("#h%d(\"%s\")\n", getLevel(), getTitle());
	}

}
