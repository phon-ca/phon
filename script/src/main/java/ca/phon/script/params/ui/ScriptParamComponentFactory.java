package ca.phon.script.params.ui;

import javax.swing.JComponent;

import ca.phon.script.params.ScriptParam;

/**
 * Extension point for custom script parameters implementations.
 * 
 *
 */
public interface ScriptParamComponentFactory {
	
	/**
	 * Does this implementation handle the given parameter?
	 * 
	 * @param scriptParam
	 * @return <code>true</code> if this factory can create a component
	 *  for the given scriptParam, <code>false</code> otherwise
	 */
	public boolean canCreateScriptParamComponent(ScriptParam scriptParam);
	
	/**
	 * Create the UI component for the given script parameter.
	 * 
	 * @param scriptParam
	 * @return the component or <code>null</code> if canCreateScriptParam 
	 *  returns <code>false</code>
	 */
	public JComponent createScriptParamComponent(ScriptParam scriptParam);

}
