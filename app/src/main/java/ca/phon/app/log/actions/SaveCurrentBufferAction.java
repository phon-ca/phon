package ca.phon.app.log.actions;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.BufferWindow;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class SaveCurrentBufferAction extends HookableAction {
	
	private static final Logger LOGGER = Logger
			.getLogger(SaveLogBufferAction.class.getName());
	
	private static final long serialVersionUID = -2827879669257916438L;
	
	private final static String CMD_NAME = "Save buffer";
	
	private final static String SHORT_DESC = "Save buffer to file...";
	
	private final static String ICON = "actions/document-save-as";

	public SaveCurrentBufferAction() {
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final BufferWindow window = BufferWindow.getInstance();
		final BufferPanel panel = window.getCurrentBuffer();
		
		if(panel != null) {
			panel.onSaveBuffer();
		}
	}

}
