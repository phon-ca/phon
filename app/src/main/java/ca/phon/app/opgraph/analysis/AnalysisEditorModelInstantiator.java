package ca.phon.app.opgraph.analysis;

import java.util.ArrayList;

import javax.swing.JComponent;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.app.extensions.Note;
import ca.gedge.opgraph.app.extensions.Notes;
import ca.gedge.opgraph.extensions.NodeMetadata;
import ca.gedge.opgraph.nodes.reflect.ObjectNode;
import ca.phon.app.opgraph.editor.EditorModelInstantiator;
import ca.phon.app.opgraph.editor.EditorModelInstantiator.EditorModelInstantiatorMenuInfo;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.project.Project;

@EditorModelInstantiatorMenuInfo(
		name="Analysis",
		tooltip = "New analysis document...",
		modelType = AnalysisOpGraphEditorModel.class)
public class AnalysisEditorModelInstantiator implements EditorModelInstantiator, IPluginExtensionPoint<EditorModelInstantiator> {

	@Override
	public AnalysisOpGraphEditorModel createModel(OpGraph graph) {
		if(graph.getVertices().size() == 0) {
			setupDefaultGraph(graph);
		}
		return new AnalysisOpGraphEditorModel(graph);
	}
	
	private void setupDefaultGraph(OpGraph graph) {
		final ObjectNode projectNode = new ObjectNode(Project.class);
		projectNode.setContextKey("_project");
		final NodeMetadata projectMeta = new NodeMetadata();
		projectMeta.setLocation(51, 52);
		projectNode.putExtension(NodeMetadata.class, projectMeta);
		graph.add(projectNode);
		
		
		final ObjectNode sessionListNode = new ObjectNode(ArrayList.class);
		sessionListNode.setContextKey("_selectedSessions");
		sessionListNode.setName("Selected Sessions");
		final NodeMetadata sessionMeta = new NodeMetadata();
		sessionMeta.setLocation(127, 298);
		sessionListNode.putExtension(NodeMetadata.class, sessionMeta);
		graph.add(sessionListNode);
		
		final ObjectNode participantListNode = new ObjectNode(ArrayList.class);
		participantListNode.setContextKey("_selectedParticipants");
		participantListNode.setName("Selected Participants");
		final NodeMetadata partMeta = new NodeMetadata();
		partMeta.setLocation(146, 418);
		participantListNode.putExtension(NodeMetadata.class, partMeta);
		graph.add(participantListNode);
		
		final Notes notes = new Notes();
		final Note note = new Note("Analysis Inputs", "");
		final JComponent noteComp = note.getExtension(JComponent.class);
		noteComp.setBounds(20, 10, 330, 540);
		
		notes.add(note);
	}

	@Override
	public Class<?> getExtensionType() {
		return EditorModelInstantiator.class;
	}

	@Override
	public IPluginExtensionFactory<EditorModelInstantiator> getFactory() {
		return (Object... args) -> this;
	}

}
