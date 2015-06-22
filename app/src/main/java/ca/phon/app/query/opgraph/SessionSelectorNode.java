package ca.phon.app.query.opgraph;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.session.SessionSelector;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;

@OpNodeInfo(
	category="Project",
	name="Select Sessions",
	description="Select sessions for project.  A session selector dialog will be presented to the user if" +
	" a pre-defined set of sessions is not given.",
	showInLibrary=true
)
public class SessionSelectorNode extends OpNode implements NodeSettings {
	
	private final InputField projectField = 
			new InputField("project", "Project for session selection", true, true);
	
	private OutputField projectOutputField = new OutputField("project", "Project", true, Project.class);
	
	private final OutputField sessionOutputField = 
			new OutputField("selected sessions", "Selected sessions", true, List.class);
	
	public SessionSelectorNode() {
		super();
		
		putField(projectField);
		putField(projectOutputField);
		putField(sessionOutputField);
		
		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(final OpContext context) throws ProcessingException {
		// get project
		final Project project = 
				(Project)(context.get(projectField) != null ? context.get(projectField)
						: context.get("_project"));
		if(project == null) throw new ProcessingException("No project available");
		
		Runnable onEDT = () -> {
			final SessionSelector selector = new SessionSelector(project);
			final JScrollPane scroller = new JScrollPane(selector);
			
			JOptionPane.showMessageDialog(CommonModuleFrame.getCurrentFrame(), scroller, 
					"Select sessions", JOptionPane.OK_OPTION);
			context.put(projectOutputField, project);
			context.put(sessionOutputField, selector.getSelectedSessions());
		};
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else
			try {
				SwingUtilities.invokeAndWait(onEDT);
			} catch (InvocationTargetException | InterruptedException e) {
				throw new ProcessingException(e);
			}
	}
	
	@Override
	public Component getComponent(GraphDocument document) {
		return new JPanel();
	}

	@Override
	public Properties getSettings() {
		return new Properties();
	}

	@Override
	public void loadSettings(Properties properties) {
	}

}
