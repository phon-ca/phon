package ca.phon.syllabifier.editor.hooks;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.commands.CommandHook;
import ca.gedge.opgraph.app.commands.Hook;
import ca.gedge.opgraph.app.commands.HookableCommand;
import ca.gedge.opgraph.app.commands.core.NewCommand;
import ca.gedge.opgraph.app.extensions.NodeMetadata;
import ca.phon.syllabifier.editor.SyllabifierGraphEditorModel;
import ca.phon.syllabifier.opgraph.nodes.IPASourceNode;

/**
 * Ensures that syllabifier settings are reset and a new
 * {@link IPASourceNode} is added to the new graph.
 */
@Hook(command=NewCommand.class)
public class NewCommandHook implements CommandHook {
	
	private static final Logger LOGGER = Logger
			.getLogger(NewCommandHook.class.getName());

	@Override
	public boolean startCommand(HookableCommand command, ActionEvent evt) {
		return false;
	}

	@Override
	public void endCommand(HookableCommand command, ActionEvent evt) {
		final GraphEditorModel editorModel = GraphEditorModel.getActiveEditorModel();
		if(editorModel instanceof SyllabifierGraphEditorModel) {
			final SyllabifierGraphEditorModel syllabifierEditorModel = 
					(SyllabifierGraphEditorModel)editorModel;
			syllabifierEditorModel.getSettingsPanel().reset();
			
			final IPASourceNode ipaSourceNode = new IPASourceNode();
			final NodeMetadata ipaMeta = new NodeMetadata(10, 10);
			ipaSourceNode.putExtension(NodeMetadata.class, ipaMeta);
			syllabifierEditorModel.getDocument().getGraph().add(ipaSourceNode);
		} else {
			LOGGER.severe("Editor model is of incorrect type!");
		}
	}
	
}
