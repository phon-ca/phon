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
package ca.phon.app.opgraph.editor;

/**
 * Basic node editor settings.
 * 
 */
public class NodeEditorSettings {
	
	private String modelType = "ca.phon.app.opgraph.editor.MacroOpgraphEditorModel";
	
	private boolean generated = false;
	
	public NodeEditorSettings() {
		
	}

	/**
	 * Returns the {@link OpgraphEditorModel} implementation
	 * used by the graph.
	 * 
	 * @return editor model type
	 */
	public String getModelType() {
		return modelType;
	}

	public void setModelType(String modelType) {
		this.modelType = modelType;
	}
	
	/**
	 * Returns <code>true</code> if this graph was
	 * generated instead of created in the editor.
	 * 
	 * Some methods may handle 'dynamic' graphs
	 * differently.
	 * 
	 * @return boolean
	 */
	public boolean isGenerated() {
		return this.generated;
	}
	
	public void setGenerated(boolean generated) {
		this.generated = generated;
	}
	

}
