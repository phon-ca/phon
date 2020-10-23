package ca.phon.app.log.actions;

import java.awt.event.*;

import ca.phon.app.hooks.*;
import ca.phon.app.log.*;
import ca.phon.ui.action.*;

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
