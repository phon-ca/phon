package ca.phon.app.query.actions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.LogUtil;
import ca.phon.query.history.QueryHistoryManager;
import ca.phon.query.script.QueryScript;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.history.ParamSetType;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.text.PromptedTextField;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

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
		dialog.setModal(true);
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
			
			DialogHeader header = new DialogHeader("Save Query", 
					"<html>Save current query parameters. These settings will be available in the 'Named Queries' combox at the top of query forms.</html>");
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
			centerPanel.add(new JLabel("Query Name:"), gbc);
						
			gbc.gridx++;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			queryNameField = new PromptedTextField("Enter query name (no extension)");
			queryNameField.setColumns(30);
			centerPanel.add(queryNameField, gbc);
			
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
			closeAct.putValue(PhonUIAction.NAME, "Close");
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
				if(stockQueries.getParamSetByName(queryName) != null
						|| queryHistory.getParamSetByName(queryName) != null) {
					Toolkit.getDefaultToolkit().beep();
					ToastFactory.makeToast("A query with that name already exists").start(queryNameField);
					return;
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
