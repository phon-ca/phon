package ca.phon.app.opgraph.analysis;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.worker.PhonWorker;

/**
 * Window which allows the user to create new analysis graphs
 * from existing documents.
 *
 */
public class AnalysisGeneratorFrame extends CommonModuleFrame {

	private static final long serialVersionUID = 381056065486562206L;

	private final static Logger LOGGER = Logger.getLogger(AnalysisGeneratorFrame.class.getName());

	private AnalysisGraphGeneratorPanel genPanel;
	
	private JButton composerButton;
	
	private JButton runButton;

	public AnalysisGeneratorFrame(Project project) {
		super();

		putExtension(Project.class, project);

		init();
	}

	private void init() {
		setLayout(new BorderLayout());

		final DialogHeader header = new DialogHeader("Analysis Composer (simple)", "Create a new analysis from existing documents");
		add(header, BorderLayout.NORTH);

		genPanel = new AnalysisGraphGeneratorPanel(getProject());
		add(genPanel, BorderLayout.CENTER);

		final PhonUIAction showComposerAct = new PhonUIAction(this, "onShowComposer");
		showComposerAct.putValue(PhonUIAction.NAME, "Open in Composer (advanced)");
		showComposerAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Open analysis in Composer (advanced)");
		composerButton = new JButton(showComposerAct);
		
		final PhonUIAction runAnalysisAct = new PhonUIAction(this, "onRunAnalysis");
		runAnalysisAct.putValue(PhonUIAction.NAME, "Run Analysis");
		runAnalysisAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Run analysis");
		runButton = new JButton(runAnalysisAct);
		
		final JComponent buttonPanel = ButtonBarBuilder.buildOkCancelBar(runButton, composerButton);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	public Project getProject() {
		return getExtension(Project.class);
	}

	public void onShowComposer() {
		final OpgraphEditor editor =  new OpgraphEditor(genPanel.getModel());

		final Project project = CommonModuleFrame.getCurrentFrame().getExtension(Project.class);
		if(project != null) {
			editor.putExtension(Project.class, project);
			((AnalysisOpGraphEditorModel)editor.getModel()).getParticipantSelector().getSessionSelector().setProject(project);
		}

		editor.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
		editor.pack();
		editor.setSize(1024, 768);
		editor.setVisible(true);
		editor.setModified(true);
		
		this.close();
		this.dispose();
	}
	
	public void onRunAnalysis() {
		final AnalysisRunner runner = new AnalysisRunner(genPanel.getGraph(), getExtension(Project.class),
				new ArrayList<>(), true);
		PhonWorker.getInstance().invokeLater(runner);
	}
	
}
