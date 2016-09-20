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
package ca.phon.app.opgraph.analysis;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JMenuBar;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXTitledPanel;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.Processor;
import ca.phon.app.opgraph.editor.actions.OpenNodeEditorAction;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.session.SessionSelector;
import ca.phon.project.Project;
import ca.phon.session.SessionPath;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.wizard.WizardStep;

public class AnalysisWizard extends NodeWizard {

	private static final long serialVersionUID = -3667158379797520370L;

	private Project project;
	
	private SessionSelector sessionSelector = new SessionSelector();
	
	public AnalysisWizard(String title, Processor processor, OpGraph graph) {
		super(title, processor, graph);
		
		if(processor.getContext().containsKey("_project")) {
			setProject((Project)processor.getContext().get("_project"));
			
			if(processor.getContext().containsKey("_selectedSessions")) {
				@SuppressWarnings("unchecked")
				final List<SessionPath> selectedSessions = 
						(List<SessionPath>)processor.getContext().get("_selectedSessions");
				sessionSelector.setSelectedSessions(selectedSessions);
			}
		}
		
		addSessionSelectionStep();
		
		getRootPane().setDefaultButton(btnNext);
	}
	
	@Override
	public void setJMenuBar(JMenuBar menuBar) {
		super.setJMenuBar(menuBar);
		
		final MenuBuilder builder = new MenuBuilder(menuBar);
		builder.addSeparator("File@1", "save");
		builder.addItem("File@save", new SaveAnalysisAction(this));
		
		builder.addSeparator("File@Save analysis...", "open_editor");
		final PhonUIAction openEditorAct = new PhonUIAction(this, "onOpenEditor");
		openEditorAct.putValue(PhonUIAction.NAME, "Open graph in analysis editor...");
		builder.addItem("File@open_editor", openEditorAct);
	}
	
	public void onOpenEditor(PhonActionEvent pae) {
		final OpenNodeEditorAction act = new OpenNodeEditorAction(getGraph());
		act.actionPerformed(pae.getActionEvent());
		
		dispose();
	}
	
	private void addSessionSelectionStep() {
		final WizardStep sessionSelectorStep = new WizardStep();
		sessionSelectorStep.setTitle("Select sessions");
		
		final JXTitledPanel panel = new JXTitledPanel("Select sessions : " + getProject().getName());
		panel.getContentContainer().setLayout(new BorderLayout());
		
		final JScrollPane scroller = new JScrollPane(sessionSelector);
		panel.getContentContainer().add(scroller, BorderLayout.CENTER);
		sessionSelectorStep.setLayout(new BorderLayout());
		sessionSelectorStep.add(panel, BorderLayout.CENTER);
		
		int insertIdx = 0;
		if(getWizardExtension().getWizardMessage() != null
				&& getWizardExtension().getWizardMessage().length() > 0) {
			insertIdx = 1;
		}
		sessionSelectorStep.setNextStep(insertIdx+1);
		sessionSelectorStep.setPrevStep(insertIdx-1);
		
		super.addWizardStep(insertIdx, sessionSelectorStep);
		
		if(insertIdx == 1) {
			getWizardStep(0).setNextStep(insertIdx);
		}
		getWizardStep(insertIdx+1).setPrevStep(insertIdx);
		gotoStep(0);
	}
	
	public void setProject(Project project) {
		this.project = project;
		putExtension(Project.class, project);
		this.sessionSelector.setProject(project);
		this.sessionSelector.revalidate();
	}
	
	public Project getProject() {
		return this.project;
	}
	
	public SessionSelector getSessionSelector() {
		return this.sessionSelector;
	}
	
	@Override
	public void gotoStep(int stepIdx) {
		if(getWizardStep(stepIdx) == reportStep && sessionSelector != null) {
			getProcessor().getContext().put("_selectedSessions", sessionSelector.getSelectedSessions());
		}
		super.gotoStep(stepIdx);
	}
}
