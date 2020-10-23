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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import ca.phon.project.*;
import ca.phon.query.history.*;
import ca.phon.query.script.*;
import ca.phon.ui.*;
import ca.phon.ui.decorations.*;
import ca.phon.ui.layout.*;

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
