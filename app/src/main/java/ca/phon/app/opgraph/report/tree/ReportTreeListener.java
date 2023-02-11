package ca.phon.app.opgraph.report.tree;

public interface ReportTreeListener {

    public void reportNodeAdded(ReportTreeNode parent, int index, ReportTreeNode node);

    public void reportNodeRemoved(ReportTreeNode parent, int index, ReportTreeNode node);

}
