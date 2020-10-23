package ca.phon.app.phonex;

import ca.phon.app.hooks.*;

public abstract class PhonexDebuggerAction extends HookableAction {

	private final PhonexDebugger debugger;
	
	public PhonexDebuggerAction(PhonexDebugger debugger) {
		super();
		this.debugger = debugger;
	}
	
	public PhonexDebugger getDebugger() {
		return this.debugger;
	}

}
