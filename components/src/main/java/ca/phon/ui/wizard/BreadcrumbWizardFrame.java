/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.jbreadcrumb.Breadcrumb;
import ca.phon.ui.jbreadcrumb.BreadcrumbButton;
import ca.phon.ui.jbreadcrumb.JBreadcrumb;

/**
 * Wizard frame with breadcrumb UI for navigation
 *
 */
public class BreadcrumbWizardFrame extends WizardFrame {

	private static final long serialVersionUID = -416029134223190198L;
	
	/** The breadCrumb */
	private Breadcrumb<WizardStep, String> breadCrumb;
	protected JScrollPane breadcrumbScroller;
	protected JBreadcrumb<WizardStep, String> breadCrumbViewer;
	
	protected BreadcrumbButton nextButton;
	
	public BreadcrumbWizardFrame(String title) {
		super(title);
		
		init();
	}
	
	private void init() {
		breadCrumb = new Breadcrumb<WizardStep, String>() {

			@Override
			public void gotoState(WizardStep state) {
				if(getCurrentStep() != state) {
					BreadcrumbWizardFrame.this.gotoStep(getStepIndex(state));
					super.gotoState(getCurrentStep());
				}
			}
			
		};
		breadCrumbViewer = new JBreadcrumb<>(breadCrumb);
		breadCrumbViewer.setFont(FontPreferences.getTitleFont());

		// turn off parent buttons
		super.btnBack.setVisible(false);
		super.btnCancel.setVisible(false);
		super.btnFinish.setVisible(false);
		super.btnNext.setVisible(false);
		
		nextButton = new BreadcrumbButton();
		nextButton.setFont(FontPreferences.getTitleFont());
		nextButton.setText("Next");
		nextButton.addActionListener( (e) -> next() );
		
		breadCrumbViewer.setStateBackground(nextButton.getBackground().darker());
		breadCrumbViewer.setFont(FontPreferences.getTitleFont().deriveFont(Font.BOLD));
		breadCrumbViewer.setBackground(Color.white);
		breadCrumbViewer.getBreadcrumb().addBreadcrumbListener( (e) -> SwingUtilities.invokeLater( this::updateBreadcrumbButtons) );
		SwingUtilities.invokeLater( this::updateBreadcrumbButtons );

		breadcrumbScroller = new JScrollPane(breadCrumbViewer);
		breadcrumbScroller.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.darkGray));
		breadcrumbScroller.getViewport().setBackground(breadCrumbViewer.getBackground());
		breadcrumbScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		breadcrumbScroller.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));
		breadcrumbScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		add(breadcrumbScroller, BorderLayout.NORTH);
		
	}
	
	protected void updateBreadcrumbButtons() {
		if(numberOfSteps() > 0 && breadCrumbViewer.getBreadcrumb().getCurrentState() == getWizardStep(numberOfSteps()-1)) {
			breadCrumbViewer.remove(nextButton);
		} else {
			breadCrumbViewer.add(nextButton);
			setBounds(nextButton);
			getRootPane().setDefaultButton(nextButton);
			breadCrumbViewer.scrollRectToVisible(nextButton.getBounds());
		}

		breadCrumbViewer.revalidate();
		breadCrumbViewer.repaint();
	}
	
	protected void setBounds(JComponent btn) {
		final Rectangle bounds =
				new Rectangle((int)(breadCrumbViewer.getBreadcrumbViewerUI().getPreferredSize().width-btn.getInsets().left/2-1),
						0, (int)btn.getPreferredSize().width, breadCrumbViewer.getHeight());
		btn.setBounds(bounds);
	}
	
	@Override
	public void removeWizardStep(WizardStep ws) {
		super.removeWizardStep(ws);
		
		if(breadCrumb.containsState(ws)) {
			while(breadCrumb.size() > 0) {
				breadCrumb.popState();
				if(breadCrumb.getCurrentState() == ws) {
					break;
				}
			}
		}
	}
	
	@Override
	public void removeAllSteps() {
		super.removeAllSteps();
		
		breadCrumb.clear();
	}

	@Override
	public void gotoStep(int stepIndex) {
		super.gotoStep(stepIndex);
		
		WizardStep ws = getWizardStep(stepIndex);
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
	}
}
