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
package ca.phon.app.opgraph.analysis;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.List;

import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.NodeWizardReportContext;
import ca.phon.app.project.ParticipantsPanel;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.Processor;
import ca.phon.project.Project;
import ca.phon.session.Participant;
import ca.phon.session.SessionPath;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.util.Tuple;

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

//	@Override
//	public void setJMenuBar(JMenuBar menuBar) {
//		super.setJMenuBar(menuBar);
//
//		final MenuBuilder builder = new MenuBuilder(menuBar);
//		builder.addSeparator("File@1", "composer");
//
//		final OpenSimpleAnalysisComposerAction openSimpleComposerAct = new OpenSimpleAnalysisComposerAction(getProject(), getGraph());
//		openSimpleComposerAct.putValue(Action.NAME, "Open analysis in Composer (simple)...");
//		builder.addItem("File@composer", openSimpleComposerAct).addActionListener( (e) -> close() );
//
//		final OpenComposerAction openComposerAct = new OpenComposerAction(getGraph());
//		openComposerAct.putValue(Action.NAME, "Open analysis in Composer (advanced)...");
//		builder.addItem("File@" + openSimpleComposerAct.getValue(PhonUIAction.NAME), openComposerAct).addActionListener( (e) -> close() );
//	}

	@Override
	public Tuple<String, String> getNoun() {
		return new Tuple<>("analysis", "analyses");
	}

	private WizardStep addSessionSelectionStep() {
		final WizardStep sessionSelectorStep = new WizardStep() {

			@Override
			public boolean validateStep() {
				if(participantsPanel.getCheckedSessions().size() == 0) {
					showOkDialog("Select Sessions", "Please select at least one session.");
					return false;
				}

				if(participantsPanel.getCheckedParticipants().size() == 0) {
					showOkDialog("Select Participants", "Please select at least one participant.");
					return false;
				}

				return true;
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
