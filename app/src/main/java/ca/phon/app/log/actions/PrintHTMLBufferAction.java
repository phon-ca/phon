package ca.phon.app.log.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.BufferPanel;
import ca.phon.ui.action.PhonUIAction;

public class PrintHTMLBufferAction extends HookableAction {

	private static final long serialVersionUID = 9121919367383017713L;

	private final BufferPanel bufferPanel;
	
	public PrintHTMLBufferAction(BufferPanel bufferPanel) {
		super();
		this.bufferPanel = bufferPanel;
		
		putValue(PhonUIAction.NAME, "Print");
		putValue(PhonUIAction.SHORT_DESCRIPTION, "Print HTML");
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		if(bufferPanel.isShowingHtml())
			bufferPanel.getBrowser().print();
	}

}
