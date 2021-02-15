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
package ca.phon.app.opgraph.wizard;

import java.awt.*;
import java.awt.event.*;
import java.lang.ref.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import ca.phon.ui.fonts.*;
import ca.phon.ui.wizard.*;
import ca.phon.util.icons.*;

public class WizardStepList extends JPanel {
	
	private static final long serialVersionUID = 6180937906703773985L;

	private final WeakReference<NodeWizard> wizardRef;
	
	private final Map<Integer, JLabel> stepLabels = new LinkedHashMap<>();
	
	public WizardStepList(NodeWizard wizard) {
		super();
		this.wizardRef = new WeakReference<NodeWizard>(wizard);
		
		init();
		getWizard().addListener( e -> { 
			if(e.getType() == WizardEvent.WizardEventType.STEP_ADDED)
				updateLabels(); 
			else
				refreshLabels();
		} );
	}

	public NodeWizard getWizard() {
		return wizardRef.get();
	}

	private void init() {
		if(getWizard() == null) return;
		
		final GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		
		updateLabels();
		
	}
	
	private void refreshLabels() {
		ImageIcon currentStepIcon = IconManager.getInstance().getIcon("actions/agt_forward", IconSize.SMALL);
		ImageIcon stepIcon = IconManager.getInstance().getDisabledIcon("actions/agt_forward", IconSize.SMALL);
		WizardStep currentStep = getWizard().getCurrentStep();
		for(int stepIdx:stepLabels.keySet()) {
			WizardStep step = getWizard().getWizardStep(stepIdx);
			if(step == currentStep) {
				stepLabels.get(stepIdx).setIcon(currentStepIcon);
				stepLabels.get(stepIdx).setFont(FontPreferences.getMonospaceFont().deriveFont(Font.BOLD));
			} else {
				stepLabels.get(stepIdx).setIcon(stepIcon);
				stepLabels.get(stepIdx).setFont(FontPreferences.getMonospaceFont());
			}
		}
	}
	
	private void updateLabels() {
		removeAll();
		stepLabels.clear();
		
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(5, 20, 5, 2);

		for(int stepIdx = 0; stepIdx < getWizard().numberOfSteps(); stepIdx++) {
			++gbc.gridy;
			final WizardStep step = getWizard().getWizardStep(stepIdx);
			final JLabel stepLbl = new JLabel(step.getTitle());
			stepLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			stepLbl.addMouseListener(new GotoStepListener(stepIdx));
			add(stepLbl, gbc);
			
			stepLabels.put(stepIdx, stepLbl);
		}

		++gbc.gridy;
		gbc.weighty = 1.0;
		add(Box.createVerticalGlue(), gbc);
		
		refreshLabels();
	}
	
	private class GotoStepListener extends MouseInputAdapter {

		int step;
		
		public GotoStepListener(int step) {
			this.step = step;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			getWizard().gotoStep(step);
		}
		
	}
	
}
