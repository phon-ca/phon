package ca.phon.app.opgraph.report;

import ca.gedge.opgraph.OpGraph;
import ca.phon.app.opgraph.nodes.query.QueryHistoryNode;
import ca.phon.opgraph.editor.NewDialogPanel;
import ca.phon.opgraph.editor.OpgraphEditorModel;

public class ReportNewPanel extends NewDialogPanel {

	private static final long serialVersionUID = 3486960052647071379L;

	@Override
	public String getTitle() {
		return "Report";
	}

	@Override
	public OpgraphEditorModel createModel() {
		final OpGraph graph = new OpGraph();
		
		final QueryHistoryNode historyNode = new QueryHistoryNode();
		graph.add(historyNode);
		
		return new ReportOpGraphEditorModel(graph);
	}

}
