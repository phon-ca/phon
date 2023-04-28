/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.orthography;

/**
 * An event written in-line with orthography.
 * 
 * Events can have syntax 'type:data' or just
 * 'data'.
 * 
 */
public class OrthographyEvent extends AbstractOrthographyElement {
	
	private final String type;
	
	private final String data;
	
	public OrthographyEvent(String data) {
		this(null, data);
	}
	
	public OrthographyEvent(String type, String data) {
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
