package ca.phon.app.opgraph.analysis;

import java.util.ArrayList;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.nodes.reflect.ObjectNode;
import ca.phon.app.opgraph.editor.NewDialogPanel;
import ca.phon.app.opgraph.editor.OpgraphEditorModel;
import ca.phon.project.Project;
import ca.phon.session.SessionPath;

public class AnalysisNewPanel extends NewDialogPanel {

	private static final long serialVersionUID = 8397066948261210026L;

	@Override
	public String getTitle() {
		return "Analysis";
	}

	@Override
	public OpgraphEditorModel createModel() {
		final OpGraph graph = new OpGraph();
		
		final ObjectNode projectNode = new ObjectNode(Project.class);
		projectNode.setContextKey("_project");
		graph.add(projectNode);
		
		final ObjectNode sessionListNode = new ObjectNode(ArrayList.class);
		sessionListNode.setContextKey("_selectedSessions");
		sessionListNode.setName("Selected Sessions");
		graph.add(sessionListNode);
		
		return new AnalysisOpGraphEditorModel(graph);
	}

}
