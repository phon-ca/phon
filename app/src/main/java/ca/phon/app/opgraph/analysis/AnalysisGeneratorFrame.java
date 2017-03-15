package ca.phon.app.opgraph.analysis;

import java.awt.BorderLayout;
import java.util.logging.Logger;

import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;

/**
 * Window which allows the user to create new analysis graphs
 * from existing documents.
 *
 */
public class AnalysisGeneratorFrame extends CommonModuleFrame {

	private static final long serialVersionUID = 381056065486562206L;

	private final static Logger LOGGER = Logger.getLogger(AnalysisGeneratorFrame.class.getName());

	private AnalysisGraphGeneratorPanel genPanel;

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

		// TODO add buttons
	}

	public Project getProject() {
		return getExtension(Project.class);
	}

}
