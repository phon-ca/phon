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
package ca.phon.ui.wizard;

import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.wizard.WizardEvent.WizardEventType;
import ca.phon.util.icons.*;
import org.jdesktop.swingx.JXBusyLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

/**
 * A basic wizard implementation.
 * 
 * Since Phon 2.2, this component includes a breadCrumb
 * viewer which is not shown by default.  To display
 * the breadCrumb, use the setBreadcrumbVisible() method.
 *
 */
public class WizardFrame extends CommonModuleFrame {
	
	private static final long serialVersionUID = 3188162360218776636L;
	
	/* UI */
	protected JPanel buttonPanel;
	protected JButton btnBack;
	protected JButton btnNext;
	protected JButton btnFinish;
	protected JButton btnCancel;
	
	private JPanel glassPane;
	private JXBusyLabel busyLabel;
	
	/** List of wizard steps */
	private HashMap<WizardStep, UUID> steps = new LinkedHashMap<>();
	
	private WizardStep currentStep;
	
	private CardLayout stepLayout;
	
	protected JPanel stepPanel;
	
	private List<WizardListener> listeners = 
			Collections.synchronizedList(new ArrayList<>());
	
	public WizardFrame(String title) {
		super(title);
		
		
		
		init();
	}
	
	public int getStepIndex(WizardStep step) {
		return (new ArrayList<>(steps.keySet())).indexOf(step);
	}
	
	private void init() {
		// step panel
		stepLayout = new CardLayout();
		stepPanel = new JPanel(stepLayout);
		add(stepPanel, BorderLayout.CENTER);
		
		// button bar
		ImageIcon icnBack = IconManager.getInstance().getIcon("actions/go-previous", IconSize.SMALL);
		ImageIcon icnNext = IconManager.getInstance().getIcon("actions/go-next", IconSize.SMALL);
		
		btnBack = new JButton("Back");
		btnBack.setIcon(icnBack);
		btnBack.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				prev();
			}
			
		});
		
		btnNext = new JButton("Next");
		btnNext.setIcon(icnNext);
		btnNext.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				next();
			}
		});
		
		btnNext.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		btnFinish = new JButton("Finish");
		btnFinish.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				finish();
			}
			
		});
		super.getRootPane().setDefaultButton(btnFinish);
		
		btnCancel = new JButton("Close");
		btnCancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
			
		});
		
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 2, 0, 0);
		
		buttonPanel = new JPanel(new GridBagLayout());
		
		gbc.weightx = 1.0;
