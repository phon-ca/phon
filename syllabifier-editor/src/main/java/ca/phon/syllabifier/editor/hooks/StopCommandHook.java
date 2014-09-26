package ca.phon.syllabifier.editor.hooks;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.commands.CommandHook;
import ca.gedge.opgraph.app.commands.Hook;
import ca.gedge.opgraph.app.commands.HookableCommand;
import ca.gedge.opgraph.app.commands.debug.StopCommand;
import ca.phon.ipa.IPATranscript;
import ca.phon.syllabifier.editor.SyllabifierGraphEditorModel;

/**
 * Resets syllabificiation display when debugging ends.
 *
 */
@Hook(command=StopCommand.class)
public class StopCommandHook implements CommandHook {

	private static final Logger LOGGER = Logger
			.getLogger(StopCommandHook.class.getName());
	
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
			syllabifierEditorModel.getSyllabificationDisplay().setTranscript(new IPATranscript());
		} else {
			LOGGER.severe("Editor model is of incorrect type!");
		}
	}

	
	
}
