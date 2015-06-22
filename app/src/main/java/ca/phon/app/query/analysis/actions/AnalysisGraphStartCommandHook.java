package ca.phon.app.query.analysis.actions;

import java.awt.event.ActionEvent;

import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.commands.CommandHook;
import ca.gedge.opgraph.app.commands.Hook;
import ca.gedge.opgraph.app.commands.HookableCommand;
import ca.gedge.opgraph.app.commands.debug.RunCommand;
import ca.phon.app.query.analysis.AnalysisEditorModel;

@Hook(command=RunCommand.class)
public class AnalysisGraphStartCommandHook implements CommandHook {

	@Override
	public boolean startCommand(HookableCommand command, ActionEvent evt) {
		final GraphEditorModel activeModel = GraphEditorModel.getActiveEditorModel();
		if(activeModel instanceof AnalysisEditorModel) {
			final AnalysisEditorModel model = (AnalysisEditorModel)activeModel;
		
			final GraphDocument document = model.getDocument();
			if(document != null) {
				Processor context = document.getProcessingContext();
				if(context == null) {
					context = new Processor(document.getGraph());
					context.getContext().put("_project", model.getProject());
					document.setProcessingContext(context);
				}

				if(context.hasNext()) {
					context.stepAll();
					document.updateDebugState(context);
				}
			}
		}
		
		return true;
	}

	@Override
	public void endCommand(HookableCommand command, ActionEvent evt) {
		
	}

}
