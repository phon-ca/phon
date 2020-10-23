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
import ca.phon.script.params.history.*;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.nativedialogs.*;

public class DeleteQueryHistoryEntryAction extends HookableAction {
	
	private static final long serialVersionUID = 5294422861119394086L;

	private final QueryAndReportWizard queryWizard;
	
	private final QueryHistoryAndNameToolbar queryHistoryPanel;
	
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
