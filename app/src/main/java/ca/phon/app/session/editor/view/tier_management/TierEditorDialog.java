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
package ca.phon.app.session.editor.view.tier_management;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;

import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;

/**
 * Simple dialog that closes on OK or Cancel. Use showDialog() to display the
 * dialog and get the return value.
 */
public class TierEditorDialog extends JDialog {

	private static final long serialVersionUID = 1218564949424490169L;

	private DialogHeader header;

	private TierInfoEditor tierEditor;

	private JButton okButton;

	private JButton cancelButton;

	private boolean okPressed = false;

	public TierEditorDialog(boolean editMode) {
		super();

		if (editMode)
			super.setTitle("Edit Tier");
		else
			super.setTitle("New Tier");

		super.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		tierEditor = new TierInfoEditor(editMode);

		init();
	}

	private void init() {
		header = new DialogHeader(getTitle(), "");

		okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				okPressed = true;
				TierEditorDialog.this.setVisible(false);
			}

		});

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				okPressed = false;
				TierEditorDialog.this.setVisible(false);
			}

		});

		final JComponent btnPanel = ButtonBarBuilder.buildOkCancelBar(okButton, cancelButton);
		
		BorderLayout layout = new BorderLayout();
		setLayout(layout);

		add(header, BorderLayout.NORTH);
		add(tierEditor, BorderLayout.CENTER);
		add(btnPanel, BorderLayout.SOUTH);

		getRootPane().setDefaultButton(okButton);
	}

	public TierInfoEditor getTierEditor() {
		return tierEditor;
	}

	/**
	 * Displays dialog to user, closes when either button is pressed.
	 * 
	 * @return true if ok was pressed, false otherwise
	 */
	public boolean showDialog() {
		pack();
		Dimension size = getSize();

		// center dialog on screen
		Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();

		if (size.width == 0 && size.height == 0)
			size = getPreferredSize();

		int xPos = ss.width / 2 - (size.width / 2);
		int yPos = ss.height / 2 - (size.height / 2);

		setBounds(xPos, yPos, size.width, size.height);

		setVisible(true);

		// .. wait for dialog

		return okPressed;
	}

	/**
	 * If not modal, showDialog will always return false. Use this method to get
	 * the dialog result.
	 * 
	 * @return
	 */
	public boolean wasOkPressed() {
		return okPressed;
	}
}