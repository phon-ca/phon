/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.session.editor.view.common;

import java.util.List;

import javax.swing.JComponent;

/**
 * Interface used to load tier editors.
 */
public interface TierEditor {

	/**
	 * Get the editor component
	 * 
	 * @return component
	 */
	public JComponent getEditorComponent();
	
	/**
	 * Add tier editor listener
	 */
	public void addTierEditorListener(TierEditorListener listener);
	
	/**
	 * remove tier editor listener
	 */
	public void removeTierEditorListener(TierEditorListener listener);
	
	/**
	 * Get tier editor listeners
	 */
	public List<TierEditorListener> getTierEditorListeners();
	
}
