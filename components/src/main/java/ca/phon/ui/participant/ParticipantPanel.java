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

import ca.phon.session.*;
import ca.phon.session.format.AgeFormatter;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.DropDownIcon;
import ca.phon.ui.PhonLoggerConsole;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.text.*;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import com.jgoodies.forms.layout.*;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.time.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * UI for editing participant information.
 *
 */
public class ParticipantPanel extends JPanel {

	private JComboBox<ParticipantRole> roleBox;

	private JTextField idField;

	private JLabel idWarningLbl;

	private JComboBox<Sex> sexBox;

	private JTextField nameField;
	private JTextField groupField;
	private JTextField sesField;
	private JTextField educationField;
	private LanguageField languageField;

	private JLabel bdayWarningLbl;

	private DatePicker bdayField;

	private FormatterTextField<Period> ageField;

	private JLabel ageWarninglbl;

	private LocalDate sessionDate;

	private List<Participant> otherParticipants;

	private JButton anonymizeBtn;

	private final Participant participant;

	public ParticipantPanel() {
		this(SessionFactory.newFactory().createParticipant());
	}

	public ParticipantPanel(Participant participant) {
		super();
		this.participant = participant;

		init();
	}

	private JLabel createFieldLabel(String text, String propName) {
		final JLabel retVal = new JLabel(text);
		retVal.setIcon(new DropDownIcon(new ImageIcon(), 0, SwingConstants.BOTTOM));
		retVal.setHorizontalTextPosition(SwingConstants.LEFT);
		retVal.addMouseListener(new FieldMenuListener(propName));
		retVal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return retVal;
	}

