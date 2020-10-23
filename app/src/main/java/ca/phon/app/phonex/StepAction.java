package ca.phon.app.phonex;

import java.awt.event.*;

public class StepAction extends PhonexDebuggerAction {

	public StepAction(PhonexDebugger debugger) {
		super(debugger);
		
		putValue(NAME, "Step");
		putValue(SHORT_DESCRIPTION, "Debug step");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		getDebugger().onStep();
	}

}
