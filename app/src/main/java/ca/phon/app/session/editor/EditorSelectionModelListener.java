/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.session.editor;

public interface EditorSelectionModelListener {
	
	/**
	 * Called when a new selection has been added to the model.
	 * 
	 * @param model
	 * @param selection
	 */
	public void selectionAdded(EditorSelectionModel model, SessionEditorSelection
			selection);
	
	/**
	 * Called when a new selection has been set (clearing other selections)
	 * 
	 * @param model
	 * @param selection
	 */
	public void selectionSet(EditorSelectionModel model, SessionEditorSelection selection);
	
	/**
	 * Called when all selections have been cleared.
	 * 
	 * @param model
	 */
	public void selectionsCleared(EditorSelectionModel model);
	
}
