package ca.phon.syllabifier.editor.hooks;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.commands.CommandHook;
import ca.gedge.opgraph.app.commands.Hook;
import ca.gedge.opgraph.app.commands.HookableCommand;
import ca.gedge.opgraph.app.commands.core.SaveCommand;
import ca.phon.syllabifier.editor.SyllabifierGraphEditorModel;
import ca.phon.syllabifier.editor.SyllabifierSettingsPanel;
import ca.phon.syllabifier.opgraph.extensions.SyllabifierSettings;

@Hook(command=SaveCommand.class)
public class SaveCommandHook implements CommandHook {

	private static final Logger LOGGER = Logger
			.getLogger(SaveCommandHook.class.getName());
	
	@Override
	public boolean startCommand(HookableCommand command, ActionEvent evt) {
		final GraphEditorModel editorModel = GraphEditorModel.getActiveEditorModel();
		if(editorModel instanceof SyllabifierGraphEditorModel) {
			final SyllabifierGraphEditorModel syllabifierEditorModel = 
					(SyllabifierGraphEditorModel)editorModel;
			
			// grab syllabifier settings
			final SyllabifierSettingsPanel settingsPanel = syllabifierEditorModel.getSettingsPanel();
			final SyllabifierSettings settings = settingsPanel.getSyllabifierSettings();
			syllabifierEditorModel.getDocument().getGraph().putExtension(SyllabifierSettings.class, settings);
		} else {
			LOGGER.severe("Editor model is of incorrect type!");
			return true;
		}
		return false;
	}

	@Override
	public void endCommand(HookableCommand command, ActionEvent evt) {
	}
	
}
