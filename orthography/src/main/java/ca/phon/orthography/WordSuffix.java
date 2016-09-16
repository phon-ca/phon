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

public class WordSuffix {
	
	private WordSuffixType type;
	
	private String formSuffix;
	
	private String code;
	
	public WordSuffix(WordSuffixType type) {
		this(type, null, null);
	}
	
	public WordSuffix(WordSuffixType type, String formSuffix, String code) {
		this.type = type;
		this.formSuffix = formSuffix;
		this.code = code;
	}

	public WordSuffixType getType() {
		return type;
	}

	public void setType(WordSuffixType type) {
		this.type = type;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append(type.getCode());
		if(formSuffix != null && formSuffix.length() > 0)
			buffer.append("-").append(formSuffix);
		if(code != null && code.length() > 0)
			buffer.append(":").append(code);
		return buffer.toString();
	}

}
