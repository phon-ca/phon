package ca.phon.app.query.analysis;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import ca.phon.app.log.BufferPanel;
import ca.phon.app.query.ScriptPanel;
import ca.phon.app.session.SessionSelector;
import ca.phon.project.Project;
import ca.phon.query.analysis.DefaultQueryAnalysis;
import ca.phon.query.analysis.QueryAnalysis;
import ca.phon.query.analysis.QueryAnalysisInput;
import ca.phon.query.analysis.QueryStep;
import ca.phon.query.analysis.ScriptReportStep;
import ca.phon.query.script.QueryScript;
import ca.phon.script.PhonScript;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.wizard.WizardFrame;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.worker.PhonWorker;

/**
 * 
 */
public class QueryAnalysisWizard extends WizardFrame {

	private static final long serialVersionUID = -3844044272954427293L;
	
	private BufferPanel bufferPanel;
	
	private WizardStep reportStep;
	
	private SessionSelector sessionSelector;
	
	private ScriptPanel queryScriptPanel;
	
	private QueryScript queryScript;
	
	private PhonScript reportScript;
	
	public QueryAnalysisWizard(String title, Project project,
			QueryScript queryScript, PhonScript reportScript) {
		super(title);
		this.queryScript = queryScript;
		this.reportScript = reportScript;
		
		putExtension(Project.class, project);
		
		init();
		
		btnFinish.setVisible(false);
	}
	
	private void init() {
		final WizardStep firstStep = createFirstStep();
		firstStep.setNextStep(1);
		addWizardStep(firstStep);
		
		reportStep = createReportStep();
		reportStep.setPrevStep(0);
		addWizardStep(reportStep);
	}
	
	private WizardStep createFirstStep() {
		final WizardStep retVal = new WizardStep();
		retVal.setLayout(new BorderLayout());
		
		retVal.add(createHeader(), BorderLayout.NORTH);
		
		final JPanel panel = new JPanel(new BorderLayout());
		sessionSelector = new SessionSelector(getExtension(Project.class));
		final JScrollPane scroller = new JScrollPane(sessionSelector);
		panel.add(scroller, BorderLayout.WEST);
		
		queryScriptPanel = new ScriptPanel(queryScript);
		panel.add(queryScriptPanel, BorderLayout.CENTER);
		
		retVal.add(panel, BorderLayout.CENTER);
		
		return retVal;
	}
	
	private WizardStep createReportStep() {
		final WizardStep retVal = new WizardStep();
		retVal.setLayout(new BorderLayout());
		
		bufferPanel = new BufferPanel(super.getTitle());
		retVal.add(bufferPanel, BorderLayout.CENTER);
		
		return retVal;
	}
	
	private DialogHeader createHeader() {
		final DialogHeader retVal = new DialogHeader(super.getTitle(), "");
		return retVal;
	}
	
	@Override
	public void next() {
		super.next();
		if(getCurrentStep() == reportStep) {
			final QueryAnalysisInput input = new QueryAnalysisInput();
			input.setProject(getExtension(Project.class));
			input.setSessions(sessionSelector.getSelectedSessions());
			
			final QueryAnalysis queryAnalysis = 
					new DefaultQueryAnalysis(queryScriptPanel.getScript(), reportScript);
			
			bufferPanel.setBusy(true);
			btnBack.setEnabled(false);
			
			final Runnable inBackground = new Runnable() {
				
				@Override
				public void run() {
					final String result = queryAnalysis.performAnalysis(input);
					
					final Runnable onEDT = new Runnable() {
						
						@Override
						public void run() {
							bufferPanel.getLogBuffer().setText(result);
							bufferPanel.onSwapBuffer();
							bufferPanel.setBusy(false);
							btnBack.setEnabled(true);
						}
						
					};
					SwingUtilities.invokeLater(onEDT);
				}
			};
			PhonWorker.getInstance().invokeLater(inBackground);
		}
	}

}
