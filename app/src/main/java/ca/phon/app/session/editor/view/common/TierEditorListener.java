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
package ca.phon.app.session.editor.view.common;

import ca.phon.session.Tier;

/**
 * Listener interface for tier editors.
 *
 */
public interface TierEditorListener {
	
	/**
	 * Called when the value of a tier changes.
	 * 
	 * @param tier
	 * @param groupIndex
	 * @param newValue
	 * @param oldValue
	 */
	public <T> void tierValueChange(Tier<T> tier, int groupIndex, T newValue, T oldValue);
	
	/**
	 * Called when the value of a tier has changed and focus has left the tier editor.
	 * 
	 * @param tier
	 * @param groupIndex
	 * @param newValue
	 * @param oldValue
	 */
	public <T> void tierValueChanged(Tier<T> tier, int groupIndex, T newValue, T oldValue);

}
