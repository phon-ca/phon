package ca.phon.app.phonex;

import java.awt.event.ActionEvent;

public class FindAction extends PhonexDebuggerAction {

	public FindAction(PhonexDebugger debugger) {
		super(debugger);
		
		putValue(NAME, "Find");
		putValue(SHORT_DESCRIPTION, "Find instances of expression within given input");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		getDebugger().onFind();
	}

}
