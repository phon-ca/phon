package ca.phon.app.opgraph.assessment;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.nodes.reflect.ObjectNode;
import ca.phon.opgraph.editor.NewDialogPanel;
import ca.phon.opgraph.editor.OpgraphEditorModel;
import ca.phon.project.Project;
import ca.phon.session.SessionPath;

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
		
		final ObjectNode sessionListNode = new ObjectNode(SessionPath[].class);
		sessionListNode.setContextKey("_selectedSessions");
		sessionListNode.setName("Selected Sessions");
		graph.add(sessionListNode);
		
		return new AssessmentOpGraphEditorModel(graph);
	}

}
