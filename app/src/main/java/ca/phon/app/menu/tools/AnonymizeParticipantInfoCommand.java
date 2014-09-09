package ca.phon.app.menu.tools;

import javax.swing.Action;

import ca.phon.app.project.AnonymizeParticipantInfoEP;
import ca.phon.plugin.PluginAction;

public class AnonymizeParticipantInfoCommand extends PluginAction {

	private static final long serialVersionUID = -3731528615504791889L;

	public AnonymizeParticipantInfoCommand() {
		super(AnonymizeParticipantInfoEP.EP_NAME);
		putValue(Action.NAME, "Strip Participant Information");
		putValue(Action.SHORT_DESCRIPTION, "Remove participant information from sessions");
	}
	
}
