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

	public String getFormSuffix() {
		return formSuffix;
	}

	public void setFormSuffix(String formSuffix) {
		this.formSuffix = formSuffix;
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
