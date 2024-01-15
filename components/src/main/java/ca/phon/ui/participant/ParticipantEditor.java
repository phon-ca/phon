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
package ca.phon.ui.participant;

import ca.phon.session.Participant;
import ca.phon.ui.FlatButton;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.List;

public class ParticipantEditor extends JDialog {

	/** GUI */
	private DialogHeader header;
	private JButton cancelButton;
	private JButton saveButton;
	
	private ParticipantPanel participantPanel;

	private JButton anonymizeBtn;
	
	/** The participant */
	private final Participant participant;

	private final ParticipantEditorListener listener;
	
	public static void editParticipant(JFrame parent, Participant part,
			List<Participant> otherParts, ParticipantEditorListener listener) {
		ParticipantEditor editor = new ParticipantEditor(parent, part, LocalDate.now(), otherParts, listener);
		editor.pack();
		editor.setLocationRelativeTo(parent);
		editor.setVisible(true);
	}
	
	public static void editNewParticipant(JFrame parent, Participant part,
			List<Participant> otherParts, ParticipantEditorListener listener) {
		ParticipantEditor editor = new ParticipantEditor(parent, part, LocalDate.now(), otherParts, listener);
		editor.participantPanel.updateRoleId();
		editor.pack();
		editor.setLocationRelativeTo(parent);
		editor.setVisible(true);
	}
	
	public static void editParticipant(JFrame parent, Participant part, LocalDate sessionDate,
			List<Participant> otherParts, ParticipantEditorListener listener) {
		ParticipantEditor editor = new ParticipantEditor(parent, part, sessionDate, otherParts, listener);
		editor.pack();
		editor.setLocationRelativeTo(parent);
		editor.setVisible(true);
	}
	
	public static void editNewParticipant(JFrame parent, Participant part, LocalDate sessionDate,
			List<Participant> otherParts, ParticipantEditorListener listener) {
		ParticipantEditor editor = new ParticipantEditor(parent, part, sessionDate, otherParts, listener);
		editor.participantPanel.updateRoleId();
		editor.pack();
		editor.setLocationRelativeTo(parent);
		editor.setVisible(true);
	}
	
	/** Consctructor */
	protected ParticipantEditor(JFrame parent, Participant participant, LocalDate sessionDate, List<Participant> otherParts, ParticipantEditorListener listener) {
		super(parent, "Edit Participant", false);
		
		this.participant = participant;
		this.listener = listener;
		
		init(sessionDate, otherParts);
	}

	private void init(LocalDate sessionDate, List<Participant> otherParts) {
		setLayout(new BorderLayout());

		// create display
		header = getHeader();

		cancelButton = getCancelButton();
		saveButton = getSaveButton();

		getRootPane().setDefaultButton(saveButton);

		participantPanel = new ParticipantPanel(participant);
		participantPanel.setSessionDate(sessionDate);
		participantPanel.setOtherParticipants(otherParts);
		participantPanel.addPropertyChangeListener("preferredSize", (e) -> {
			pack();
			revalidate();
			repaint();
		});

		final PhonUIAction anonymizeAct = PhonUIAction.runnable(participantPanel::onAnonymize);
		anonymizeAct.putValue(PhonUIAction.NAME, "Anonymize");
		anonymizeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Remove all optional information");
		anonymizeAct.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
		anonymizeAct.putValue(FlatButton.ICON_NAME_PROP, "privacy_tip");
		anonymizeAct.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
		anonymizeBtn = new FlatButton(anonymizeAct);

		final JComponent btnGroup = ButtonBarBuilder.buildOkCancelBar(saveButton, cancelButton, anonymizeBtn);

		add(header, BorderLayout.NORTH);
		add(participantPanel, BorderLayout.CENTER);
		add(btnGroup, BorderLayout.SOUTH);
	}

	private JButton getCancelButton() {
		if(cancelButton == null) {
			cancelButton = new FlatButton(IconManager.GoogleMaterialDesignIconsFontName, "close", IconSize.MEDIUM);
			cancelButton.setText("Cancel");
			cancelButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					listener.editorClosed(true);
					dispose();
				}
				
			});
		}
		return cancelButton;
	}

	private DialogHeader getHeader() {
		if(header == null) {
			header = new DialogHeader("Edit Participant", 
					participant.getName());
		}
		return header;
	}
	
	private JButton getSaveButton() {
		if(saveButton == null) {
			saveButton = new FlatButton(IconManager.GoogleMaterialDesignIconsFontName, "done", IconSize.MEDIUM);
			saveButton.setText("Ok");
			saveButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					listener.editorClosed(false);
					dispose();
				}
				
			});
		}
		return saveButton;
	}

	public interface ParticipantEditorListener {

		void editorClosed(boolean wasCanceled);

	}

}
