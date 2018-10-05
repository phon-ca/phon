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
