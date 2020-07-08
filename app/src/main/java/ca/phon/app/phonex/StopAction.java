package ca.phon.app.phonex;

import java.awt.event.ActionEvent;

public class StopAction extends PhonexDebuggerAction {

	public StopAction(PhonexDebugger debugger) {
		super(debugger);
		
		putValue(NAME, "Stop");
		putValue(SHORT_DESCRIPTION, "Stop debugger");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		getDebugger().onStop();
	}
	
}
