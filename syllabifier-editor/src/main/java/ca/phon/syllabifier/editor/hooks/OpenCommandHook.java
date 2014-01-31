package ca.phon.syllabifier.editor.hooks;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.commands.CommandHook;
import ca.gedge.opgraph.app.commands.Hook;
import ca.gedge.opgraph.app.commands.HookableCommand;
import ca.gedge.opgraph.app.commands.core.OpenCommand;
import ca.phon.syllabifier.editor.SyllabifierGraphEditorModel;
import ca.phon.syllabifier.opgraph.extensions.SyllabifierSettings;

@Hook(command=OpenCommand.class)
public class OpenCommandHook implements CommandHook {

	private static final Logger LOGGER = Logger
			.getLogger(OpenCommandHook.class.getName());
	
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
			
			// grab settings
			final OpGraph graph = syllabifierEditorModel.getDocument().getGraph();
			final SyllabifierSettings settings = graph.getExtension(SyllabifierSettings.class);
			if(settings != null) {
				syllabifierEditorModel.getSettingsPanel().loadSettings(settings);
			}
		} else {
			LOGGER.severe("Editor model is of incorrect type!");
		}
	}

}
