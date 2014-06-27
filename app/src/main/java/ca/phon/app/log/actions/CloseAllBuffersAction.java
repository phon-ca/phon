package ca.phon.app.log.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.BufferWindow;

public class CloseAllBuffersAction extends HookableAction {

	private static final long serialVersionUID = -8345491546953230785L;

	private final static String CMD_NAME = "Close all buffers";
	
	private final static String SHORT_DESC = "Close all open buffers";
	
	public CloseAllBuffersAction() {
		super();
		
		putValue(HookableAction.NAME, CMD_NAME);
		putValue(HookableAction.SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final BufferWindow window = BufferWindow.getInstance();
		window.closeAllBuffers();
	}

}
