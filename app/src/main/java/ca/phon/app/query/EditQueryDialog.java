/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.query;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;

import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.nativedialogs.OSInfo;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

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
		
		final PhonUIAction okAction = new PhonUIAction(this, "onOk");
		okAction.putValue(PhonUIAction.NAME, "Ok");
		okButton = new JButton(okAction);
		getRootPane().setDefaultButton(okButton);
		
		final PhonUIAction cancelAction = new PhonUIAction(this, "onCancel");
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