//		buttonPanel.add(breadCrumbViewer, gbc);
		
		++gbc.gridx;
		buttonPanel.add(Box.createHorizontalGlue(), gbc);
		
		++gbc.gridx;
		gbc.weightx = 0.0;
		buttonPanel.add(btnBack, gbc);
		++gbc.gridx;
		gbc.insets = new Insets(0, 0, 0, 0);
		buttonPanel.add(btnNext, gbc);
		++gbc.gridx;
		gbc.insets = new Insets(0, 5, 0, 0);
		buttonPanel.add(btnFinish, gbc);
		++gbc.gridx;
		buttonPanel.add(btnCancel, gbc);
		buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.DARK_GRAY));
		
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	public WizardStep getWizardStep(int idx) {
		return (new ArrayList<>(steps.keySet())).get(idx);
	}
	
	public void addWizardStep(int idx, WizardStep ws) {
		UUID uuid = UUID.randomUUID();
		
		LinkedHashMap<WizardStep, UUID> newSteps = new LinkedHashMap<>();
		ArrayList<WizardStep> keys = new ArrayList<>(steps.keySet());
		
		WizardStep[] prevSteps = new WizardStep[keys.size()+1];
		WizardStep[] nextSteps = new WizardStep[keys.size()+1];
		
		for(int i = 0; i < idx; i++) {
			WizardStep step = getWizardStep(i);
			prevSteps[i] = (step.getPrevStep() >= 0 ? getWizardStep(step.getPrevStep()) : null);
			nextSteps[i] = (step.getNextStep() >= 0 ? getWizardStep(step.getNextStep()) : null);
			newSteps.put(keys.get(i), steps.get(keys.get(i)));
		}
		newSteps.put(ws, uuid);
		for(int i = idx; i < keys.size(); i++) {
			WizardStep step = getWizardStep(i);
			prevSteps[i+1] = (step.getPrevStep() >= 0 ? getWizardStep(step.getPrevStep()) : null);
			nextSteps[i+1] = (step.getNextStep() >= 0 ? getWizardStep(step.getNextStep()) : null);
			newSteps.put(keys.get(i), steps.get(keys.get(i)));
		}
		keys = new ArrayList<>(newSteps.keySet());
		for(int i = 0; i < keys.size(); i++) {
			if(i == idx) continue;
			
			// fix step links
			WizardStep prevStep = prevSteps[i];
			int prevStepIdx = (prevStep != null ? keys.indexOf(prevStep) : -1);

			WizardStep nextStep = nextSteps[i];
			int nextStepIdx = (nextStep != null ? keys.indexOf(nextStep) : -1);
			
			keys.get(i).setPrevStep(prevStepIdx);
			keys.get(i).setNextStep(nextStepIdx);
		}
		steps = newSteps;
		
		stepPanel.add(ws, uuid.toString());
		
		fireWizardEvent(new WizardEvent(WizardEventType.STEP_ADDED, this, idx));
		
		if(steps.size() == 1)
			gotoStep(0);
	}
	
	public void addWizardStep(WizardStep ws) {
		UUID uuid = UUID.randomUUID();
		steps.put(ws, uuid);
		stepPanel.add(ws, uuid.toString());
		if(steps.size() == 1)
			gotoStep(0);
		fireWizardEvent(new WizardEvent(WizardEventType.STEP_ADDED, this, steps.size()-1));
	}
	
	public WizardStep addWizardStep(JComponent comp) {
		WizardStep retVal = new WizardStep();
		retVal.setLayout(new BorderLayout());
		
		retVal.add(comp, BorderLayout.CENTER);
		
		addWizardStep(retVal);
		
		return retVal;
	}
	
	public void removeWizardStep(WizardStep ws) {
		steps.remove(ws);
		stepPanel.remove(ws);
	}
	
	public void removeAllSteps() {
		steps.clear();
		stepPanel.removeAll();
	}
	
	public int numberOfSteps() {
		return steps.size();
	}
	
	public void gotoStep(int stepIndex) {
		if(currentStep != null) {
			remove(currentStep);
		}
		if(stepIndex < 0 || stepIndex >= numberOfSteps()) return;
		
		WizardStep ws = (new ArrayList<>(steps.keySet())).get(stepIndex);
		currentStep = ws;
		
		stepLayout.show(stepPanel, steps.get(ws).toString());
		
		setupButtons();
		
		fireWizardEvent(WizardEvent.createGotoStepEvent(this, stepIndex));
	}

	protected void setupButtons() {
		int back = (currentStep == null ? -1 : currentStep.getPrevStep());
		int next = (currentStep == null ? -1 : currentStep.getNextStep());
		
		if(back >= 0)
			btnBack.setEnabled(true);
		else
			btnBack.setEnabled(false);
		
		if(next >= 0 && next < numberOfSteps())
			btnNext.setEnabled(true);
		else
			btnNext.setEnabled(false);
		
		if(steps.size() > 0 && currentStep == (new ArrayList<>(steps.keySet()).get(numberOfSteps()-1)))
			btnFinish.setEnabled(true);
		else
			btnFinish.setEnabled(false);
		
		btnCancel.setEnabled(true);
	}
	
	protected void next() {
		if(currentStep.validateStep())
			gotoStep(currentStep.getNextStep());
	}
	
	protected void prev() {
		gotoStep(currentStep.getPrevStep());
	}
	
	protected void finish() {
		if(currentStep.validateStep()) {
			fireWizardEvent(WizardEvent.createFinishedEvent(this));
			
			setVisible(false);
			dispose();
		}
	}
	
	protected void cancel() {
		setVisible(false);
		dispose();
	}
	
	@Override
	public void setVisible(boolean vis) {
		super.setVisible(vis);
		setupButtons();
	}

	public WizardStep getCurrentStep() {
		return currentStep;
	}
	
	public int getCurrentStepIndex() {
		int retVal = -1;
		if(currentStep == null) return retVal;
		for(WizardStep step:steps.keySet()) {
			++retVal;
			if(step == currentStep) {
				break;
			}
		}
		return retVal;
	}
	
	public void showWizard() {
		// setup wizard in middle of screen with a nice size
		Dimension size = new Dimension(600, 500);
		Dimension screenSize =
			Toolkit.getDefaultToolkit().getScreenSize();
		
		int posX = screenSize.width / 2 - size.width / 2;
		int posY = screenSize.height / 2 - size.height / 2;
		
		this.setBounds(posX, posY, size.width, size.height);
		setVisible(true);
	}
	
	protected void showBusyLabel(JComponent comp) {
		if(glassPane == null) {
			glassPane = new JPanel();
			glassPane.setLayout(null);
			
			setGlassPane(glassPane);
			
			busyLabel = new JXBusyLabel();
			Rectangle consoleBounds = comp.getBounds();
			Rectangle rect = SwingUtilities.convertRectangle(comp.getParent(), consoleBounds, glassPane);
			Rectangle busyBounds = 
				new Rectangle(rect.x + rect.width - busyLabel.getPreferredSize().width-20,
						rect.y+10,
						busyLabel.getPreferredSize().width,
						busyLabel.getPreferredSize().height);
			busyLabel.setBounds(busyBounds);

			glassPane.setOpaque(false);
			glassPane.add(busyLabel);
		}
		glassPane.setVisible(true);
		busyLabel.setBusy(true);
	}
	
	protected void stopBusyLabel() {
		if(glassPane == null) return;
		busyLabel.setBusy(false);
		glassPane.setVisible(false);
	}
	
	public void addListener(WizardListener listener) {
		if(!listeners.contains(listener))
			this.listeners.add(listener);
	}
	
	public void removeListener(WizardListener listener) {
		this.listeners.remove(listener);
	}
	
	public void fireWizardEvent(WizardEvent event) {
		for(WizardListener listener:listeners) {
			listener.wizardEvent(event);
		}
	}

}
