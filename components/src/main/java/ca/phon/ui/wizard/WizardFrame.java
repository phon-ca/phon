/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.ui.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXBusyLabel;

import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.wizard.WizardEvent.WizardEventType;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Frame for a wizard display.  This wizard does not block.
 *
 */
public class WizardFrame extends CommonModuleFrame {
	
	private static final long serialVersionUID = 3188162360218776636L;
	
	/* UI */
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
	
	private JPanel stepPanel;
	
	private List<WizardListener> listeners = 
			Collections.synchronizedList(new ArrayList<>());
	
	public WizardFrame(String title) {
		super(title);
		
		init();
	}
	
	private void init() {
		// step panel
		stepLayout = new CardLayout();
		stepPanel = new JPanel(stepLayout);
		add(stepPanel, BorderLayout.CENTER);
		
		// button bar
		ImageIcon icnBack = IconManager.getInstance().getIcon("actions/agt_back", IconSize.SMALL);
		ImageIcon icnNext = IconManager.getInstance().getIcon("actions/agt_forward", IconSize.SMALL);
		ImageIcon icnFinish = IconManager.getInstance().getIcon("actions/button_ok", IconSize.SMALL);
		ImageIcon icnCancel = IconManager.getInstance().getIcon("actions/button_cancel", IconSize.SMALL);
		
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
		btnFinish.setIcon(icnFinish);
		btnFinish.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				finish();
			}
			
		});
		super.getRootPane().setDefaultButton(btnFinish);
		
		btnCancel = new JButton("Close");
		btnCancel.setIcon(icnCancel);
		btnCancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
			
		});
		
		FormLayout barLayout = new FormLayout(
				"fill:pref:grow, pref, pref, 5dlu, pref, pref",
				"pref");
		CellConstraints cc = new CellConstraints();
		JPanel buttonPanel = new JPanel(barLayout);
		
		buttonPanel.add(btnBack, cc.xy(2,1));
		buttonPanel.add(btnNext, cc.xy(3,1));
		buttonPanel.add(btnFinish, cc.xy(5,1));
		buttonPanel.add(btnCancel, cc.xy(6,1));
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
		
		for(int i = 0; i < idx; i++) {
			newSteps.put(keys.get(i), steps.get(keys.get(i)));
		}
		newSteps.put(ws, uuid);
		for(int i = idx; i < keys.size(); i++) {
			newSteps.put(keys.get(i), steps.get(keys.get(i)));
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
		
		if(currentStep == (new ArrayList<>(steps.keySet()).get(numberOfSteps()-1)))
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
//			System.out.println(rect);
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