	private void init() {
		final ImageIcon warningIcn = IconManager.getInstance().getIcon("emblems/flag-red", IconSize.XSMALL);

		// setup form
		roleBox = new JComboBox<>(ParticipantRole.values());

		idField = new JTextField();
		idWarningLbl = new JLabel("");
		idWarningLbl.setIcon(warningIcn);
		idWarningLbl.setFont(idWarningLbl.getFont().deriveFont(10.0f));
		final JPanel idPanel = new JPanel(new VerticalLayout());
		idPanel.add(idField);
		idPanel.add(idWarningLbl);
		updateIdWarningLabel();

		sexBox = new JComboBox<>(Sex.values());
		sexBox.setSelectedItem(
				(participant.getSex() != null ? participant.getSex() : Sex.UNSPECIFIED));
		sexBox.setRenderer(new DefaultListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList<?> list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				final JLabel retVal = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
				final Sex sex = (Sex)value;

				retVal.setText(sex.getText());

				return retVal;
			}

		});

		final PhonUIAction anonymizeAct = PhonUIAction.runnable(this::onAnonymize);
		anonymizeAct.putValue(PhonUIAction.NAME, "Anonymize");
		anonymizeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Remove all optional information");
		anonymizeBtn = new JButton(anonymizeAct);

		int defCols = 20;
		nameField = new JTextField();
		nameField.setColumns(defCols);

		groupField = new JTextField();
		groupField.setColumns(defCols);

		sesField = new JTextField();
		sesField.setColumns(defCols);
		educationField = new JTextField();
		educationField.setColumns(defCols);
		languageField = new LanguageField();
		languageField.setColumns(defCols);

		bdayField = new DatePicker(sessionDate);

		bdayWarningLbl = new JLabel("Birthday is after specified session date");
		bdayWarningLbl.setIcon(warningIcn);
		bdayWarningLbl.setFont(bdayWarningLbl.getFont().deriveFont(10.0f));
		updateBirthdayWarningLabel();

		final JPanel bdayPanel = new JPanel(new VerticalLayout());
		bdayPanel.add(bdayField);
		bdayPanel.add(bdayWarningLbl);

		ageField = FormatterTextField.createTextField(Period.class);
		ageField.setPrompt("YY;MM.DD");
		ageField.setToolTipText("Enter age in format YY;MM.YY");
		ageField.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
				if(ageField.getText().length() > 0 &&
						!ageField.validateText()) {
					ToastFactory.makeToast("Age format: " + AgeFormatter.AGE_FORMAT).start(ageField);
					Toolkit.getDefaultToolkit().beep();
					bdayField.requestFocus();
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
			}

		});

		ageWarninglbl = new JLabel("Age does not match specified birthday");
		ageWarninglbl.setIcon(warningIcn);
		ageWarninglbl.setFont(ageWarninglbl.getFont().deriveFont(10.0f));
		updateAgeWarningLabel();

		final JPanel agePanel = new JPanel(new VerticalLayout());
		agePanel.add(ageField);
		agePanel.add(ageWarninglbl);

		// setup info

		if(participant.getRole() != null)
			roleBox.setSelectedItem(participant.getRole());
		if(participant.getId() != null) {
			idField.setText(participant.getId());
		}
		updateIdWarningLabel();
		if(participant.getName() != null)
			nameField.setText(participant.getName());
		if(participant.getGroup() != null)
			groupField.setText(participant.getGroup());
		if(participant.getSES() != null)
			sesField.setText(participant.getSES());
		if(participant.getLanguage() != null)
			languageField.setText(participant.getLanguage());
		if(participant.getEducation() != null)
			educationField.setText(participant.getEducation());

		if(participant.getBirthDate() != null) {
			bdayField.setDateTime(participant.getBirthDate());
		}

		if(participant.getAge(null) != null) {
			ageField.setValue(participant.getAge(null));
		}

		// setup listeners
		final Consumer<Participant> roleUpdater = (obj) -> {
			final ParticipantRole role = (ParticipantRole)roleBox.getSelectedItem();
			obj.setRole(role);
			idField.setText(getRoleId());
		};
		roleBox.addItemListener(new ItemUpdater(roleUpdater));

		final Consumer<Participant> idUpdater = (obj) -> {
			if(idField.getText().trim().length() > 0
				&& idField.getText().split("\\s").length == 1)
				obj.setId(idField.getText());
			updateIdWarningLabel();
		};
		idField.getDocument().addDocumentListener(new TextFieldUpdater(idUpdater));

		final Consumer<Participant> nameUpdater = (obj) -> {
			obj.setName(nameField.getText());
		};
		nameField.getDocument().addDocumentListener(new TextFieldUpdater(nameUpdater));

		final Consumer<Participant> langUpdater = (obj) -> {
			obj.setLanguage(languageField.getText());
		};
		languageField.getDocument().addDocumentListener(new TextFieldUpdater(langUpdater));

		final Consumer<Participant> groupUpdater = (obj) -> {
			obj.setGroup(groupField.getText());
		};
		groupField.getDocument().addDocumentListener(new TextFieldUpdater(groupUpdater));

		final Consumer<Participant> eduUpdater = (obj) -> {
			obj.setEducation(educationField.getText());
		};
		educationField.getDocument().addDocumentListener(new TextFieldUpdater(eduUpdater));

		final Consumer<Participant> sesUpdater = (obj) -> {
			obj.setSES(sesField.getText());
		};
		sesField.getDocument().addDocumentListener(new TextFieldUpdater(sesUpdater));

		final Consumer<Participant> sexUpdater = (obj) -> {
			obj.setSex((Sex)sexBox.getSelectedItem());
		};
		sexBox.addItemListener(new ItemUpdater(sexUpdater));

		final Consumer<Participant> bdayUpdater = (obj) -> {
			final LocalDate bday = bdayField.getDateTime();
			obj.setBirthDate(bday);
			if(obj.getAge(null) == null) {
				if(sessionDate != null && obj.getBirthDate() != null
					&& sessionDate.isAfter(obj.getBirthDate())) {
					final Period age = obj.getAge(sessionDate);
					ageField.setPrompt(AgeFormatter.ageToString(age));
					ageField.setKeepPrompt(true);
				} else {
					ageField.setPrompt("YY:MM.DD");
					ageField.setKeepPrompt(false);
				}
			}

			updateBirthdayWarningLabel();
			updateAgeWarningLabel();
		};
		bdayField.addPropertyChangeListener(DatePicker.DATETIME_PROP, new PropertyUpdater(bdayUpdater));
		bdayField.getTextField().addActionListener(new ActionUpdater(bdayUpdater));

		final Consumer<Participant> ageUpdater = (obj) -> {
			if(ageField.getText().trim().length() == 0) {
				obj.setAge(null);
			} else {
				final Period p = ageField.getValue();
				obj.setAge(p);
			}
			updateAgeWarningLabel();
		};
		ageField.getDocument().addDocumentListener(new TextFieldUpdater(ageUpdater));

		// ensure a role is selected!
		if(participant.getRole() == null) {
			roleBox.setSelectedItem(ParticipantRole.TARGET_CHILD);
		}

		final CellConstraints cc = new CellConstraints();
		final FormLayout reqLayout = new FormLayout(
				"right:pref, 3dlu, fill:pref:grow",
				"pref, pref, pref");
		final JPanel required = new JPanel(reqLayout);
		required.setBorder(BorderFactory.createTitledBorder("Required Information"));
		required.add(new JLabel("Role"), cc.xy(1,1));
		required.add(roleBox, cc.xy(3,1));
		required.add(createFieldLabel("Id", "id"), cc.xy(1, 3));
		required.add(idPanel, cc.xy(3, 3));

		final FormLayout optLayout = new FormLayout(
				"right:pref, 3dlu, fill:pref:grow, 5dlu, right:pref, 3dlu, fill:pref:grow",
				"pref, pref, pref, pref");
		final JPanel optional = new JPanel(optLayout);
		optional.setBorder(BorderFactory.createTitledBorder("Optional Information"));
		optional.add(createFieldLabel("Name", "name"), cc.xy(1, 1));
		optional.add(nameField, cc.xy(3, 1));
		optional.add(createFieldLabel("Sex", "sex"), cc.xy(1, 2));
		optional.add(sexBox, cc.xy(3, 2));
		optional.add(createFieldLabel("Birthday (YYYY-MM-DD)", "birthday"), cc.xy(1, 3));
		optional.add(bdayPanel, cc.xy(3, 3));
		optional.add(createFieldLabel("Age (" + AgeFormatter.AGE_FORMAT + ")", "age"), cc.xy(1, 4));
		optional.add(agePanel, cc.xy(3, 4));

		optional.add(createFieldLabel("Language", "language"), cc.xy(5, 1));
		optional.add(languageField, cc.xy(7, 1));
		optional.add(createFieldLabel("Group", "group"), cc.xy(5, 2));
		optional.add(groupField, cc.xy(7, 2));
		optional.add(createFieldLabel("Education", "education"), cc.xy(5, 3));
		optional.add(educationField, cc.xy(7, 3));
		optional.add(createFieldLabel("SES", "ses"), cc.xy(5, 4));
		optional.add(sesField, cc.xy(7, 4));

		setLayout(new VerticalLayout(5));
		add(required);
		add(optional);
		add(ButtonBarBuilder.buildOkBar(anonymizeBtn));
		add(new JSeparator(SwingConstants.HORIZONTAL));
	}

	private void updateIdWarningLabel() {
		if(idField.getText() == null || idField.getText().trim().length() == 0) {
			idWarningLbl.setText("Id cannot be empty");
			idWarningLbl.setVisible(true);
		} else if(idField.getText().split("\\s").length > 1) {
			idWarningLbl.setText("Id cannot contain spaces");
			idWarningLbl.setVisible(true);
		} else {
			idWarningLbl.setVisible(false);
		}
	}

	private void updateAgeWarningLabel() {
		if(sessionDate != null) {
			final Period specifiedAge = ageField.getValue();
			if (specifiedAge != null && participant.getBirthDate() != null) {
				if(sessionDate.isAfter(participant.getBirthDate())) {
					final Period calculatedAge = participant.getBirthDate().until(sessionDate);
					ageWarninglbl.setVisible(!calculatedAge.equals(specifiedAge));
				} else {
					ageWarninglbl.setVisible(false);
				}
			} else {
				ageWarninglbl.setVisible(false);
			}
		} else {
			ageWarninglbl.setVisible(false);
		}
	}

	private void updateBirthdayWarningLabel() {
		if(sessionDate != null)
			bdayWarningLbl.setVisible(participant.getBirthDate() != null ? participant.getBirthDate().isAfter(sessionDate) : false);
		else
			bdayWarningLbl.setVisible(false);
	}

	public void setOtherParticipants(List<Participant> parts) {
		this.otherParticipants = parts;

		if(participant.getRole() == null) {
			participant.setRole(ParticipantRole.TARGET_CHILD);
			participant.setId(getRoleId());
			idField.setText(participant.getId());
		}
	}

	public LocalDate getSessionDate() {
		return this.sessionDate;
	}

	public void setSessionDate(LocalDate sessionDate) {
		this.sessionDate = sessionDate;
		bdayField.setPromptDate(sessionDate);
		updateBirthdayWarningLabel();
		updateAgeWarningLabel();

		if(sessionDate != null && participant.getAge(null) == null
				&& participant.getBirthDate() != null
				&& participant.getBirthDate().isBefore(sessionDate)) {
			final Period age = participant.getAge(sessionDate);
			ageField.setPrompt(AgeFormatter.ageToString(age));
			ageField.setKeepPrompt(true);
		}
	}

	public String getRoleId() {
		final ParticipantRole role = (ParticipantRole)roleBox.getSelectedItem();
		String id = role.getId();

		if(otherParticipants != null) {
			boolean checked = false;
			int idx = 0;
			while(!checked) {
				checked = true;
				for(Participant otherP:otherParticipants) {
					if(otherP.getId().equals(id)) {
						id = role.getId().substring(0, 2) + (++idx);
						checked = false;
					}
				}
			}
		}
		return id;
	}

	public void updateRoleId() {
		idField.setText(getRoleId());
	}

	private void onShowPropertyMenu(JComponent lbl, String propName) {
		final JPopupMenu menu = new JPopupMenu();
		final MenuBuilder builder = new MenuBuilder(menu);

		switch (propName) {
			case "id" -> {
				final PhonUIAction<Void> assignIdFromRole = PhonUIAction.runnable(this::updateRoleId);
				assignIdFromRole.putValue(PhonUIAction.NAME, "Assign id from role");
				assignIdFromRole.putValue(PhonUIAction.SHORT_DESCRIPTION, "Assign id from selected role");
				builder.addItem(".", assignIdFromRole);
			}

			case "birthday" -> {
				if(participant.getAge(null) != null) {
					final PhonUIAction<Void> onCalcBirthday = PhonUIAction.runnable(this::onCalcBirthday);
					onCalcBirthday.putValue(PhonUIAction.NAME, "Calculate from age");
					onCalcBirthday.putValue(PhonUIAction.SHORT_DESCRIPTION, "Calculate birthday from provided age");
					builder.addItem(".", onCalcBirthday);
					builder.addSeparator(".", "custom_items");
				}
			}

			case "age" -> {
				if(participant.getBirthDate() != null) {
					final PhonUIAction<Void> onCalcAge = PhonUIAction.runnable(this::onCalcAge);
					onCalcAge.putValue(PhonUIAction.NAME, "Calculate from birthday");
					onCalcAge.putValue(PhonUIAction.SHORT_DESCRIPTION, "Calculate age from provided birthday");
					builder.addItem(".", onCalcAge);
					builder.addSeparator(".", "custom_items");
				}
			}
		}

		if(!"id".equals(propName)) {
			final PhonUIAction<String> onClearField = PhonUIAction.consumer(this::onClearField, propName);
			onClearField.putValue(PhonUIAction.NAME, "Clear " + propName);
			onClearField.putValue(PhonUIAction.SHORT_DESCRIPTION, "Clear data for property " + propName);
			builder.addItem(".", onClearField);
		}

		menu.show(lbl, 0, lbl.getHeight());
	}

	private void onClearField(String propName) {
		switch (propName) {
			case "name" -> {
				nameField.setText("");
			}

			case "language" -> {
				languageField.setText("");
			}

			case "birthday" -> {
				bdayField.setDateTime(null);
			}

			case "age" -> {
				ageField.setText("");
			}

			case "group" -> {
				groupField.setText("");
			}

			case "sex" -> {
				sexBox.setSelectedItem(Sex.UNSPECIFIED);
			}

			case "ses" -> {
				sesField.setText("");
			}

			case "education" -> {
				educationField.setText("");
			}
		}
	}

	private void onCalcBirthday() {
		final Period age = participant.getAge(null);
		if(age != null) {
			final LocalDate sessionDate = getSessionDate();
			final LocalDate bday = sessionDate.minus(age);
			bdayField.setDateTime(bday);
		}
	}

	private void onCalcAge() {
		final LocalDate sessionDate = getSessionDate();
		final LocalDate bday = participant.getBirthDate();
		if(sessionDate.isAfter(bday)) {
			final Period age = bday.until(sessionDate);
			ageField.setValue(age);
		}
	}

	private void onAnonymize() {
		final JDialog anonymizeDialog = new JDialog(CommonModuleFrame.getCurrentFrame());
		anonymizeDialog.setModal(true);

		anonymizeDialog.setLayout(new BorderLayout());
		final DialogHeader header = new DialogHeader("Anonymize Participant",
				"Anonymize selected information for " + participant.toString());
		anonymizeDialog.add(header, BorderLayout.NORTH);

		final AnonymizeParticipantOptionsPanel optionsPanel = new AnonymizeParticipantOptionsPanel();
		optionsPanel.setBorder(BorderFactory.createTitledBorder("Select information to strip"));
		anonymizeDialog.add(optionsPanel, BorderLayout.CENTER);

		final ActionListener closeListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				anonymizeDialog.setVisible(false);
			}
		};

		final PhonUIAction okAct = PhonUIAction.consumer(this::doAnonymizeParticipant, optionsPanel);
		okAct.putValue(PhonUIAction.NAME, "Ok");
		final JButton okBtn = new JButton(okAct);
		okBtn.addActionListener(closeListener);

		final JButton closeBtn = new JButton("Cancel");
		closeBtn.addActionListener(closeListener);

		final JComponent btnPanel = ButtonBarBuilder.buildOkCancelBar(okBtn, closeBtn);
		anonymizeDialog.add(btnPanel, BorderLayout.SOUTH);

		anonymizeDialog.pack();
		anonymizeDialog.setLocationRelativeTo(this);
		anonymizeDialog.setVisible(true);
	}

	public void doAnonymizeParticipant(AnonymizeParticipantOptionsPanel optionsPanel) {
		if(optionsPanel.isAssignId())
			idField.setText(getRoleId());
		if(optionsPanel.isAnonName())
			nameField.setText("");

		final String ageTxt = ageField.getPrompt();
		if(optionsPanel.isAnonBday())
			bdayField.setDateTime(null);

		if(optionsPanel.isAnonAge())
			ageField.setText("");
		else {
			if(participant.getAge(null) == null && ageTxt.matches("[0-9]+;[0-9]{1,2}\\.[0-9]{1,2}")) {
				ageField.setText(ageTxt);
			}
		}

		if(optionsPanel.isAnonSex())
			sexBox.setSelectedItem(Sex.UNSPECIFIED);
		if(optionsPanel.isAnonLang())
			languageField.setText("");
		if(optionsPanel.isAnonGroup())
			groupField.setText("");
		if(optionsPanel.isAnonEdu())
			educationField.setText("");
		if(optionsPanel.isAnonSes())
			sesField.setText("");
	}

	public Participant getParticipant() {
		return this.participant;
	}

	private class FieldMenuListener extends MouseInputAdapter {

		final String propName;

		public FieldMenuListener(String propName) {
			this.propName = propName;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			onShowPropertyMenu((JLabel)e.getSource(), propName);
		}

	}

	private class ItemUpdater implements ItemListener {

		private final Consumer<Participant> updater;

		public ItemUpdater(Consumer<Participant> updater) {
			this.updater = updater;
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			updater.accept(participant);
		}

	}

	private class ActionUpdater implements ActionListener {

		private final Consumer<Participant> updater;

		public ActionUpdater(Consumer<Participant> updater) {
			this.updater = updater;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			updater.accept(participant);
		}

	}

	private class TextFieldUpdater implements DocumentListener {

		private final Consumer<Participant> updater;

		public TextFieldUpdater(Consumer<Participant> updater) {
			this.updater = updater;
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			updater.accept(participant);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updater.accept(participant);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {

		}

	}

	private class PropertyUpdater implements PropertyChangeListener {

		private final Consumer<Participant> updater;

		public PropertyUpdater(Consumer<Participant> updater) {
			super();
			this.updater = updater;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			updater.accept(participant);
		}

	}

}
