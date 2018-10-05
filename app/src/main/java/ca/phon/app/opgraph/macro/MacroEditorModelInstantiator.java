/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.opgraph.macro;

import ca.phon.app.opgraph.editor.EditorModelInstantiator;
import ca.phon.app.opgraph.editor.EditorModelInstantiator.EditorModelInstantiatorMenuInfo;
import ca.phon.opgraph.OpGraph;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;

@EditorModelInstantiatorMenuInfo(
		name="Macro",
		tooltip="Create new macro...",
		modelType=MacroOpgraphEditorModel.class
)
public class MacroEditorModelInstantiator implements EditorModelInstantiator, IPluginExtensionPoint<EditorModelInstantiator> {

	@Override
	public MacroOpgraphEditorModel createModel(OpGraph graph) {
		return new MacroOpgraphEditorModel(graph);
	}

	@Override
	public Class<?> getExtensionType() {
		return EditorModelInstantiator.class;
	}

	@Override
	public IPluginExtensionFactory<EditorModelInstantiator> getFactory() {
		return (Object... args) -> this;
	}

}
