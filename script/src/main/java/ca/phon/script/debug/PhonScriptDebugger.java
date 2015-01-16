package ca.phon.script.debug;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.debug.DebugFrame;
import org.mozilla.javascript.debug.DebuggableScript;
import org.mozilla.javascript.debug.Debugger;


public class PhonScriptDebugger implements Debugger {

	@Override
	public DebugFrame getFrame(Context arg0, DebuggableScript arg1) {
		return new PhonScriptDebugFrame(arg1);
	}

	@Override
	public void handleCompilationDone(Context arg0, DebuggableScript arg1,
			String arg2) {
		
	}
 
}
