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
package ca.phon.orthography;

/**
 * An event written in-line with orthography.
 * 
 * Events can have syntax 'type:data' or just
 * 'data'.
 * 
 */
public class OrthoEvent extends AbstractOrthoElement {
	
	private final String type;
	
	private final String data;
	
	public OrthoEvent(String data) {
		this(null, data);
	}
	
	public OrthoEvent(String type, String data) {
		super();
		this.type = type;
		this.data = data;
	}
	
	/**
	 * Get the type of the event.  This is the text before
	 * the first ':' in the event text.
	 * 
	 * @return the type of the event.  Default is 'action' if
	 *  not defined
	 */
	public String getType() {
		return this.type;
	}
	
	/**
	 * Event data
	 * 
	 * @return the data for the event
	 */
	public String getData() {
		return this.data;
	}

	@Override
	public String text() {
		return ("*" + (this.type == null ? "" : this.type + ":") + this.data + "*");
	}
	
	@Override
	public String toString() {
		return text();
	}
	

}
