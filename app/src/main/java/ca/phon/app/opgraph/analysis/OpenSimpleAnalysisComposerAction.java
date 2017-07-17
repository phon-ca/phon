package ca.phon.app.opgraph.analysis;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.VertexNotFoundException;
import ca.gedge.opgraph.exceptions.ItemMissingException;
import ca.gedge.opgraph.nodes.general.MacroNode;
import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.editor.SimpleEditor;
import ca.phon.app.opgraph.nodes.AnalysisNodeInstantiator;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;

public class OpenSimpleAnalysisComposerAction extends HookableAction {
	
	private static final long serialVersionUID = 3781206121705628643L;

	private final static Logger LOGGER = Logger.getLogger(OpenSimpleAnalysisComposerAction.class.getName());
	
	private final static String TXT = "Composer (simple)...";
	
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
							LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
							final MessageDialogProperties props = new MessageDialogProperties();
							props.setTitle("Composer (simple)");
							props.setHeader("Unable to create analysis from query");
							props.setMessage(e.getLocalizedMessage());
							props.setOptions(MessageDialogProperties.okOptions);
							props.setRunAsync(false);
							props.setParentWindow(CommonModuleFrame.getCurrentFrame());
							NativeDialogs.showMessageDialog(props);
						}
						return new MacroNode();
					} ,
					AnalysisRunner::new );
		frame.setIncludeQueries(true);

		if(analysisGraph != null) {
			frame.addGraph(analysisGraph);
		}
		
		frame.pack();
		frame.setSize(new Dimension(700, 500));
		frame.centerWindow();
		frame.setVisible(true);
	}

}
