package ca.phon.app.opgraph.report.tree;

public interface ReportTreeListener {

    public void reportNodeAdded(ReportTreeNode parent, ReportTreeNode node);

    public void reportNodeRemoved(ReportTreeNode parent, ReportTreeNode node);

}
