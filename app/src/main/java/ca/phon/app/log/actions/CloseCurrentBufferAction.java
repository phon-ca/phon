package ca.phon.app.log.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.BufferWindow;

public class CloseCurrentBufferAction extends HookableAction {

	private static final long serialVersionUID = 1L;

	private final static String CMD_NAME = "Close";
	
	private final static String SHORT_DESC = "Close buffer";
	
	public CloseCurrentBufferAction() {
		super();
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	} 
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final BufferWindow window = BufferWindow.getInstance();
		window.closeCurrentBuffer();
	}

}
