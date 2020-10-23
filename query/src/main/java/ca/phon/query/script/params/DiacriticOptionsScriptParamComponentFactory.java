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
package ca.phon.query.script.params;

import javax.swing.*;

import ca.phon.plugin.*;
import ca.phon.script.params.*;
import ca.phon.script.params.ui.*;

public class DiacriticOptionsScriptParamComponentFactory 
	implements ScriptParamComponentFactory, IPluginExtensionPoint<ScriptParamComponentFactory>{

	@Override
	public Class<?> getExtensionType() {
		return ScriptParamComponentFactory.class;
	}

	@Override
	public IPluginExtensionFactory<ScriptParamComponentFactory> getFactory() {
		return (args) -> this;
	}

	@Override
	public boolean canCreateScriptParamComponent(ScriptParam scriptParam) {
		return (scriptParam instanceof DiacriticOptionsScriptParam);
	}

	@Override
	public JComponent createScriptParamComponent(ScriptParam scriptParam) {
		DiacriticOptionsPanel retVal = new DiacriticOptionsPanel((DiacriticOptionsScriptParam)scriptParam);
		
		
		
		return retVal;
	}
	
}
