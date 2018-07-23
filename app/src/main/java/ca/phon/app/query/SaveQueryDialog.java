/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;

import ca.phon.app.opgraph.nodes.query.QueryHistoryNode;
import ca.phon.project.Project;
import ca.phon.query.history.QueryHistoryManager;
import ca.phon.query.script.QueryScript;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;

/**
 * Dialog for saving canned queries.
 */
public class SaveQueryDialog extends JDialog {
	
	private static final long serialVersionUID = -4839782859373658825L;

	private CommonModuleFrame parentFrame;

	private QueryScript queryScript;
	
	private QueryHistoryManager stockQueries;
	
	private QueryHistoryManager queryHistoryManager;

	private SaveQueryForm form;
	private JButton saveBtn;
	private JButton cancelBtn;

	public SaveQueryDialog(CommonModuleFrame parent, QueryScript script, QueryHistoryManager stockQueries, QueryHistoryManager queryHistoryManager) {
		super(parent);
		this.parentFrame = parent;
		this.queryScript = script;
		this.stockQueries = stockQueries;
		this.queryHistoryManager = queryHistoryManager;
		
		super.setTitle("Save Query");
		super.setResizable(false);

		init();
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension retVal = super.getPreferredSize();

		retVal.width = 500;

		return retVal;
	}
	
	public Project getProject() {
		return this.parentFrame.getExtension(Project.class);
	}
	
	public SaveQueryForm getForm() {
		return this.form;
	}

	private void init() {
		form = new SaveQueryForm(getProject(), queryScript, stockQueries, queryHistoryManager);
		
		saveBtn = new JButton("Save");
		saveBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				save();
			}
		});
		super.getRootPane().setDefaultButton(saveBtn);

		cancelBtn = new JButton("Cancel");
		cancelBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
				dispose();
			}
			
		});

		final DialogHeader header =
				new DialogHeader("Save Query", "");
		JComponent btnBar = ButtonBarBuilder.buildOkCancelBar(saveBtn, cancelBtn);

		setLayout(new BorderLayout());
		add(header, BorderLayout.NORTH);
		add(form, BorderLayout.CENTER);
		add(btnBar, BorderLayout.SOUTH);
	}

	private void save() {
		if(form.checkForm()) {
			form.save();
			setVisible(false);
			dispose();
		}
	}

	public void showDialog() {
		pack();
		super.setLocationRelativeTo(parentFrame);
		setVisible(true);
	}
	
}
