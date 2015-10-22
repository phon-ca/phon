package ca.phon.app.opgraph.assessment;

import java.util.ArrayList;
import java.util.List;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.nodes.reflect.ObjectNode;
import ca.phon.opgraph.editor.NewDialogPanel;
import ca.phon.opgraph.editor.OpgraphEditorModel;
import ca.phon.project.Project;
import ca.phon.session.Session;

public class AssessmentNewPanel extends NewDialogPanel {

	private static final long serialVersionUID = 8397066948261210026L;

	@Override
	public String getTitle() {
		return "Assessment";
	}

	@Override
	public OpgraphEditorModel createModel() {
		final OpGraph graph = new OpGraph();
		
		final ObjectNode projectNode = new ObjectNode(Project.class);
		projectNode.setContextKey("_project");
		graph.add(projectNode);
		
		final ObjectNode sessionListNode = new ObjectNode(Session[].class);
		sessionListNode.setName("Selected Sessions");
		sessionListNode.setContextKey("_selectedSessions");
		graph.add(sessionListNode);
		
		return new AssessmentOpGraphEditorModel(graph);
	}

}
