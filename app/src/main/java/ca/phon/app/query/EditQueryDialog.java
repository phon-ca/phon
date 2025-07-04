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
package ca.phon.app.query;

import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.nativedialogs.OSInfo;
import ca.phon.util.icons.*;

import javax.swing.*;
import java.awt.*;

/**
 * Custom dialog for editing query information.
 *
 */
public class EditQueryDialog extends JDialog {
	
	private static final long serialVersionUID = 7982672156818550768L;

	public enum ReturnStatus {
		OK,
		CANCEL;
	}
	private ReturnStatus status = ReturnStatus.CANCEL;
	
	/**
	 * buttons
	 */
	private JButton okButton;
	private JButton cancelButton;
	
	private EditQueryPanel panel;
	
	private DialogHeader header;
	
	private Project project;
	
	private Query query;
	
	/*
	 * Cached values
	 */
	private boolean cStarred = false;
	private String cName = "";
	private String cComments = "";
	
	public EditQueryDialog(Project project, Query query) {
		super();
		super.setTitle("Query Details");
		
		this.project = project;
		this.query = query;
		
		cStarred = query.isStarred();
		cName = query.getName();
		cComments = query.getComments();
		
		init();
	}
	
	private void init() {
		// set window icon
		if(!OSInfo.isMacOs()) {
			ImageIcon icon = 
				IconManager.getInstance().getIcon("apps/database-phon", IconSize.SMALL);
			if(icon != null) {
				super.setIconImage(icon.getImage());
			}
		}
		
		setLayout(new BorderLayout());
		
		final PhonUIAction<Void> okAction = PhonUIAction.runnable(this::onOk);
		okAction.putValue(PhonUIAction.NAME, "Ok");
		okButton = new JButton(okAction);
		getRootPane().setDefaultButton(okButton);
		
		final PhonUIAction<Void> cancelAction = PhonUIAction.runnable(this::onCancel);
		cancelAction.putValue(PhonUIAction.NAME, "Cancel");
		cancelButton = new JButton(cancelAction);
		
		final JComponent buttonPanel = ButtonBarBuilder.buildOkCancelBar(okButton, cancelButton);
		
		header = new DialogHeader("Query Details", "Enter query name and comments.");
		
		panel = new EditQueryPanel(query);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		add(header, BorderLayout.NORTH);
		add(panel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	public ReturnStatus showModal() {
		super.setModal(true);
		super.pack();
		panel.getQueryNameField().requestFocus();
		panel.getQueryNameField().selectAll();
		super.setVisible(true);
		
		return status;
	}
	
	/**
	 * Called when ok button is pressed.
	 */
	public void onOk() {
		status = ReturnStatus.OK;
		super.setVisible(false);
		super.dispose();
	}
	
	void resetValues() {
		query.setName(cName);
		query.setComments(cComments);
		query.setStarred(cStarred);
	}
	
	/**
	 * Called when cancel button is pressed.
	 * By default will close and dispose window.
	 */
	public void onCancel() {
		resetValues();
		super.setVisible(false);
		super.dispose();
	}
	

	public boolean hasChanged() {
		return hasNameChanged() || hasStarredChanged() || hasCommentsChanged();
	}
	
	public boolean hasNameChanged() {
		return !cName.equals(query.getName());
	}
	
	public boolean hasStarredChanged() {
		return cStarred != query.isStarred();
	}
	
	public boolean hasCommentsChanged() {
		return !cComments.equals(query.getComments());
	}
	
}
