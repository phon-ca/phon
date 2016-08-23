package ca.phon.app.opgraph.analysis;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JMenuBar;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXTitledPanel;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.Processor;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.session.SessionSelector;
import ca.phon.project.Project;
import ca.phon.session.SessionPath;
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
		
		sessionSelectorStep.setNextStep(1);
		
		for(int stepIdx = 0; stepIdx < numberOfSteps(); stepIdx++) {
			final WizardStep ws = super.getWizardStep(stepIdx);
			
			if(ws.getPrevStep() >= 0) {
				ws.setPrevStep(ws.getPrevStep()+1);
			}
			if(ws.getNextStep() >= 0) {
				ws.setNextStep(ws.getNextStep()+1);
			}
		}
		super.addWizardStep(0, sessionSelectorStep);
		super.getWizardStep(1).setPrevStep(0);
		super.gotoStep(0);
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
			getBufferPanel().closeAllBuffers();
			getProcessor().getContext().put("_selectedSessions", sessionSelector.getSelectedSessions());
		}
		super.gotoStep(stepIdx);
	}
}
