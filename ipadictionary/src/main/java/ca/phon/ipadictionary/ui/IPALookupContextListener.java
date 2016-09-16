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
package ca.phon.ipadictionary.ui;

/**
 * Listener interface for IPALookupContext
 *
 */
public interface IPALookupContextListener {

	/**
	 * Fired when the selected dictionary changes.
	 * 
	 */
	public void dictionaryChanged(String newDictionary);
	
	/**
	 * Fired when a new dictionary is added.
	 * 
	 */
	public void dictionaryAdded(String newDictionary);
	
	/**
	 * Fired when a dictionary is dropped
	 */
	public void dictionaryRemoved(String dictName);

	/**
	 * Fired when a new messages is available from
	 * the lookup context.
	 */
	public void handleMessage(String msg);

	/**
	 * Fired when a new error occurs
	 */
	public void errorOccured(String err);
	
}
