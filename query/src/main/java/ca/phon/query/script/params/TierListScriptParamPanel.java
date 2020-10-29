package ca.phon.query.script.params;

import java.awt.*;
import java.beans.*;
import java.util.*;
import java.util.stream.*;

import javax.swing.*;
import javax.swing.event.*;

import org.jdesktop.swingx.*;

import ca.phon.script.params.*;
import ca.phon.session.*;
import ca.phon.ui.text.*;
import ca.phon.ui.text.PromptedTextField.*;

public class TierListScriptParamPanel extends JPanel {
	
	/* Checkboxes for default tiers */
	private JCheckBox orthographyBox;
	private JCheckBox ipaTargetBox;
	private JCheckBox ipaActualBox;
	private JCheckBox alignmentBox;
	private JCheckBox notesBox;
	
	/* Other tiers */
	private PromptedTextField tierField;
		
	private TierListScriptParam param;
	
	public TierListScriptParamPanel(TierListScriptParam param) {
		super();
		
		this.param = param;
		init();
		installParamListeners();
	}
	
	private void init() {
		Set<String> tierSet = param.tierSet();
		
		orthographyBox = new JCheckBox(SystemTierType.Orthography.getName());
		
		orthographyBox.addActionListener( (e) -> {
			if(orthographyBox.isSelected()) {
				param.addTier(SystemTierType.Orthography.getName());
			} else {
				param.removeTier(SystemTierType.Orthography.getName());
			}
		});
		
		ipaTargetBox = new JCheckBox(SystemTierType.IPATarget.getName());
		
		ipaTargetBox.addActionListener( (e) -> {
			if(ipaTargetBox.isSelected()) {
				param.addTier(SystemTierType.IPATarget.getName());
			} else {
				param.removeTier(SystemTierType.IPATarget.getName());
			}
		});
		
		ipaActualBox = new JCheckBox(SystemTierType.IPAActual.getName());
		
		ipaActualBox.addActionListener( (e) -> {
			if(ipaActualBox.isSelected()) {
				param.addTier(SystemTierType.IPAActual.getName());
			} else {
				param.removeTier(SystemTierType.IPAActual.getName());
			}
		});
		
		alignmentBox = new JCheckBox(SystemTierType.SyllableAlignment.getName());
		
		alignmentBox.addActionListener( (e) -> {
			if(alignmentBox.isSelected()) {
				param.addTier(SystemTierType.SyllableAlignment.getName());
			} else {
				param.removeTier(SystemTierType.SyllableAlignment.getName());
			}
		});
		
		notesBox = new JCheckBox(SystemTierType.Notes.getName());
		
		notesBox.addActionListener( (e) -> {
			if(notesBox.isSelected()) {
				param.addTier(SystemTierType.Notes.getName());
			} else {
				param.removeTier(SystemTierType.Notes.getName());
			}
		});
		
		JPanel boxPanel = new JPanel(new GridLayout(2, 3));
		boxPanel.add(orthographyBox);
		boxPanel.add(ipaTargetBox);
		boxPanel.add(ipaActualBox);
		boxPanel.add(alignmentBox);
		boxPanel.add(notesBox);

		
		tierField = new PromptedTextField("Enter tier names separated by ','");
		TextCompleter completer = new TextCompleter(createTextCompleterModel());
		completer.install(tierField);
		tierField.getDocument().addDocumentListener(tierFieldListener);
		
		updateForm();
		
		setLayout(new VerticalLayout());
		add(boxPanel);
		add(tierField);
	}
	
	private void installParamListeners() {
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
					tierField.setState(FieldState.INPUT);
				} else {
					tierField.setState(FieldState.UNDEFINED);
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
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		orthographyBox.setEnabled(enabled);
		ipaTargetBox.setEnabled(enabled);
		ipaActualBox.setEnabled(enabled);
		alignmentBox.setEnabled(enabled);
		notesBox.setEnabled(enabled);
		tierField.setEnabled(enabled);
	}

	private DefaultTextCompleterModel createTextCompleterModel() {
		DefaultTextCompleterModel retVal = new DefaultTextCompleterModel();
		
		String[] otherTiers = {
			"Target Syllabification", "Actual Syllabification",
			"Target CV", "Actual CV",
			"Target Stress", "Actual Stress"
		};
		for(SystemTierType stt:SystemTierType.values()) {
			retVal.addCompletion(stt.getName());
		}
		for(String otherTier:otherTiers) {
			retVal.addCompletion(otherTier);
		}
		
		return retVal;
	}
	
	private void updateForm() {
		Set<String> tierSet = param.tierSet();
		if(tierField.getState() != FieldState.PROMPT) return;
		
		orthographyBox.setSelected(tierSet.contains(SystemTierType.Orthography.getName()));
		ipaTargetBox.setSelected(tierSet.contains(SystemTierType.IPATarget.getName()));
		ipaActualBox.setSelected(tierSet.contains(SystemTierType.IPAActual.getName()));
		alignmentBox.setSelected(tierSet.contains(SystemTierType.SyllableAlignment.getName()));
		notesBox.setSelected(tierSet.contains(SystemTierType.Notes.getName()));
		
		for(SystemTierType stt:SystemTierType.values())
			tierSet.remove(stt.getName());
		String remainder = tierSet.stream().collect(Collectors.joining(","));
		tierField.getDocument().removeDocumentListener(tierFieldListener);
		tierField.setText(remainder);
		tierField.getDocument().addDocumentListener(tierFieldListener);
	}
	
	private Set<String> selectedTiers() {
		Set<String> retVal = new LinkedHashSet<>();
		
		if(orthographyBox.isSelected())
			retVal.add(SystemTierType.Orthography.getName());
		if(ipaTargetBox.isSelected())
			retVal.add(SystemTierType.IPATarget.getName());
		if(ipaActualBox.isSelected())
			retVal.add(SystemTierType.IPAActual.getName());
		if(alignmentBox.isSelected())
			retVal.add(SystemTierType.SyllableAlignment.getName());
		if(notesBox.isSelected())
			retVal.add(SystemTierType.Notes.getName());
		
		String[] otherTiers = tierField.getText().split(",");
		for(String otherTier:otherTiers) {
			if(otherTier.trim().length() > 0) {
				retVal.add(otherTier.trim());
			}
		}
		
		return retVal;
	}
	
	private final DocumentListener tierFieldListener = new DocumentListener() {
		
		@Override
		public void removeUpdate(DocumentEvent e) {
			if(tierField.getState() != FieldState.PROMPT)
				param.setTiers(selectedTiers());
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			if(tierField.getState() != FieldState.PROMPT)
				param.setTiers(selectedTiers());
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) {
			
		}
	};
	
}
