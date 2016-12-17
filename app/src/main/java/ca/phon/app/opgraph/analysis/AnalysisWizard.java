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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXBusyLabel;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.Processor;
import ca.phon.app.opgraph.editor.actions.OpenNodeEditorAction;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.NodeWizardReportContext;
import ca.phon.app.session.ParticipantSelector;
import ca.phon.app.session.SessionSelector;
import ca.phon.project.ParticipantHistory;
import ca.phon.project.Project;
import ca.phon.session.Participant;
import ca.phon.session.SessionPath;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxState;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeModel;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeModel.CheckingMode;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.worker.PhonWorker;

public class AnalysisWizard extends NodeWizard {

	private static final long serialVersionUID = -3667158379797520370L;

	private Project project;
	
	private WizardStep sessionSelectorStep;
	private SessionSelector sessionSelector = new SessionSelector();
	private ParticipantSelector participantSelector = new ParticipantSelector();
	private JXBusyLabel participantBusyLabel = new JXBusyLabel(new Dimension(16, 16));
	
	public AnalysisWizard(String title, Processor processor, OpGraph graph) {
		super(title, processor, graph);
		
		if(processor.getContext().containsKey("_project")) {
			setProject((Project)processor.getContext().get("_project"));
			
			if(processor.getContext().containsKey("_selectedSessions")) {
				@SuppressWarnings("unchecked")
				final List<SessionPath> selectedSessions = 
						(List<SessionPath>)processor.getContext().get("_selectedSessions");
				sessionSelector.setSelectedSessions(selectedSessions);
				
				SwingUtilities.invokeLater( () -> updateParticipantSelector() );
			}
		}
		
		sessionSelectorStep = addSessionSelectionStep();
		gotoStep(0);
		
		getRootPane().setDefaultButton(btnNext);
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
				
				if(sessionSelector.getSelectedSessions().size() == 0) {
					valid = false;
					ToastFactory.makeToast("Please select at least one session")
						.start(sessionSelector);
				}
				
				if(participantSelector.getSelectedParticpants().size() == 0) {
					valid = false;
					ToastFactory.makeToast("Please select at least one participant")
						.start(participantSelector);
				}
				
				return valid;
			}
			
		};
		sessionSelectorStep.setTitle("Sessions & Participants");
		
		final Container container = sessionSelectorStep;

		final GridBagConstraints gbc = new GridBagConstraints();
		container.setLayout(new GridBagLayout());
		gbc.gridx = 0; 
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridheight = 2;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(2, 2, 2, 2);
		
		final TitledPanel panel = new TitledPanel("Select Sessions");
		panel.getContentContainer().setLayout(new BorderLayout());
		final JScrollPane sessionScroller = new JScrollPane(sessionSelector);
		sessionSelector.getModel().addTreeModelListener(new TreeModelListener() {
			
			@Override
			public void treeStructureChanged(TreeModelEvent e) {
			}
			
			@Override
			public void treeNodesRemoved(TreeModelEvent e) {
			}
			
			@Override
			public void treeNodesInserted(TreeModelEvent e) {
			}
			
			@Override
			public void treeNodesChanged(TreeModelEvent e) {
				updateParticipantSelector();
			}
			
		});
		panel.getContentContainer().add(sessionScroller, BorderLayout.CENTER);
		
		final TitledPanel participantPanel = new TitledPanel("Select Participants");
		participantPanel.getContentContainer().setLayout(new BorderLayout());
		final JScrollPane participantScroller = new JScrollPane(participantSelector);
		participantPanel.getContentContainer().add(participantScroller, BorderLayout.CENTER);
		participantPanel.setLeftDecoration(participantBusyLabel);
		
		sessionSelectorStep.add(panel, gbc);
		++gbc.gridx;
		sessionSelectorStep.add(participantPanel, gbc);
		
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
	
	private void updateParticipantSelector() {
		final Runnable onBg = () -> {
			SwingUtilities.invokeLater(() -> participantBusyLabel.setBusy(true));
			
			final TristateCheckBoxTreeModel model = ParticipantSelector.createModel(getProject(), sessionSelector.getSelectedSessions(), 
					new ParticipantSelector.DefaultParticipantComparator());
			
			SwingUtilities.invokeLater( () -> { 
				participantSelector.setModel(model);
				participantSelector.setCheckingStateForPath(new TreePath(participantSelector.getRoot()), TristateCheckBoxState.CHECKED);
				participantBusyLabel.setBusy(false);
			});
		};
		PhonWorker.getInstance().invokeLater(onBg);
	}
	
//	private WizardStep addParticipantSelectionStep() {
//		final WizardStep participantSelectorStep = new WizardStep();
//		participantSelectorStep.setTitle("Select participants");
//		
//		final TitledPanel panel = new TitledPanel("Select participants");
//		panel.getContentContainer().setLayout(new BorderLayout());
//		
//		final JScrollPane scroller = new JScrollPane(participantSelector);
//		panel.getContentContainer().add(scroller, BorderLayout.CENTER);
//		participantSelectorStep.setLayout(new BorderLayout());
//		participantSelectorStep.add(panel, BorderLayout.CENTER);
//		
//		int insertIdx = 1;
//		if(getWizardExtension().getWizardMessage() != null
//				&& getWizardExtension().getWizardMessage().length() > 0) {
//			insertIdx = 2;
//		}
//		participantSelectorStep.setNextStep(insertIdx+1);
//		participantSelectorStep.setPrevStep(insertIdx-1);
//		
//		super.addWizardStep(insertIdx, participantSelectorStep);
//		
//		getWizardStep(insertIdx-1).setNextStep(insertIdx);
//		getWizardStep(insertIdx+1).setPrevStep(insertIdx);
//		
//		return participantSelectorStep;
//	}
	
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
//		if(getWizardStep(stepIdx) == participantSelectorStep) {
//			// populate tree with information from selected sessions
//			participantSelector.loadParticipants(getProject(), sessionSelector.getSelectedSessions());
//			participantSelector.setCheckingStateForPath(new TreePath(participantSelector.getRoot()), TristateCheckBoxState.CHECKED);
//		}
		if(getWizardStep(stepIdx) == reportDataStep) {
			if(sessionSelector != null)
				getProcessor().getContext().put("_selectedSessions", sessionSelector.getSelectedSessions());
			if(participantSelector != null) {
				List<Participant> selectedParticipants = participantSelector.getSelectedParticpants();
				for(Participant p:selectedParticipants) {
					final ParticipantHistory history = ParticipantHistory.calculateHistoryForParticpant(project, sessionSelector.getSelectedSessions(), p);
					p.putExtension(ParticipantHistory.class, history);
				}
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
		context.put("selectedSessions", sessionSelector.getSelectedSessions());
		context.put("selectedParticipants", participantSelector.getSelectedParticpants());
	}
	
}
