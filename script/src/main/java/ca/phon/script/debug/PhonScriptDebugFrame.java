package ca.phon.script.debug;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.debug.DebugFrame;
import org.mozilla.javascript.debug.DebuggableScript;

public class PhonScriptDebugFrame implements DebugFrame {
	
	private final static Logger LOGGER = Logger.getLogger(PhonScriptDebugFrame.class.getName());
	
	private DebuggableScript fnOrScript;
	
	public PhonScriptDebugFrame(DebuggableScript fnOrScript) {
		this.fnOrScript = fnOrScript;
	}

	@Override
	public void onDebuggerStatement(Context arg0) {
	}

	@Override
	public void onEnter(Context arg0, Scriptable arg1, Scriptable arg2,
			Object[] arg3) {
		
	}

	@Override
	public void onExceptionThrown(Context arg0, Throwable arg1) {
		LOGGER.log(Level.SEVERE, arg1.getLocalizedMessage(), arg1);
	}

	@Override
	public void onExit(Context arg0, boolean arg1, Object arg2) {
		
	}

	@Override
	public void onLineChange(Context arg0, int arg1) {
		System.out.println(fnOrScript.getFunctionName() + "(" + arg1 + ")");
	}
	
}
