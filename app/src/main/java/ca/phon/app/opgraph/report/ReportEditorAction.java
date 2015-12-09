package ca.phon.app.opgraph.report;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.editor.OpgraphEditorEP;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;

/**
 * Open the node editor with a new Report model.
 *
 */
public class ReportEditorAction extends HookableAction {
	
	private static final long serialVersionUID = -3297019664686256298L;

	private final static Logger LOGGER = Logger.getLogger(ReportEditorAction.class.getName());

	private final static String TXT = "Node editor...";
	
	private final static String DESC = "Open node editor to create new report";
	
	public ReportEditorAction() {
		super();
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final ReportOpGraphEditorModel model = new ReportOpGraphEditorModel();
		final Map<String, Object> args = new HashMap<>();
		args.put(OpgraphEditorEP.OPGRAPH_MODEL_KEY, model);
		try {
			PluginEntryPointRunner.executePlugin(OpgraphEditorEP.EP_NAME, args);
		} catch (PluginException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

}
