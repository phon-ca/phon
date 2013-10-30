/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.app.session.participant;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.joda.time.DateTime;
import org.joda.time.JodaTimePermission;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import ca.phon.session.AgeFormatter;
import ca.phon.session.Participant;
import ca.phon.session.ParticipantRole;
import ca.phon.session.Sex;
import ca.phon.ui.DateTimeDocument;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.util.PhonDuration;
import ca.phon.util.PhonDurationFormat;

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

 
public class ParticipantEditor extends JDialog {
	private static final long serialVersionUID = -878164228645403658L;
	
	/** GUI */
	private JTextField nameField;
	private JRadioButton maleButton;
	private JRadioButton femaleButton;
	private JTextField bdayField;
	private JLabel ageLabel;
	private JTextField educationField;
	private JTextField groupField;
	private JTextField languageField;
//	private JTextField roleField;
	private JComboBox roleField;
	
	private DialogHeader header;
	private JButton cancelButton;
	private JButton saveButton;
	
	/** The participant */
	private Participant participant;
	private DateTime sessionDate;
	
	private boolean wasCanceled = false;
	
//	/**
//	 * Displays the editor for the given participant and session
//	 * date (for calculating age.)
//	 * 
//	 * @param participant
//	 * @param sessionDate
//	 * @return <CODE>false</CODE> if the dialog was cancled, <CODE>true</CODE>
//	 * otherwise.
//	 */
//	public static boolean showEditor(IParticipant participant, Calendar sessionDate) {
//		ParticipantEditor editor = new ParticipantEditor(participant, sessionDate);
//		editor.hasFinished = false;
//		
//		editor.setShowInWindowMenu(false);
//		
//		editor.pack();
//		editor.setVisible(true);
//		
//		while(!editor.hasFinished) {
//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {}
//		}
//		
//		return editor.wasCanceled;
//	}
	
	public static boolean editParticipant(JFrame parent, Participant part) {
		ParticipantEditor editor = new ParticipantEditor(parent, part, DateTime.now());
		editor.ageLabel.setVisible(false);
		editor.pack();
		editor.setLocationRelativeTo(parent);
		editor.setVisible(true);
		
		return editor.wasCanceled;
	}
	
	public static boolean editParticipant(JFrame parent, Participant part, DateTime sessionDate) {
		ParticipantEditor editor = new ParticipantEditor(parent, part, sessionDate);
		editor.pack();
		editor.setLocationRelativeTo(parent);
		editor.setVisible(true);
		
		return editor.wasCanceled;
	}
	
	public boolean wasCanceled() {
		return wasCanceled;
	}
	
	/** Consctructor */
	protected ParticipantEditor(JFrame parent, Participant participant, DateTime sessionDate) {
		super(parent, "Edit Participant", true);
		
		this.participant = participant;
		this.sessionDate = sessionDate;
		
		init();
	}
	
