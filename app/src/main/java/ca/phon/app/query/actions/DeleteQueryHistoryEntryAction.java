package ca.phon.app.query.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.SwingUtilities;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.query.QueryAndReportWizard;
import ca.phon.app.query.QueryHistoryPanel;
import ca.phon.logging.LogUtil;
import ca.phon.query.history.QueryHistoryManager;
import ca.phon.query.script.QueryScript;
import ca.phon.script.params.history.ParamSetType;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;

public class DeleteQueryHistoryEntryAction extends HookableAction {
	
	private static final long serialVersionUID = 5294422861119394086L;

	private final QueryAndReportWizard queryWizard;
	
	private final QueryHistoryPanel queryHistoryPanel;
	
	public DeleteQueryHistoryEntryAction(QueryAndReportWizard wizard) {
		super();
		
		this.queryWizard = wizard;
		this.queryHistoryPanel = queryWizard.getQueryHistoryPanel();
		
		putValue(HookableAction.NAME, "Delete current entry in query history");
		putValue(HookableAction.SHORT_DESCRIPTION, "Delete current entry in query history (action cannot be undone)");
	}
	
	private void deleteCurrentEntry() {
		final ParamSetType toDelete = queryHistoryPanel.currentQuery();
		if(toDelete != null) {
			queryHistoryPanel.getQueryHistoryManager().removeParamSet(toDelete.getHash());
			
			try {
				QueryHistoryManager.save(queryHistoryPanel.getQueryHistoryManager(), (QueryScript)queryHistoryPanel.getScriptPanel().getScript());
			} catch (IOException e) {
				Toolkit.getDefaultToolkit().beep();
				LogUtil.severe(e);
			}
			
			// save
			int cIdx = queryHistoryPanel.getCurrentIndex();
			if(cIdx >= queryHistoryPanel.getQueryHistoryManager().size()) {
				cIdx = queryHistoryPanel.getQueryHistoryManager().size()-1;
			}
			if(cIdx >= 0 && cIdx < queryHistoryPanel.getQueryHistoryManager().size())
				queryHistoryPanel.gotoIndex(cIdx);
			else {
				queryWizard.resetQueryParameters(new PhonActionEvent(new ActionEvent(this, -1, "reset")));
				queryHistoryPanel.updateLabelFromCurrentHash();
			}
		}
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		if(queryHistoryPanel.getCurrentIndex() >= 0) {
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setParentWindow(CommonModuleFrame.getCurrentFrame());
			props.setHeader("Delete Entry in Query History");
			props.setTitle("Delete Entry in Query History");
			props.setMessage("Delete current entry in query history? This action cannot be undone.");
			props.setOptions(MessageDialogProperties.okCancelOptions);
			props.setRunAsync(true);
			props.setListener( (l) -> {
				if(l.getDialogResult() == 0) {
					SwingUtilities.invokeLater( this::deleteCurrentEntry );
				}
			});
			
			NativeDialogs.showMessageDialog(props);
		}
	}

}
