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
package ca.phon.ui.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXBusyLabel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.breadcrumb.BreadCrumb;
import ca.phon.ui.breadcrumb.BreadCrumbViewer;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.wizard.WizardEvent.WizardEventType;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

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
	
	/** The breadCrumb */
	private BreadCrumb<WizardStep, String> breadCrumb;
	protected BreadCrumbViewer<WizardStep, String> breadCrumbViewer;
	
	private List<WizardListener> listeners = 
			Collections.synchronizedList(new ArrayList<>());
	
	public WizardFrame(String title) {
		super(title);
		
		breadCrumb = new BreadCrumb<WizardStep, String>() {

			@Override
			public void gotoState(WizardStep state) {
				super.gotoState(state);
				
				if(getCurrentStep() != state)
					WizardFrame.this.gotoStep(getStepIndex(state));
			}
			
		};
		
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
		
		breadCrumbViewer = new BreadCrumbViewer<>(breadCrumb);
		breadCrumbViewer.setFont(FontPreferences.getTitleFont());
		breadCrumbViewer.setVisible(false);
		
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
	
	public boolean isBreadcrumbVisible() {
		return breadCrumbViewer.isVisible();
	}
	
	public void setBreadcrumbVisible(boolean breadcrumbVisible) {
		breadCrumbViewer.setVisible(breadcrumbVisible);
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
		
		if(breadCrumb.containsState(ws)) {
			while(breadCrumb.size() > 0) {
				breadCrumb.popState();
				if(breadCrumb.peekState(breadCrumb.size()-1) == ws) {
					break;
				}
			}
		}
	}
	
	public void removeAllSteps() {
		steps.clear();
		stepPanel.removeAll();
		
		breadCrumb.clear();
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
		
		if(breadCrumb.containsState(ws)) {
			breadCrumb.gotoState(ws);
		} else {
			breadCrumb.clear();
			for(int i = 0; i < numberOfSteps() && i <= getCurrentStepIndex(); i++) {
				WizardStep step = getWizardStep(i);
				breadCrumb.addState(step, step.getTitle());
				if(step == ws) break;
			}
		}
		
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
		else {
			final MessageDialogProperties messageProps = new MessageDialogProperties();
			messageProps.setTitle("Step Incomplete");
			messageProps.setHeader(messageProps.getTitle());
			messageProps.setMessage("This step requires user input or corrections. Please review and click Next to continue.");
			messageProps.setOptions(MessageDialogProperties.okOptions);
			messageProps.setRunAsync(true);
			NativeDialogs.showMessageDialog(messageProps);
		}
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
