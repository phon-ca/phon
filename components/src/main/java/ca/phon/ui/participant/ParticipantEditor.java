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
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.List;

public class ParticipantEditor extends JDialog {
	private static final long serialVersionUID = -878164228645403658L;
	
	/** GUI */
	private DialogHeader header;
	private JButton cancelButton;
	private JButton saveButton;
	
	private ParticipantPanel participantPanel;
	
	/** The participant */
	private Participant participant;
	private LocalDate sessionDate;
	
	private boolean wasCanceled = false;
	
	public static boolean editParticipant(JFrame parent, Participant part,
			List<Participant> otherParts) {
		ParticipantEditor editor = new ParticipantEditor(parent, part, LocalDate.now(), otherParts);
		editor.pack();
		editor.setLocationRelativeTo(parent);
		editor.setVisible(true);
		
		return editor.wasCanceled;
	}
	
	public static boolean editNewParticipant(JFrame parent, Participant part,
			List<Participant> otherParts) {
		ParticipantEditor editor = new ParticipantEditor(parent, part, LocalDate.now(), otherParts);
		editor.participantPanel.updateRoleId();
		editor.pack();
		editor.setLocationRelativeTo(parent);
		editor.setVisible(true);
		
		return editor.wasCanceled;
	}
	
	public static boolean editParticipant(JFrame parent, Participant part, LocalDate sessionDate,
			List<Participant> otherParts) {
		ParticipantEditor editor = new ParticipantEditor(parent, part, sessionDate, otherParts);
		editor.pack();
		editor.setLocationRelativeTo(parent);
		editor.setVisible(true);
		
		return editor.wasCanceled;
	}
	
	public static boolean editNewParticipant(JFrame parent, Participant part, LocalDate sessionDate,
			List<Participant> otherParts) {
		ParticipantEditor editor = new ParticipantEditor(parent, part, sessionDate, otherParts);
		editor.participantPanel.updateRoleId();
		editor.pack();
		editor.setLocationRelativeTo(parent);
		editor.setVisible(true);
		
		return editor.wasCanceled;
	}
	
	public boolean wasCanceled() {
		return wasCanceled;
	}
	
	/** Consctructor */
	protected ParticipantEditor(JFrame parent, Participant participant, LocalDate sessionDate, List<Participant> otherParts) {
		super(parent, "Edit Participant", true);
		
		this.participant = participant;
		this.sessionDate = sessionDate;
		
		init();
		participantPanel.setOtherParticipants(otherParts);
		participantPanel.setSessionDate(sessionDate);
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		// create display
		header = getHeader();
		
		cancelButton = getCancelButton();
		saveButton = getSaveButton();
		
		getRootPane().setDefaultButton(saveButton);
		
		participantPanel = new ParticipantPanel(participant);
		
		final JComponent btnGroup = ButtonBarBuilder.buildOkCancelBar(saveButton, cancelButton);
		
		add(header, BorderLayout.NORTH);
		add(participantPanel, BorderLayout.CENTER);
		add(btnGroup, BorderLayout.SOUTH);
	}

	private JButton getCancelButton() {
		if(cancelButton == null) {
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					wasCanceled = true;
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
			saveButton = new JButton("Ok");
			
			saveButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					wasCanceled = false;
					dispose();
				}
				
			});
		}
		return saveButton;
	}
	
}
