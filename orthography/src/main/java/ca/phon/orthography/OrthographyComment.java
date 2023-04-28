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
 * Comment with syntax
 *  type:data
 *  
 * type is optional
 */
public final class OrthographyComment extends AbstractOrthographyElement {
	
	private final String type;
	
	private final String data;
	
	public OrthographyComment(String data) {
		this(null, data);
	}
	
	public OrthographyComment(String type, String data) {
		super();
		this.type = type;
		this.data = data;
	}
	
	public String getType() {
		return this.type;
	}
	
	public String getData() {
		return this.data;
	}
	
	@Override
	public String text() {
		return ("(" + (this.type == null ? "" : this.type + ":") + this.data + ")");
	}

	@Override
	public String toString() {
		return text();
	}
	
}
