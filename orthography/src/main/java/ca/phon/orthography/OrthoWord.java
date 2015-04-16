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
 * A word in an orthographic phrase.
 * Words may have prefix and/or suffix
 * codes.
 */
public class OrthoWord extends AbstractOrthoElement {
	
	private final WordPrefix prefix;
	
	private final WordSuffix suffix;
	
	private final String data;
	
	public OrthoWord(String data) {
		this(data, null, null);
	}

	public OrthoWord(String data, WordPrefix prefix) {
		this(data, prefix, null);
	}
	
	public OrthoWord(String data, WordSuffix suffix) {
		this(data, null, suffix);
	}
	
	public OrthoWord(String data, WordPrefix prefix, WordSuffix suffix) {
		super();
		this.prefix = prefix;
		this.suffix = suffix;
		this.data = data;
	}
	
	/**
	 * Get prefix for word.
	 * 
	 * @return the word prefix, or <code>null</code> if
	 *  none
	 */
	public WordPrefix getPrefix() {
		return this.prefix;
	}
	
	/**
	 * Get suffix for word.
	 * 
	 * @return the word suffix, or <code>null</code> if
	 *  none
	 */
	public WordSuffix getSuffix() {
		return this.suffix;
	}
	
	/**
	 * Get the root word data (without prefix/suffix)
	 * 
	 * @return the root word 
	 */
	public String getWord() {
		return this.data;
	}
	
	@Override
	public String text() {
		return (
			(this.prefix == null ? "" : this.prefix.getCode()) + 
			this.data + 
			(this.suffix == null ? "" : "@" + this.suffix.getCode())
		);
	}
	
	@Override
	public String toString() {
		return text();
	}
	
}
