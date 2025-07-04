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
package ca.phon.app.project.checkwizard;

import ca.phon.app.prefs.PhonProperties;
import ca.phon.app.session.SessionSelector;
import ca.phon.project.Project;
import ca.phon.session.SessionPath;
import ca.phon.syllabifier.*;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.util.PrefHelper;
import com.jgoodies.forms.layout.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Step 1 - Check IPA wizard
 * 
 * Select sessions and actions to perform.
 *
 */
public class CheckWizardStep1 extends WizardStep {
	
	private static final long serialVersionUID = -1527112442759878465L;

	public static enum Operation {
		CHECK_IPA,
		RESET_SYLLABIFICATION,
		RESET_ALIGNMENT;
	}
	
	private SessionSelector sessionSelector;
	
	private JRadioButton checkIPAButton;
	
	private JRadioButton resetSyllabificationButton;
	
	private JCheckBox resetAlignmentBox;
	
	private JRadioButton resetAlignmentButton;
	
	private JComboBox syllabifierList;
	
	private Project project;
	
	public CheckWizardStep1(Project project) {
		super();
		
		this.project = project;
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		FormLayout topLayout = new FormLayout(
				"20px, pref, pref:grow", 
				"pref, pref, pref, pref, pref");
		CellConstraints cc = new CellConstraints();
		topPanel.setLayout(topLayout);
		
		final SyllabifierLibrary library = SyllabifierLibrary.getInstance();
		final Iterator<Syllabifier> syllabifiers = library.availableSyllabifiers();
		final List<Syllabifier> orderedSyllabifiers = new ArrayList<Syllabifier>();
		while(syllabifiers.hasNext()) orderedSyllabifiers.add(syllabifiers.next());
		Collections.sort(orderedSyllabifiers, new SyllabifierComparator());
	    
	    syllabifierList = new JComboBox(orderedSyllabifiers.toArray(new Syllabifier[0]));
	    syllabifierList.setSelectedItem(SyllabifierLibrary.getInstance().defaultSyllabifier());
	    syllabifierList.setEnabled(false);
	    syllabifierList.setRenderer(new SyllabifierCellRenderer());
	   
	    final String preferredSyllabifier = PrefHelper.get(
	    		PhonProperties.SYLLABIFIER_LANGUAGE,
	    		PhonProperties.DEFAULT_SYLLABIFIER_LANGUAGE);
	    syllabifierList.setSelectedItem(preferredSyllabifier);
		
		checkIPAButton = new JRadioButton("Check IPA Tiers");
		checkIPAButton.setToolTipText("Check IPA tiers for invalid transcriptions.");
		resetSyllabificationButton = new JRadioButton("Reset syllabification");
		
		resetSyllabificationButton.setToolTipText("Reset syllabification for all IPA tiers in selected sessions.");
		resetAlignmentBox = new JCheckBox("also reset phone alignment");
		resetAlignmentBox.setEnabled(false);
		
		resetAlignmentButton = new JRadioButton("Reset phone alignment");
		resetAlignmentButton.setToolTipText("Reset alignment for all records in selected sessions.");
		
		ButtonGroup btnGroup = new ButtonGroup();
		btnGroup.add(checkIPAButton);
		btnGroup.add(resetSyllabificationButton);
		btnGroup.add(resetAlignmentButton);
		
		resetSyllabificationButton.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				resetAlignmentBox.setEnabled(resetSyllabificationButton.isSelected());
				syllabifierList.setEnabled(resetSyllabificationButton.isSelected());
			}
		});

		checkIPAButton.setSelected(true);
		
		topPanel.add(checkIPAButton, cc.xyw(1,1,3));
		topPanel.add(resetSyllabificationButton, cc.xyw(1,2,2));
		topPanel.add(syllabifierList, cc.xy(3, 2));
		topPanel.add(resetAlignmentBox, cc.xy(2,4));
		topPanel.add(resetAlignmentButton, cc.xyw(1,5,3));
		
		final TitledPanel opTitledPanel = new TitledPanel("Operation", topPanel);
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		sessionSelector = new SessionSelector(project);
		sessionSelector.setVisibleRowCount(20);
		sessionSelector.setPreferredSize(new Dimension(350, 0));
		centerPanel.add(new JScrollPane(sessionSelector), BorderLayout.CENTER);
		
		final TitledPanel sessionsTitledPanel = new TitledPanel("Select sessions", centerPanel);
		
		final JSplitPane splitPane = new JSplitPane();
		splitPane.setLeftComponent(sessionsTitledPanel);
		splitPane.setRightComponent(opTitledPanel);
		
		super.add(splitPane, BorderLayout.CENTER);
	}

	public Operation getOperation() {
		Operation retVal = Operation.CHECK_IPA;
		
		if(resetSyllabificationButton.isSelected()) {
			retVal = Operation.RESET_SYLLABIFICATION;
		} else if(resetAlignmentButton.isSelected()) {
			retVal = Operation.RESET_ALIGNMENT;
		}
		
		return retVal;
	}
	
	public boolean isResetAlignment() {
		return resetAlignmentBox.isSelected();
	}
	
	public List<SessionPath> getSelectedSessions() {
		List<SessionPath> locations = sessionSelector.getSelectedSessions();
		Collections.sort(locations);
		return locations;
	}
	
	public Syllabifier getSyllabifier() {
		return (Syllabifier)syllabifierList.getSelectedItem();
	}
	
	@Override
	public boolean validateStep() {
		boolean retVal = true;
		
		if(getSelectedSessions().size() == 0) {
			final CommonModuleFrame cmf = CommonModuleFrame.getCurrentFrame();
			if(cmf != null) {
				cmf.showMessageDialog("Check Transcriptions", "Please select at least one session", new String[] {"Ok"});
			}
			retVal = false;
		}
		
		return retVal;
	}
	
	private class SyllabifierComparator implements Comparator<Syllabifier> {

		@Override
		public int compare(Syllabifier o1, Syllabifier o2) {
			return o1.toString().compareTo(o2.toString());
		}
		
	}
	
	private class SyllabifierCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			final JLabel retVal = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			
			if(value != null) {
				final Syllabifier syllabifier = (Syllabifier)value;
				final String text = syllabifier.getName() + " (" + syllabifier.getLanguage().toString() + ")";
				retVal.setText(text);
			}
			
			return retVal;
		}
		
	}
}
