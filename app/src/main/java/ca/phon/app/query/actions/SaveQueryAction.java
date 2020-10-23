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
import ca.phon.query.history.*;
import ca.phon.query.script.*;
import ca.phon.script.*;
import ca.phon.script.params.history.*;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.decorations.*;
import ca.phon.ui.layout.*;
import ca.phon.ui.nativedialogs.*;
import ca.phon.ui.text.*;
import ca.phon.ui.toast.*;
import ca.phon.util.icons.*;

/**
 * Save (or name) query param settings in user history.
 * 
 */
public class SaveQueryAction extends HookableAction {

	private QueryHistoryManager stockQueries;
	
	private QueryHistoryManager queryHistory;
	
	private QueryScript queryScript;
	
	public SaveQueryAction(QueryHistoryManager stockQueries, QueryHistoryManager queryHistory, QueryScript script) {
		super();
		
		this.stockQueries = stockQueries;
		this.queryHistory = queryHistory;
		this.queryScript = script;
		
		final ImageIcon saveIcon = IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL);
		putValue(NAME, "Save query...");
		putValue(SMALL_ICON, saveIcon);
		putValue(SHORT_DESCRIPTION, "Save current query parameters");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		QueryNameDialog dialog = new QueryNameDialog();
		dialog.setModal(false);
		dialog.setPreferredSize(new Dimension(550, 200));
		dialog.pack();
		
		dialog.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
		dialog.setVisible(true);
	}
	
	public class QueryNameDialog extends JDialog {
		
		private PromptedTextField queryNameField;
		
		public QueryNameDialog() {
			super();
			
			init();
		}
		
		private void init() {
			setLayout(new BorderLayout());
			
			DialogHeader header = new DialogHeader("Name Query", 
					"<html>Save current query parameters. These settings will be available from the 'Query name' combo box at the top of query forms.</html>");
			add(header, BorderLayout.NORTH);
			
			JPanel centerPanel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.fill = GridBagConstraints.NONE;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.gridx = 1;
			gbc.gridy = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.insets = new Insets(0, 10, 0, 0);
			centerPanel.add(new JLabel("Query Name:"), gbc);
						
			gbc.gridx++;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			gbc.insets = new Insets(0, 5, 0, 10);
			queryNameField = new PromptedTextField("Enter query name (no extension)");
			queryNameField.setColumns(30);
			centerPanel.add(queryNameField, gbc);
			
			DefaultTextCompleterModel textCompleterModel = new DefaultTextCompleterModel();
			stockQueries.getNamedParamSets().forEach( (ps) -> textCompleterModel.addCompletion(ps.getName(), ps.getName()) );
			queryHistory.getNamedParamSets().forEach( (ps) -> textCompleterModel.addCompletion(ps.getName(), ps.getName()) );
			TextCompleter tc = new TextCompleter(textCompleterModel);
//			tc.setUseDataForCompletion(true);
			tc.install(queryNameField);
			
			gbc.gridy++;
			JLabel lbl = new JLabel("(leave empty to unset query name)");
			lbl.setFont(lbl.getFont().deriveFont(10.0f));
			centerPanel.add(lbl, gbc);
			
			try {
				ParamSetType paramSet = queryHistory.getParamSet(queryScript);
				if(paramSet != null && paramSet.getName() != null && paramSet.getName().length() > 0) {
					queryNameField.setText(paramSet.getName());
					
					gbc.gridy = 0;
					centerPanel.add(new JLabel(String.format("Renaming query '%s'", paramSet.getName())), gbc);
				}
			} catch (PhonScriptException e) {
				LogUtil.warning(e);
			}
			
			
			add(centerPanel, BorderLayout.CENTER);
			
			PhonUIAction closeAct = new PhonUIAction(this, "onClose");
			closeAct.putValue(PhonUIAction.NAME, "Cancel");
			closeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Close without saving");
			JButton closeBtn = new JButton(closeAct);
			
			PhonUIAction saveAct = new PhonUIAction(this, "onSave");
			saveAct.putValue(PhonUIAction.NAME, "Save");
			saveAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save query and close");
			JButton saveBtn = new JButton(saveAct);
			add(ButtonBarBuilder.buildOkCancelBar(saveBtn, closeBtn), BorderLayout.SOUTH);
			getRootPane().setDefaultButton(saveBtn);
		}
		
		public void onSave() {
			String queryName = queryNameField.getText().trim();
			if(queryName.length() > 0) {
				if(stockQueries.getParamSetByName(queryName) != null) {
					// can't rename stock query
					Toolkit.getDefaultToolkit().beep();
					ToastFactory.makeToast("A stock query with that name already exists").start(queryNameField);
					return;
				} else if(queryHistory.getParamSetByName(queryName) != null) {
					MessageDialogProperties props = new MessageDialogProperties();
					props.setParentWindow(this);
					props.setRunAsync(false);
					props.setTitle("Rename query");
					props.setHeader("Rename query");
					props.setMessage("A query with this name already exists? Rename query using these settings?");
					props.setOptions(new String[] {"Rename query", "Cancel"});
					
					int retVal = NativeDialogs.showMessageDialog(props);
					if(retVal == 1) return;
					
					// remove old query name
					queryHistory.getParamSetByName(queryName).setName(null);
				}
				try {
					queryHistory.nameParamSet(queryName, queryScript);
				} catch (PhonScriptException e) {
					LogUtil.warning(e);
				}
			} else {
				try {
					if(queryHistory.getParamSet(queryScript) != null)
						queryHistory.nameParamSet(null, queryScript);
				} catch (PhonScriptException e) {
					LogUtil.warning(e);
				}
			}
			try {
				QueryHistoryManager.save(queryHistory, queryScript);
			} catch (IOException e) {
				LogUtil.warning(e);
			}
			onClose();
		}
		
		public void onClose() {
			setVisible(false);
		}
		
	}
	
}
