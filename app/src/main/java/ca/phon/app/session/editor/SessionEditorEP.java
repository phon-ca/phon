package ca.phon.app.session.editor;

import java.util.Map;

import javax.swing.SwingUtilities;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.project.ProjectFrameExtension;
import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;
import ca.phon.session.Session;

/**
 * SessionEditor entry point
 * 
 * 
 */
@PhonPlugin(name="Session Info")
public class SessionEditorEP implements IPluginEntryPoint {
	
	private final static String EP_NAME = "SessionEditor";

	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		final EntryPointArgs epArgs = new EntryPointArgs(args);
		final Project project = epArgs.getProject();
		final Session session = epArgs.getSession();
		
		final Runnable onEdt = new Runnable() {
			public void run() {
				showEditor(project, session);
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			onEdt.run();
		else
			SwingUtilities.invokeLater(onEdt);
	}

	public void showEditor(Project project, Session session) {
		final SessionEditor editor = new SessionEditor(session);
		final ProjectFrameExtension pfe = new ProjectFrameExtension(project);
		editor.putExtension(ProjectFrameExtension.class, pfe);
		
		editor.pack();
		editor.setLocationByPlatform(true);
		editor.setVisible(true);
	}
}
