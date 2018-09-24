package ca.phon.app.opgraph.analysis;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.editor.SimpleEditor;
import ca.phon.app.opgraph.nodes.AnalysisNodeInstantiator;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.dag.CycleDetectedException;
import ca.phon.opgraph.dag.VertexNotFoundException;
import ca.phon.opgraph.exceptions.ItemMissingException;
import ca.phon.opgraph.nodes.general.MacroNode;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;

public class OpenSimpleAnalysisComposerAction extends HookableAction {
	
	private static final long serialVersionUID = 3781206121705628643L;

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(OpenSimpleAnalysisComposerAction.class.getName());
	
	private final static String TXT = "Analysis Composer...";
	
	private final Project project;
	
	private final OpGraph analysisGraph;
	
	public OpenSimpleAnalysisComposerAction(Project project) {
		this(project, null);
	}
	
	public OpenSimpleAnalysisComposerAction(Project project, OpGraph analysisGraph) {
		super();
		
		putValue(NAME, TXT);
		
		this.project = project;
		this.analysisGraph = analysisGraph;
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final SimpleEditor frame =
			new SimpleEditor(project,
					new AnalysisLibrary(), new AnalysisEditorModelInstantiator(), new AnalysisNodeInstantiator(),
					(qs) ->  {
						try {
							return AnalysisLibrary.analysisFromQuery(qs);
						} catch (IOException | IllegalArgumentException | ItemMissingException | VertexNotFoundException | CycleDetectedException | InstantiationException | URISyntaxException e) {
							LOGGER.error( e.getLocalizedMessage(), e);
							final MessageDialogProperties props = new MessageDialogProperties();
							props.setTitle("Composer (simple)");
							props.setHeader("Unable to create analysis from query");
							props.setMessage(e.getLocalizedMessage());
							props.setOptions(MessageDialogProperties.okOptions);
							props.setRunAsync(true);
							props.setParentWindow(CommonModuleFrame.getCurrentFrame());
							NativeDialogs.showMessageDialog(props);
						}
						return new MacroNode();
					} ,
					AnalysisRunner::new );
		frame.getEditor().setIncludeQueries(true);

		if(analysisGraph != null) {
			frame.getEditor().addGraph(analysisGraph);
		}
		
		frame.pack();
		frame.setSize(new Dimension(1024, 768));
		frame.centerWindow();
		frame.setVisible(true);
	}

}
