package ca.phon.app.query.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.SwingUtilities;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.LogUtil;
import ca.phon.app.query.QueryAndReportWizard;
import ca.phon.app.query.QueryHistoryPanel;
import ca.phon.query.history.QueryHistoryManager;
import ca.phon.query.script.QueryScript;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;

public class DeleteQueryHistoryAction extends HookableAction {
	
	private static final long serialVersionUID = 5717800064526334465L;

	private final QueryAndReportWizard queryWizard;
	
	private final QueryHistoryPanel queryHistoryPanel;

	public DeleteQueryHistoryAction(QueryAndReportWizard wizard) {
		super();
		
		this.queryWizard = wizard;
		this.queryHistoryPanel = queryWizard.getQueryHistoryPanel();
		
		putValue(HookableAction.NAME, "Delete all entries in query history");
		putValue(HookableAction.SHORT_DESCRIPTION, "Delete all entries in query history (action cannot be undone)");
	}
	
	private void deleteEntries() {
		queryHistoryPanel.getQueryHistoryManager().removeAll();
		
		try {
			QueryHistoryManager.save(queryHistoryPanel.getQueryHistoryManager(), (QueryScript)queryHistoryPanel.getScriptPanel().getScript());
		} catch (IOException e) {
			Toolkit.getDefaultToolkit().beep();
			LogUtil.severe(e);
		}
	
		queryWizard.resetQueryParameters(new PhonActionEvent(new ActionEvent(this, -1, "reset")));
		queryHistoryPanel.updateLabelFromCurrentHash();
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		if(queryHistoryPanel.getCurrentIndex() >= 0) {
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setParentWindow(CommonModuleFrame.getCurrentFrame());
			props.setHeader("Delete Entries in Query History");
			props.setTitle("Delete All Entries in Query History");
			props.setMessage("Delete all entries in query history? This action cannot be undone.");
			props.setOptions(MessageDialogProperties.okCancelOptions);
			props.setRunAsync(true);
			props.setListener( (l) -> {
				if(l.getDialogResult() == 0) {
					SwingUtilities.invokeLater( this::deleteEntries );
				}
			});
			
			NativeDialogs.showMessageDialog(props);
		}
	}
	
}
