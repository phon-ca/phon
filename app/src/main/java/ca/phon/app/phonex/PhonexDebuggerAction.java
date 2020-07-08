package ca.phon.app.phonex;

import java.awt.event.ActionEvent;

import ca.phon.app.hooks.HookableAction;

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
