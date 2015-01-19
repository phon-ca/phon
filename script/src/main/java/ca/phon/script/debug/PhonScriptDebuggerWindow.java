package ca.phon.script.debug;

import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.tools.debugger.Main;

public class PhonScriptDebuggerWindow extends Main {

	public PhonScriptDebuggerWindow(ContextFactory factory, String title) {
		super(title);
	}

}
