/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
