package ca.phon.script.params.ui;

import java.lang.ref.WeakReference;

import javax.swing.AbstractAction;

import ca.phon.script.params.ScriptParam;

/**
 * Abstract class for script parameter actions.
 */
public abstract class ScriptParamAction extends AbstractAction {
	
	private static final long serialVersionUID = 4191137819061729454L;
	
	private final WeakReference<ScriptParam> paramRef;
	
	private final String paramId;
	
	public ScriptParamAction(ScriptParam param, String id) {
		super();
		this.paramRef = new WeakReference<ScriptParam>(param);
		this.paramId = id;
	}

	public String getParamId() {
		return paramId;
	}
	
	public ScriptParam getScriptParam() {
		return paramRef.get();
	}

}
