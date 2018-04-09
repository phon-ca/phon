package ca.phon.app.opgraph.report.tree;

/**
 * Report tree used in OpGraph documents useful for creating
 * report templates.
 * 
 */
public class ReportTree {

	private final ReportTreeNode root;
	
	public ReportTree() {
		this(new SectionHeaderNode(""));
	}
	
	public ReportTree(ReportTreeNode root) {
		this.root = root;
	}
	
	public ReportTreeNode getRoot() {
		return this.root;
	}
	
	public String getReportTemplate() {
		final StringBuffer buffer = new StringBuffer();
		
		for(ReportTreeNode reportNode:root.getChildren()) {
			append(buffer, reportNode);
		}
		
		return buffer.toString();
	}
		
	private void append(StringBuffer buffer, ReportTreeNode node) {
		buffer.append(node.getReportTemplateBlock());
		buffer.append("\n");
		for(ReportTreeNode reportNode:node.getChildren())
			append(buffer, reportNode);
	}
	
}