	private void init() {
		// layout
		FormLayout layout = new FormLayout(
				"5dlu, right:pref, 3dlu, fill:200px:grow, 5dlu, right:pref, 3dlu, fill:200px:grow, 5dlu",
				"5dlu, fill:70px, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 5dlu");
		this.setLayout(layout);
		
		// create display
		nameField = getNameField();
		bdayField = getBdayField();
		ageLabel = getAgeLabel();
		educationField = getEducationField();
		groupField = getGroupField();
		languageField = getLanguageField();
		roleField = getRoleField();
		header = getHeader();
		
		ButtonGroup group = new ButtonGroup();
		group.add((maleButton = getMaleButton()));
		group.add((femaleButton = getFemaleButton()));
		
		cancelButton = getCancelButton();
		saveButton = getSaveButton();
		
		getRootPane().setDefaultButton(saveButton);
		
		JComponent sexComp = new JPanel();
		sexComp.setLayout(new FlowLayout(FlowLayout.LEFT));
		sexComp.add(maleButton);
		sexComp.add(femaleButton);
		
		JComponent btnGroup = ButtonBarFactory.buildOKCancelBar(saveButton, cancelButton);
		
		// add components
		CellConstraints cc = new CellConstraints();
		
		this.add(header, cc.xyw(2, 2, 7));
		
		this.add(new JLabel("Name:" ), cc.xy(2, 4));
		this.add(nameField, cc.xy(4, 4));
		
		this.add(new JLabel("Gender:" ), cc.xy(6, 4));
		this.add(sexComp, cc.xy(8, 4));
		
		this.add(new JLabel("Birthday:"), cc.xy(2, 6));
		this.add(bdayField, cc.xy(4, 6));
		
		this.add(new JLabel("Age:"), cc.xy(6, 6));
		this.add(ageLabel, cc.xy(8, 6));
		
		this.add(new JLabel("Language:"), cc.xy(2, 8));
		this.add(languageField, cc.xy(4, 8));
		
		this.add(new JLabel("Education:"), cc.xy(6, 8));
		this.add(educationField, cc.xy(8, 8));
		
		this.add(new JLabel("Group:"), cc.xy(2, 10));
		this.add(groupField, cc.xy(4, 10));
		
		this.add(new JLabel("Role:"), cc.xy(6, 10));
		this.add(roleField, cc.xy(8, 10));
		
		this.add(btnGroup, cc.xyw(2, 12, 7));
	}

	private JLabel getAgeLabel() {
		if(ageLabel == null) {
			ageLabel = new JLabel();
			
//			PhonDurationFormat ageFormat = new PhonDurationFormat(PhonDurationFormat.PHON_FORMAT);
			if(participant.getAge(sessionDate) != null)
				ageLabel.setText(AgeFormatter.ageToString(participant.getAge(sessionDate)));
			else
				ageLabel.setText(AgeFormatter.ageToString(new Period()));
		}
		return ageLabel;
	}

