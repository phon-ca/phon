package ca.phon.app.query.analysis;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import ca.phon.app.log.BufferPanel;
import ca.phon.app.query.ScriptPanel;
import ca.phon.app.session.SessionSelector;
import ca.phon.project.Project;
import ca.phon.query.analysis.DefaultQueryAnalysis;
import ca.phon.query.analysis.QueryAnalysis;
import ca.phon.query.analysis.QueryAnalysisInput;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.script.PhonScript;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.toast.ToastFactory;
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
		
		sessionSelector = new SessionSelector(getExtension(Project.class)){
			@Override
			public Dimension getPreferredSize() {
				Dimension retVal = super.getPreferredSize();
				retVal.width = 230;
				return retVal;
			}
		};
		final JScrollPane scroller = new JScrollPane(sessionSelector);
		
		queryScriptPanel = new ScriptPanel(queryScript);
		
		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, scroller, queryScriptPanel);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(0.4f);
		retVal.add(splitPane, BorderLayout.CENTER);
		
		return retVal;
	}
	
	private WizardStep createReportStep() {
		final WizardStep retVal = new WizardStep();
		retVal.setLayout(new BorderLayout());
		
		retVal.add(createHeader(), BorderLayout.NORTH);
		
		bufferPanel = new BufferPanel(super.getTitle());
		retVal.add(bufferPanel, BorderLayout.CENTER);
		
		return retVal;
	}
	
	private DialogHeader createHeader() {
		final QueryName qn = queryScript.getExtension(QueryName.class);
		if(qn != null) {
			final DialogHeader retVal = new DialogHeader("Assessment: " + qn.getName(), "");
			return retVal;
		} else {
			return new DialogHeader("Assessment", "");
		}
	}
	
	
	
	@Override
	public void next() {
		// make sure we have at least one session selected
		if(sessionSelector.getSelectedSessions().size() == 0) {
			ToastFactory.makeToast("Please select at least one session").start(sessionSelector);
			return;
		}
		if(!queryScriptPanel.checkParams()) {
			return;
		}
		super.next();
		if(getCurrentStep() == reportStep) {
			final QueryAnalysisInput input = new QueryAnalysisInput();
			input.setProject(getExtension(Project.class));
			input.setSessions(sessionSelector.getSelectedSessions());
			
			final QueryAnalysis queryAnalysis = 
					new DefaultQueryAnalysis(queryScriptPanel.getScript(), reportScript);
			
			bufferPanel.getLogBuffer().setText(new String());
			if(!bufferPanel.isShowingBuffer()) bufferPanel.onSwapBuffer();
			
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
