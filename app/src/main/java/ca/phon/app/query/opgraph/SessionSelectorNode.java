package ca.phon.app.query.opgraph;

import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
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
import ca.phon.session.Session;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;

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
		if(project == null) throw new ProcessingException(null, "No project available");
		
		final Session session = (Session)context.get("_session");
		context.put(projectOutputField, project);
		if(session != null) {
			context.put(sessionOutputField, Collections.singletonList(session));
		} else {
			Runnable onEDT = () -> {
				final SessionSelector selector = new SessionSelector(project);
				final JScrollPane scroller = new JScrollPane(selector);
				
				
				final DialogHeader header = new DialogHeader("Select Sessions", "");
				
				final JDialog selectorDialog = new JDialog();
				selectorDialog.setModal(true);
				selectorDialog.setTitle("Select Sessions");
				selectorDialog.setLayout(new BorderLayout());
				selectorDialog.add(header, BorderLayout.NORTH);
				selectorDialog.add(scroller, BorderLayout.CENTER);
				
				final JButton okButton = new JButton("Ok");
				okButton.addActionListener((e) -> { 
					selectorDialog.setVisible(false); 
					context.put(projectOutputField, project);
					context.put(sessionOutputField, selector.getSelectedSessions());
				});
				selectorDialog.add(ButtonBarBuilder.buildOkBar(okButton), BorderLayout.SOUTH);
				selectorDialog.getRootPane().setDefaultButton(okButton);
				
				selectorDialog.setSize(500, 600);
				selectorDialog.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
				selectorDialog.setVisible(true);
			};
			if(SwingUtilities.isEventDispatchThread())
				onEDT.run();
			else
				try {
					SwingUtilities.invokeAndWait(onEDT);
				} catch (InvocationTargetException | InterruptedException e) {
					throw new ProcessingException(null, e);
				}
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
