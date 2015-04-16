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
package ca.phon.orthography;


/**
 * Comment with syntax
 *  type:data
 *  
 * type is optional
 */
public class OrthoComment extends AbstractOrthoElement {
	
	private final String type;
	
	private final String data;
	
	public OrthoComment(String data) {
		this(null, data);
	}
	
	public OrthoComment(String type, String data) {
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
