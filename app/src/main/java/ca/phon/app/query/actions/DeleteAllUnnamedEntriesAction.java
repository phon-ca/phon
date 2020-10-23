/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.query.actions;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import ca.phon.app.hooks.*;
import ca.phon.app.log.*;
import ca.phon.app.query.*;
import ca.phon.query.history.*;
import ca.phon.query.script.*;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.nativedialogs.*;

public class DeleteAllUnnamedEntriesAction extends HookableAction {

	private static final long serialVersionUID = -1201099472874912876L;

	private final QueryAndReportWizard queryWizard;
	
	private final QueryHistoryAndNameToolbar queryHistoryPanel;
	
	public DeleteAllUnnamedEntriesAction(QueryAndReportWizard wizard) {
		super();
		
		this.queryWizard = wizard;
		this.queryHistoryPanel = queryWizard.getQueryHistoryPanel();
		
		putValue(HookableAction.NAME, "Clear query history");
		putValue(HookableAction.SHORT_DESCRIPTION, "Delete all unnamed entries in query history (action cannot be undone)");
	}
	
	private void deleteEntries() {
		queryHistoryPanel.getQueryHistoryManager().removeAllUnnamedParamSets();
		
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
			props.setHeader("Clear Query History");
			props.setTitle("Clear Query History");
			props.setMessage("Delete all unnamed entries in query history? This action cannot be undone.");
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
