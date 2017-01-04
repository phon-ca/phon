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
import java.util.Collection;
import java.util.List;

import javax.swing.JMenuBar;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.Processor;
import ca.phon.app.opgraph.editor.actions.OpenNodeEditorAction;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.NodeWizardReportContext;
import ca.phon.app.project.ParticipantsPanel;
import ca.phon.project.Project;
import ca.phon.session.Participant;
import ca.phon.session.SessionPath;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.ui.wizard.WizardStep;

public class AnalysisWizard extends NodeWizard {

	private static final long serialVersionUID = -3667158379797520370L;

	private Project project;
	
	private WizardStep sessionSelectorStep;
	private ParticipantsPanel participantsPanel;
	
	public AnalysisWizard(String title, Processor processor, OpGraph graph) {
		super(title, processor, graph);
		
		sessionSelectorStep = addSessionSelectionStep();
		if(processor.getContext().containsKey("_project")) {
			setProject((Project)processor.getContext().get("_project"));
			
			if(processor.getContext().containsKey("_selectedSessions")) {
				@SuppressWarnings("unchecked")
				final List<SessionPath> selectedSessions = 
						(List<SessionPath>)processor.getContext().get("_selectedSessions");
				participantsPanel.getSessionSelector().setSelectedSessions(selectedSessions);
			}
		}
		getRootPane().setDefaultButton(btnNext);
		
		gotoStep(0);
	}
	
	public WizardStep getSessionSelectorStep() {
		return this.sessionSelectorStep;
	}
	
	@Override
	public void setJMenuBar(JMenuBar menuBar) {
		super.setJMenuBar(menuBar);
		
		final MenuBuilder builder = new MenuBuilder(menuBar);
		builder.addSeparator("File@1", "save");
		builder.addItem("File@save", new SaveAnalysisAction(this));
		
		final PhonUIAction openEditorAct = new PhonUIAction(this, "onOpenEditor");
		openEditorAct.putValue(PhonUIAction.NAME, "Open graph in analysis editor...");
		builder.addItem("File@" + SaveAnalysisAction.TXT, openEditorAct);
	}
	
	public void onOpenEditor(PhonActionEvent pae) {
		final OpenNodeEditorAction act = new OpenNodeEditorAction(getGraph());
		act.actionPerformed(pae.getActionEvent());
		
		dispose();
	}
	
	private WizardStep addSessionSelectionStep() {
		final WizardStep sessionSelectorStep = new WizardStep() {

			@Override
			public boolean validateStep() {
				boolean valid = true;
				
				if(participantsPanel.getCheckedSessions().size() == 0) {
					valid = false;
					ToastFactory.makeToast("Please select at least one session")
						.start(participantsPanel.getSessionSelector());
				}
				
				if(participantsPanel.getCheckedParticipants().size() == 0) {
					valid = false;
					ToastFactory.makeToast("Please select at least one participant")
						.start(participantsPanel.getParticipantSelector());
				}
				
				return valid;
			}
			
		};
		sessionSelectorStep.setTitle("Sessions & Participants");
	
		participantsPanel = new ParticipantsPanel(getProject());
		sessionSelectorStep.setLayout(new BorderLayout());
		sessionSelectorStep.add(participantsPanel, BorderLayout.CENTER);
		
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
		
		return sessionSelectorStep;
	}

	public void setProject(Project project) {
		this.project = project;
		putExtension(Project.class, project);
		participantsPanel.setProject(project);
	}
	
	public Project getProject() {
		return this.project;
	}
	
	@Override
	public void gotoStep(int stepIdx) {
		if(getWizardStep(stepIdx) == reportDataStep) {
			if(participantsPanel != null)
				getProcessor().getContext().put("_selectedSessions", participantsPanel.getCheckedSessions());
			if(participantsPanel != null) {
				Collection<Participant> selectedParticipants = participantsPanel.getCheckedParticipants();
				getProcessor().getContext().put("_selectedParticipants", selectedParticipants);
			}
		}
		super.gotoStep(stepIdx);
	}
	
	@Override
	public void setupReportContext(NodeWizardReportContext context) {
		super.setupReportContext(context);
		
		// add selected sessions and participants
		context.put("project", getProject());
		context.put("selectedSessions", participantsPanel.getCheckedSessions());
		context.put("selectedParticipants", participantsPanel.getCheckedParticipants());
	}
	
}
