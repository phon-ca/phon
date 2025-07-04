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
package ca.phon.app.session.editor.view.syllabificationAlignment.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.syllabificationAlignment.SyllabificationAlignmentEditorView;
import ca.phon.app.session.editor.view.syllabificationAlignment.SyllabificationSettingsPanel;
import ca.phon.app.session.editor.view.transcript.extensions.SyllabifierChangeEdit;
import ca.phon.session.SystemTierType;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.util.Language;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SyllabificationSettingsCommand extends SyllabificationAlignmentCommand {
	
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
		final JDialog settingsDialog = new JDialog(CommonModuleFrame.getCurrentFrame());
		settingsDialog.setModal(true);
		
		settingsDialog.setLayout(new BorderLayout());
		final DialogHeader header = new DialogHeader("Syllabifier settings", "Select syllabifier for IPA tiers.");
		settingsDialog.add(header, BorderLayout.NORTH);
		
		final SyllabificationSettingsPanel settingsPanel = new SyllabificationSettingsPanel(getSession());
		settingsDialog.add(settingsPanel, BorderLayout.CENTER);
		
		final JButton okBtn = new JButton("Ok");
		okBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				getUndoSupport().beginUpdate("Change syllabifier settings");
				// set syllabifiers for session based on selected syllabifiers
				final Language ipaTargetLang = settingsPanel.getSelectedTargetSyllabifier();
				final SyllabifierChangeEdit ipaTargetEdit = new SyllabifierChangeEdit(getSession(), getEditor().getEventManager(),
						SystemTierType.IPATarget.getName(), ipaTargetLang.toString());
				getUndoSupport().postEdit(ipaTargetEdit);

				final Language ipaActualLang = settingsPanel.getSelectedActualSyllabifier();
				final SyllabifierChangeEdit ipaActualEdit = new SyllabifierChangeEdit(getSession(), getEditor().getEventManager(),
						SystemTierType.IPAActual.getName(), ipaActualLang.toString());
				getUndoSupport().postEdit(ipaActualEdit);
				getUndoSupport().endUpdate();

				settingsDialog.setVisible(false);
				settingsDialog.dispose();
			}
			
		});
		final JButton cancelBtn = new JButton("Cancel");
		cancelBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				settingsDialog.setVisible(false);
				settingsDialog.dispose();
			}

		});
		final JComponent btnPanel = ButtonBarBuilder.buildOkCancelBar(okBtn, cancelBtn);
		settingsDialog.getRootPane().setDefaultButton(okBtn);
		
		settingsDialog.add(btnPanel, BorderLayout.SOUTH);
		settingsDialog.pack();
		settingsDialog.setLocationRelativeTo(getView());
		settingsDialog.setVisible(true);
	}

}
