/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.script.params.ui;

import javax.swing.*;

import ca.phon.script.params.*;

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
