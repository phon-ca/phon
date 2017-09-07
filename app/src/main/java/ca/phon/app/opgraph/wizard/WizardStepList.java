/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.opgraph.wizard;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import java.util.*;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

import ca.phon.ui.fonts.FontPreferences;
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
				stepLabels.get(stepIdx).setFont(FontPreferences.getControlFont().deriveFont(Font.BOLD));
			} else {
				stepLabels.get(stepIdx).setIcon(stepIcon);
				stepLabels.get(stepIdx).setFont(FontPreferences.getControlFont());
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
