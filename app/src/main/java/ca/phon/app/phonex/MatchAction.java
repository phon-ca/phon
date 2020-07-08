package ca.phon.app.phonex;

import java.awt.event.ActionEvent;

public class MatchAction extends PhonexDebuggerAction {

	public MatchAction(PhonexDebugger debugger) {
		super(debugger);
		
		putValue(NAME, "Match");
		putValue(SHORT_DESCRIPTION, "Run phonex expression with given input");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		getDebugger().onMatch();
	}

}
