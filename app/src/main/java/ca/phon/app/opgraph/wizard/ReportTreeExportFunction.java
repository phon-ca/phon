package ca.phon.app.opgraph.wizard;

import ca.phon.app.opgraph.report.tree.ReportTree;
import ca.phon.app.opgraph.report.tree.ReportTreeNode;

@FunctionalInterface
public interface ReportTreeExportFunction {

	public boolean exportReportTree(String folder, ReportTreeNode reportTree, boolean useIntegerForBoolean);

}
