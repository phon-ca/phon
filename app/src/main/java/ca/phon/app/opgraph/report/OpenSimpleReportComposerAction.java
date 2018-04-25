package ca.phon.app.opgraph.report;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.editor.SimpleEditor;
import ca.phon.app.opgraph.nodes.ReportNodeInstantiator;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.nodes.general.MacroNode;
import ca.phon.project.Project;

public class OpenSimpleReportComposerAction extends HookableAction {
	
	private static final long serialVersionUID = 3781206121705628643L;

	private final static String TXT = "Composer (simple)...";
	
	private final OpGraph reportGraph;
	
	private final Project project;
	
	private final String queryId;
	
	public OpenSimpleReportComposerAction(Project project, String queryId) {
		this(project, queryId, null);
	}
	
	public OpenSimpleReportComposerAction(Project project, String queryId, OpGraph reportGraph) {
		super();
		
		putValue(NAME, TXT);
		
		this.project = project;
		this.queryId = queryId;
		this.reportGraph = reportGraph;
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final SimpleEditor frame =
				new SimpleEditor(project,
						new ReportLibrary(), new ReportEditorModelInstantiator(), new ReportNodeInstantiator(),
						(qs) -> new MacroNode(),
						(graph, project) -> new ReportRunner(graph, project, queryId) );
		frame.getEditor().setIncludeQueries(true);

		if(reportGraph != null) {
			frame.getEditor().addGraph(reportGraph);
		}
		
		frame.pack();
		frame.setSize(new Dimension(700, 500));
		frame.centerWindow();
		frame.setVisible(true);
	}

}
