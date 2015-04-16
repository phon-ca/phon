/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.ui.participant;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.joda.time.DateTime;

import ca.phon.session.Participant;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;

public class ParticipantEditor extends JDialog {
	private static final long serialVersionUID = -878164228645403658L;
	
	/** GUI */
	private DialogHeader header;
	private JButton cancelButton;
	private JButton saveButton;
	
	private ParticipantPanel participantPanel;
	
	/** The participant */
	private Participant participant;
	private DateTime sessionDate;
	
	private boolean wasCanceled = false;
	
	public static boolean editParticipant(JFrame parent, Participant part,
			List<Participant> otherParts) {
		ParticipantEditor editor = new ParticipantEditor(parent, part, DateTime.now(), otherParts);
		editor.pack();
		editor.setLocationRelativeTo(parent);
		editor.setVisible(true);
		
		return editor.wasCanceled;
	}
	
	public static boolean editParticipant(JFrame parent, Participant part, DateTime sessionDate,
			List<Participant> otherParts) {
		ParticipantEditor editor = new ParticipantEditor(parent, part, sessionDate, otherParts);
		editor.pack();
		editor.setLocationRelativeTo(parent);
		editor.setVisible(true);
		
		return editor.wasCanceled;
	}
	
	public boolean wasCanceled() {
		return wasCanceled;
	}
	
	/** Consctructor */
	protected ParticipantEditor(JFrame parent, Participant participant, DateTime sessionDate, List<Participant> otherParts) {
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
