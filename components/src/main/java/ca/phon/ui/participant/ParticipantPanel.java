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
package ca.phon.ui.participant;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.VerticalLayout;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.session.AgeFormatter;
import ca.phon.session.Participant;
import ca.phon.session.ParticipantRole;
import ca.phon.session.SessionFactory;
import ca.phon.session.Sex;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.text.DatePicker;
import ca.phon.ui.text.FormatterTextField;
import ca.phon.ui.text.LanguageField;
import ca.phon.ui.toast.ToastFactory;

/**
 * UI for editing participant information.
 *
 */
public class ParticipantPanel extends JPanel {

	private static final long serialVersionUID = 8479424482231807300L;

	/*
	 * UI
	 */
	private JComboBox<ParticipantRole> roleBox;

	private JCheckBox assignIdBox;
	private JTextField idField;

	private JComboBox<Sex> sexBox;

	private JTextField nameField;
	private JTextField groupField;
	private JTextField sesField;
	private JTextField educationField;
	private LanguageField languageField;

	private DatePicker bdayField;

	private FormatterTextField<Period> ageField;
//	private JButton calcAgeBtn;

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

	private void init() {
		// setup form
		roleBox = new JComboBox<>(ParticipantRole.values());

		assignIdBox = new JCheckBox("Assign ID from role");
		assignIdBox.setSelected(true);

		idField = new JTextField();
		idField.setEnabled(false);

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

		final PhonUIAction anonymizeAct = new PhonUIAction(this, "onAnonymize");
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

		bdayField = new DatePicker();
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

		// setup info

		if(participant.getRole() != null)
			roleBox.setSelectedItem(participant.getRole());
		if(participant.getId() != null) {
			idField.setText(participant.getId());
		}
		idField.setText(participant.getId());
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
			if(assignIdBox.isSelected()) {
				idField.setText(getRoleId());
			}
		};
		roleBox.addItemListener(new ItemUpdater(roleUpdater));

		final Consumer<Participant> idUpdater = (obj) -> {
			obj.setId(idField.getText());
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

		final Consumer<Participant> assignIdFunctor = (obj) -> {
			if(assignIdBox.isSelected()) {
				if(assignIdBox.isSelected()) {
					idField.setText(getRoleId());
				}
			}
			idField.setEnabled(!assignIdBox.isSelected());
		};
		assignIdBox.addItemListener(new ItemUpdater(assignIdFunctor));

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
		};
//		bdayField.addPropertyChangeListener(DatePicker.DATETIME_PROP, new PropertyUpdater(bdayUpdater));
		bdayField.getTextField().getDocument().addDocumentListener(new TextFieldUpdater(bdayUpdater));
		bdayField.getTextField().addActionListener(new ActionUpdater(bdayUpdater));

		final Consumer<Participant> ageUpdater = (obj) -> {
			if(ageField.getText().trim().length() == 0) {
				obj.setAge(null);
			} else {
				final Period p = ageField.getValue();
				obj.setAge(p);
			}
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
		required.add(assignIdBox, cc.xy(3,2));
		required.add(new JLabel("Id"), cc.xy(1, 3));
		required.add(idField, cc.xy(3, 3));

		final FormLayout optLayout = new FormLayout(
				"right:pref, 3dlu, fill:pref:grow, 5dlu, right:pref, 3dlu, fill:pref:grow",
				"pref, pref, pref, pref");
		final JPanel optional = new JPanel(optLayout);
		optional.setBorder(BorderFactory.createTitledBorder("Optional Information"));
		optional.add(new JLabel("Name"), cc.xy(1, 1));
		optional.add(nameField, cc.xy(3, 1));
		optional.add(new JLabel("Sex"), cc.xy(1, 2));
		optional.add(sexBox, cc.xy(3, 2));
		optional.add(new JLabel("Birthday (YYYY-MM-DD)"), cc.xy(1, 3));
		optional.add(bdayField, cc.xy(3, 3));
		optional.add(new JLabel("Age (" + AgeFormatter.AGE_FORMAT + ")"), cc.xy(1, 4));
		optional.add(ageField, cc.xy(3, 4));

		optional.add(new JLabel("Language"), cc.xy(5, 1));
		optional.add(languageField, cc.xy(7, 1));
		optional.add(new JLabel("Group"), cc.xy(5, 2));
		optional.add(groupField, cc.xy(7, 2));
		optional.add(new JLabel("Education"), cc.xy(5, 3));
		optional.add(educationField, cc.xy(7, 3));
		optional.add(new JLabel("SES"), cc.xy(5, 4));
		optional.add(sesField, cc.xy(7, 4));

		setLayout(new VerticalLayout(5));
		add(required);
		add(optional);
		add(ButtonBarBuilder.buildOkBar(anonymizeBtn));
		add(new JSeparator(SwingConstants.HORIZONTAL));
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

	public void onAnonymize() {
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

		final PhonUIAction okAct = new PhonUIAction(this, "doAnonymizeParticipant", optionsPanel);
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
			bdayField.getTextField().setText("");

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
