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
import java.util.List;

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
import org.joda.time.DateTime;
import org.joda.time.Period;

import ca.phon.functor.Functor;
import ca.phon.session.AgeFormatter;
import ca.phon.session.DateFormatter;
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

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * UI for editing participant information.
 *
 */
public class ParticipantPanel extends JPanel {

	private static final long serialVersionUID = 8479424482231807300L;
	
	/*
	 * UI
	 */
	private JComboBox roleBox;
	
	private JCheckBox assignIdBox;
	private JTextField idField;
	
	private JComboBox sexBox;
	
	private JTextField nameField;
	private JTextField groupField;
	private JTextField sesField;
	private JTextField educationField;
	private LanguageField languageField;
	
	private DatePicker bdayField;
	
	private FormatterTextField<Period> ageField;
//	private JButton calcAgeBtn;
	
	private DateTime sessionDate;
	
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
		roleBox = new JComboBox(ParticipantRole.values());
		
		assignIdBox = new JCheckBox("Assign ID from role");
		assignIdBox.setSelected(true);
		
		idField = new JTextField();
		idField.setEnabled(false);
		
		sexBox = new JComboBox(Sex.values());
		sexBox.setSelectedItem(
				(participant.getSex() != null ? participant.getSex() : Sex.UNSPECIFIED));
		sexBox.setRenderer(new DefaultListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList list,
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
		final Functor<Void, Participant> roleUpdater = new Functor<Void, Participant>() {
			
			@Override
			public Void op(Participant obj) {
				final ParticipantRole role = (ParticipantRole)roleBox.getSelectedItem();
				participant.setRole(role);
				if(assignIdBox.isSelected()) {
					idField.setText(getRoleId());
				}
				
				return null;
			}
			
		};
		roleBox.addItemListener(new ItemUpdater(roleUpdater));
		
		final Functor<Void, Participant> idUpdater = new Functor<Void, Participant>() {
			
			@Override
			public Void op(Participant obj) {
				participant.setId(idField.getText());
				return null;
			}
			
		};
		idField.getDocument().addDocumentListener(new TextFieldUpdater(idUpdater));
		
		final Functor<Void, Participant> nameUpdater = new Functor<Void, Participant>() {
			
			@Override
			public Void op(Participant obj) {
				participant.setName(nameField.getText());
				return null;
			}
			
		};
		nameField.getDocument().addDocumentListener(new TextFieldUpdater(nameUpdater));
		
		final Functor<Void, Participant> langUpdater = new Functor<Void, Participant>() {
			
			@Override
			public Void op(Participant obj) {
				participant.setLanguage(languageField.getText());
				return null;
			}
			
		};
		languageField.getDocument().addDocumentListener(new TextFieldUpdater(langUpdater));
		
		final Functor<Void, Participant> groupUpdater = new Functor<Void, Participant>() {
			
			@Override
			public Void op(Participant obj) {
				participant.setGroup(groupField.getText());
				return null;
			}
			
		};
		groupField.getDocument().addDocumentListener(new TextFieldUpdater(groupUpdater));
		
		final Functor<Void, Participant> eduUpdater = new Functor<Void, Participant>() {
			
			@Override
			public Void op(Participant obj) {
				participant.setEducation(educationField.getText());
				return null;
			}
			
		};
		educationField.getDocument().addDocumentListener(new TextFieldUpdater(eduUpdater));
		
		final Functor<Void, Participant> sesUpdater = new Functor<Void, Participant>() {
			
			@Override
			public Void op(Participant obj) {
				participant.setSES(sesField.getText());
				return null;
			}
			
		};
		sesField.getDocument().addDocumentListener(new TextFieldUpdater(sesUpdater));
		
		final Functor<Void, Participant> sexUpdater = new Functor<Void, Participant>() {
			
			@Override
			public Void op(Participant obj) {
				participant.setSex((Sex)sexBox.getSelectedItem());
				return null;
			}
			
		};
		sexBox.addItemListener(new ItemUpdater(sexUpdater));
		
		final Functor<Void, Participant> assignIdFunctor = new Functor<Void, Participant>() {
			
			@Override
			public Void op(Participant obj) {
				if(assignIdBox.isSelected()) {
					if(assignIdBox.isSelected()) {
						idField.setText(getRoleId());
					}
				}
				idField.setEnabled(!assignIdBox.isSelected());
				return null;
			}
			
		};
		assignIdBox.addItemListener(new ItemUpdater(assignIdFunctor));
		
		final Functor<Void, Participant> bdayUpdater = new Functor<Void, Participant>() {
			
			@Override
			public Void op(Participant obj) {
				final DateTime bday = bdayField.getDateTime();
				participant.setBirthDate(bday);
				if(participant.getAge(null) == null) {
					if(sessionDate != null
						&& sessionDate.isAfter(participant.getBirthDate())) {
						final Period age = participant.getAge(sessionDate);
						ageField.setPrompt(AgeFormatter.ageToString(age));
						ageField.setKeepPrompt(true);
					} else {
						ageField.setPrompt("YY:MM.DD");
						ageField.setKeepPrompt(false);
					}
				}
				return null;
			}
			
		};
		bdayField.getTextField().getDocument().addDocumentListener(new TextFieldUpdater(bdayUpdater));
		bdayField.getTextField().addActionListener(new ActionUpdater(bdayUpdater));
		
		final Functor<Void, Participant> ageUpdater = new Functor<Void, Participant>() {
			
			@Override
			public Void op(Participant obj) {
				if(ageField.getText().trim().length() == 0) {
					participant.setAge(null);
				} else {
					final Period p = ageField.getValue();
					participant.setAge(p);
				}
				return null;
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
		optional.add(new JLabel("Birthday (" + DateFormatter.DATETIME_FORMAT + ")"), cc.xy(1, 3));
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
	
	public DateTime getSessionDate() {
		return this.sessionDate;
	}
	
	public void setSessionDate(DateTime sessionDate) {
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
		
		private final Functor<Void, Participant> updater;
		
		public ItemUpdater(Functor<Void, Participant> updater) {
			this.updater = updater;
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			updater.op(participant);
		}
		
	}
	
	private class ActionUpdater implements ActionListener {
		
		private final Functor<Void, Participant> updater;
		
		public ActionUpdater(Functor<Void, Participant> updater) {
			this.updater = updater;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			updater.op(participant);
		}
		
	}
	
	private class TextFieldUpdater implements DocumentListener {

		private final Functor<Void, Participant> updater;
		
		public TextFieldUpdater(Functor<Void, Participant> updater) {
			this.updater = updater;
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			updater.op(participant);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updater.op(participant);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			
		}
		
	}
	
	private class PropertyUpdater implements PropertyChangeListener {
		
		private final Functor<Void, Participant> updater;
		
		public PropertyUpdater(Functor<Void, Participant> updater) {
			super();
			this.updater = updater;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			updater.op(participant);
		}
		
	}
	
}
