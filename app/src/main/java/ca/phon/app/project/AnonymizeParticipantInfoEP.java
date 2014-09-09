package ca.phon.app.project;

import java.util.Map;

import javax.swing.SwingUtilities;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;

/**
 * Entry point for module to strip participant information.
 *
 */
@PhonPlugin(name="default")
public class AnonymizeParticipantInfoEP implements IPluginEntryPoint {

	public final static String EP_NAME = "AnonymizeParticipantInformation";
	
	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		final EntryPointArgs info = new EntryPointArgs(args);
		final Project project = info.getProject();
		
		if(project == null) return;
		
		final Runnable onEDT = new Runnable() {
			
			@Override
			public void run() {
				final AnonymizeParticipantInfoWizard wizard = new AnonymizeParticipantInfoWizard(project);
				wizard.setSize(500, 600);
				wizard.centerWindow();
				wizard.showWizard();
			}
			
		};
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else
			SwingUtilities.invokeLater(onEDT);
	}

}
