/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.session.editor.view.syllabification_and_alignment.actions;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.syllabification_and_alignment.SyllabificationAlignmentEditorView;
import ca.phon.app.session.editor.view.syllabification_and_alignment.SyllabificationSettingsPanel;
import ca.phon.session.SyllabifierInfo;
import ca.phon.session.SystemTierType;
import ca.phon.ui.decorations.DialogHeader;

public class SyllabificationSettingsCommand extends SyllabificationAlignmentCommand {
	
	private static final long serialVersionUID = -5741712114218933203L;

	private final static String TEXT = "Syllabifier Settings";

	private final static String DESC = "Edit syllabifier settings...";
	
	public SyllabificationSettingsCommand(SessionEditor editor,
			SyllabificationAlignmentEditorView view) {
		super(editor, view);
		
		putValue(NAME, TEXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final JDialog settingsDialog = new JDialog(getEditor());
		settingsDialog.setModal(true);
		
		settingsDialog.setLayout(new BorderLayout());
		final DialogHeader header = new DialogHeader("Syllabifier settings", "Select syllabifier for IPA tiers.");
		settingsDialog.add(header, BorderLayout.NORTH);
		
		final SyllabifierInfo info = getEditor().getSession().getExtension(SyllabifierInfo.class);
		final SyllabificationSettingsPanel settingsPanel = new SyllabificationSettingsPanel(info);
		settingsDialog.add(settingsPanel, BorderLayout.CENTER);
		
		final AtomicBoolean wasCanceled = new AtomicBoolean(true);
		final JPanel btmPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		final JButton okBtn = new JButton("Ok");
		okBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				wasCanceled.getAndSet(false);
				settingsDialog.setVisible(false);
			}
			
		});
		btmPanel.add(okBtn);
		settingsDialog.getRootPane().setDefaultButton(okBtn);
		
		settingsDialog.add(btmPanel, BorderLayout.SOUTH);
		settingsDialog.pack();
		settingsDialog.setLocationRelativeTo(getView());
		settingsDialog.setVisible(true);
		
		// wait
		
		if(!wasCanceled.get()) {
			info.setSyllabifierLanguageForTier(SystemTierType.IPATarget.getName(), settingsPanel.getSelectedTargetSyllabifier());
			info.setSyllabifierLanguageForTier(SystemTierType.IPAActual.getName(), settingsPanel.getSelectedActualSyllabifier());
			
			info.saveInfo(getEditor().getSession());
		}
	}

}
