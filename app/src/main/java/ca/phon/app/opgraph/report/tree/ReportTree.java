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
	
}
