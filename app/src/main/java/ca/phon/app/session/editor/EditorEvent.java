/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

/**
 * Entity class for editor events.
 */
public class EditorEvent {

	/** Event name */
	private String evtName;

	/** Source */
	private Object source;

	/** Event data */
	private Object evtData;

	public EditorEvent() {
		super();
	}

	public EditorEvent(String evtName) {
		this(evtName, null, null);
	}

	public EditorEvent(String evtName, Object src) {
		this(evtName, src, null);
	}

	public EditorEvent(String evtName, Object src, Object data) {
		super();

		this.evtName = evtName;
		this.source = src;
		this.evtData = data;
	}

	/** Get/Set */
	public String getEventName() {
		return evtName;
	}

	public void setEventName(String evtName) {
		this.evtName = evtName;
	}

	public Object getSource() {
		return source;
	}

	public void setSource(Object src) {
		this.source = src;
	}

	public Object getEventData() {
		return this.evtData;
	}

	public void setEventData(Object data) {
		this.evtData = data;
	}

}
