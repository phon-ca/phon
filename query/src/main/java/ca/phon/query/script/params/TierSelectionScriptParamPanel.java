package ca.phon.query.script.params;

import ca.phon.script.params.*;
import ca.phon.session.SystemTierType;
import ca.phon.ui.text.*;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

public class TierSelectionScriptParamPanel extends JPanel  {

	private JRadioButton orthographyButton;
	private JRadioButton ipaTargetButton;
	private JRadioButton ipaActualButton;
	private JRadioButton alignmentButton;
	private JRadioButton notesButton;
	private JRadioButton otherButton;

	/* Other tiers */
	private PromptedTextField tierField;

	private TierSelectionScriptParam param;

	public TierSelectionScriptParamPanel(TierSelectionScriptParam param) {
		super();

		this.param = param;
		init();
	}

	private void init() {
		ButtonGroup btnGrp = new ButtonGroup();
		ActionListener actionListener = (ActionEvent e) -> {
			JRadioButton src = (JRadioButton) e.getSource();
			this.param.setTier(src.getText());
			tierField.setEnabled(false);
		};

		orthographyButton = new JRadioButton(SystemTierType.Orthography.getName());
		orthographyButton.addActionListener(actionListener);
		btnGrp.add(orthographyButton);

		ipaTargetButton = new JRadioButton(SystemTierType.IPATarget.getName());
		ipaTargetButton.addActionListener(actionListener);
		btnGrp.add(ipaTargetButton);

		ipaActualButton = new JRadioButton(SystemTierType.IPAActual.getName());
		ipaActualButton.addActionListener(actionListener);
		btnGrp.add(ipaActualButton);

		alignmentButton = new JRadioButton(SystemTierType.SyllableAlignment.getName());
		alignmentButton.addActionListener(actionListener);
		btnGrp.add(alignmentButton);

		notesButton = new JRadioButton(SystemTierType.Notes.getName());
		notesButton.addActionListener(actionListener);
		btnGrp.add(notesButton);

		otherButton = new JRadioButton("Other (type below)");
		btnGrp.add(otherButton);
		otherButton.addActionListener((e) -> {
			tierField.setEnabled(otherButton.isSelected());
			this.param.setTier("");
		});

		JPanel boxPanel = new JPanel(new GridLayout(2, 3));
		boxPanel.add(orthographyButton);
		boxPanel.add(ipaTargetButton);
		boxPanel.add(ipaActualButton);
		boxPanel.add(alignmentButton);
		boxPanel.add(notesButton);
		boxPanel.add(otherButton);

		tierField = new PromptedTextField("Enter tier name");
		TextCompleter completer = new TextCompleter(createTextCompleterModel());
		completer.install(tierField);
		tierField.getDocument().addDocumentListener(tierFieldListener);

		updateForm();

		setLayout(new VerticalLayout());
		add(boxPanel);
		add(tierField);
	}

	private String selectedTier() {
		if(orthographyButton.isSelected()) {
			return SystemTierType.Orthography.getName();
		} else if(ipaTargetButton.isSelected()) {
			return SystemTierType.IPATarget.getName();
		} else if(ipaActualButton.isSelected()) {
			return SystemTierType.IPAActual.getName();
		} else if(alignmentButton.isSelected()) {
			return SystemTierType.SyllableAlignment.getName();
		} else if(notesButton.isSelected()) {
			return SystemTierType.Notes.getName();
		} else {
			return tierField.getText();
		}
	}

	private void installParamListeners() {
		param.addPropertyChangeListener(ScriptParam.ENABLED_PROP, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setEnabled((boolean)evt.getNewValue());
			}

		});
		param.addPropertyChangeListener(ScriptParam.VISIBLE_PROP, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setVisible((boolean)evt.getNewValue());
			}

		});
		param.addPropertyChangeListener(StringScriptParam.PROMPT_PROP, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				tierField.setPrompt(param.getPrompt());
			}
		});
		param.addPropertyChangeListener(StringScriptParam.VALIDATE_PROP, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if((Boolean)evt.getNewValue()) {
					tierField.setState(PromptedTextField.FieldState.INPUT);
				} else {
					tierField.setState(PromptedTextField.FieldState.UNDEFINED);
				}
			}
		});
		param.addPropertyChangeListener(StringScriptParam.TOOLTIP_TEXT_PROP, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				tierField.setToolTipText((String)evt.getNewValue());
			}
		});
		param.addPropertyChangeListener(param.getParamId(), new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				updateForm();
			}

		});
	}

	private void updateForm() {
		if(tierField.getState() != PromptedTextField.FieldState.PROMPT) return;
		String selectedTier = (param.getTier() != null && param.getTier().length() > 0 ? this.param.getTier() : null);
		if(selectedTier == null || selectedTier.length() == 0) {
			tierField.setText("");
			tierField.setEnabled(false);
		} else {
			tierField.setText("");
			tierField.setEnabled(false);
			if(SystemTierType.Orthography.getName().equals(selectedTier)) {
				orthographyButton.setSelected(true);
			} else if(SystemTierType.IPATarget.getName().equals(selectedTier)) {
				ipaTargetButton.setSelected(true);
			} else if(SystemTierType.IPAActual.getName().equals(selectedTier)) {
				ipaActualButton.setSelected(true);
			} else if(SystemTierType.SyllableAlignment.getName().equals(selectedTier)) {
				alignmentButton.setSelected(true);
			} else if(SystemTierType.Notes.getName().equals(selectedTier)) {
				notesButton.setSelected(true);
			} else {
				otherButton.setSelected(true);
				tierField.setEnabled(true);
				tierField.setText(selectedTier);
			}
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		orthographyButton.setEnabled(enabled);
		ipaTargetButton.setEnabled(enabled);
		ipaActualButton.setEnabled(enabled);
		alignmentButton.setEnabled(enabled);
		notesButton.setEnabled(enabled);
		otherButton.setEnabled(enabled);
		tierField.setEnabled(enabled);
	}

	private DefaultTextCompleterModel createTextCompleterModel() {
		DefaultTextCompleterModel retVal = new DefaultTextCompleterModel();
		retVal.setSeparator(",");

		for(SystemTierType stt:SystemTierType.values()) {
			if(stt == SystemTierType.ActualSyllables ||
					stt == SystemTierType.TargetSyllables) continue;
			retVal.addCompletion(stt.getName());
		}

		return retVal;
	}

	private final DocumentListener tierFieldListener = new DocumentListener() {

		@Override
		public void removeUpdate(DocumentEvent e) {
			if(tierField.getState() != PromptedTextField.FieldState.PROMPT)
				param.setTier(selectedTier());
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			if(tierField.getState() != PromptedTextField.FieldState.PROMPT)
				param.setTier(selectedTier());
		}

		@Override
		public void changedUpdate(DocumentEvent e) {

		}
	};

}