	private JTextField getBdayField() {
		if(bdayField == null) {
			bdayField = new JTextField();
			
			bdayField.addFocusListener(new FocusListener() {

				@Override
				public void focusGained(FocusEvent e) {
				}

				@Override
				public void focusLost(FocusEvent e) {
//					// make sure the format is correct!
//					// ppl can put in crazy dates, allow them, but fix it
//					final JTextField textField = getBdayField();
//					final DateTimeDocument dateTimeDoc = (DateTimeDocument)textField.getDocument();
//					final DateTime dateTime = dateTimeDoc.getDateTime();
				}
				
			});
			
			DateTime bday = participant.getBirthDate();
			if(bday == null)
				bday = DateTime.now();
			
			bdayField.setDocument(new DateTimeDocument(bday));
			
			bdayField.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void changedUpdate(DocumentEvent e) {
					updateAge();
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					updateAge();
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					updateAge();
				}
				
				private void updateAge() {
//					 update age string as birthdate is changed
					final JTextField textField = getBdayField();
					final DateTimeDocument dateTimeDoc = (DateTimeDocument)textField.getDocument();
					final DateTime dateTime = dateTimeDoc.getDateTime();
					
					final Period age = new Period(dateTime, sessionDate);
//					final PeriodFormatter ageFormatter = 
//							new PeriodFormatterBuilder().printZeroAlways()
//								.minimumPrintedDigits(2).appendYears()
//								.appendLiteral(";")
//								.minimumPrintedDigits(2).appendMonths()
//								.appendLiteral(".")
//								.minimumPrintedDigits(2).appendDays()
//								.toFormatter();
					final String ageString = AgeFormatter.ageToString(age);
					getAgeLabel().setText(ageString);
					
//					Calendar calendar = 
//						((CalendarDocument)getBdayField().getDocument()).getCalendar();
//					PhonDuration age = 
//						PhonDuration.getDuration(calendar, sessionDate);
//					if(age != null) {
//						PhonDuration dur = null;
//						if(age.valid())
//							dur = age;
//						else
//							dur = new PhonDuration();
//						PhonDurationFormat ageFormat = 
//							new PhonDurationFormat(PhonDurationFormat.PHON_FORMAT);
//						getAgeLabel().setText(ageFormat.format(dur));
//					}
//					getAgeLabel().repaint();
				}
			});
			
			
			
			
		}
		return bdayField;
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

	private JTextField getEducationField() {
		if(educationField == null) {
			educationField = new JTextField();
			
			if(participant.getEducation() != null)
				educationField.setText(participant.getEducation());
		}
		return educationField;
	}

	private JRadioButton getFemaleButton() {
		if(femaleButton == null) {
			femaleButton = new JRadioButton("F");
			
			femaleButton.setSelected(
					participant.getSex() == Sex.FEMALE);
		}
		return femaleButton;
	}

	private JTextField getGroupField() {
		if(groupField == null) {
			groupField = new JTextField();
			
			if(participant.getGroup() != null)
				groupField.setText(participant.getGroup());
		}
		return groupField;
	}

	private DialogHeader getHeader() {
		if(header == null) {
			header = new DialogHeader("Edit Participant", 
					participant.getName());
		}
		return header;
	}

	private JTextField getLanguageField() {
		if(languageField == null) {
			languageField = new JTextField();
			
			if(participant.getLanguage() != null)
				languageField.setText(participant.getLanguage());
		}
		return languageField;
	}

	private JRadioButton getMaleButton() {
		if(maleButton == null) {
			maleButton = new JRadioButton("M");
			
			maleButton.setSelected(
					participant.getSex() == Sex.MALE || participant.getSex() == null);
		}
		return maleButton;
	}

	private JTextField getNameField() {
		if(nameField == null) {
			nameField = new JTextField();
			
			nameField.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void changedUpdate(DocumentEvent e) {
					updateHeader();
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					updateHeader();
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					updateHeader();
				}
				
				private void updateHeader() {
					if(header != null)
						header.setDescText(getNameField().getText());
				}
			});
			
			if(participant.getName() != null)
				nameField.setText(participant.getName());
		}
		return nameField;
	}

	private JComboBox getRoleField() {
		if(roleField == null) {
			roleField = new JComboBox(ParticipantRole.values());
			
			if(participant.getRole() != null)
				roleField.setSelectedItem(participant.getRole());
			else
				roleField.setSelectedItem(ParticipantRole.TARGET_CHILD);  // default
		}
		return roleField;
	}

	private JButton getSaveButton() {
		if(saveButton == null) {
			saveButton = new JButton("Ok");
			
			saveButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					wasCanceled = false;
					saveValues();
					dispose();
				}
				
			});
		}
		return saveButton;
	}
	
	private void saveValues() {
		if(participant == null) return;
		
		participant.setName(getNameField().getText());
		
		final JTextField textField = getBdayField();
		final DateTimeDocument dateTimeDoc = (DateTimeDocument)textField.getDocument();
		final DateTime bday = dateTimeDoc.getDateTime();
		participant.setBirthDate(bday);
		
		Sex selectedSex = 
			(getMaleButton().isSelected() ? Sex.MALE : Sex.FEMALE);
		participant.setSex(selectedSex);
		
//		PhonDurationFormat ageFormat = new PhonDurationFormat(PhonDurationFormat.PHON_FORMAT);
//		PhonDuration age = new PhonDuration();
//		try {
//			age = 
//				(PhonDuration)ageFormat.parseObject(getAgeLabel().getText());
//			if(!age.isNegative())
//				participant.setAge(age);
//		} catch (ParseException e) {
//			PhonLogger.warning(getClass(), e.getMessage());
//		}

		participant.setEducation(getEducationField().getText());
		participant.setGroup(getGroupField().getText());
		participant.setLanguage(getLanguageField().getText());
		ParticipantRole selectedRole = (ParticipantRole)getRoleField().getSelectedItem();
		selectedRole = (selectedRole == null ? ParticipantRole.TARGET_CHILD : selectedRole);
		participant.setRole(selectedRole);
	}
}
