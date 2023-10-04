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

import ca.phon.orthography.mor.Pos;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Suffix of word including type form suffix and user special forms
 */
public final class WordSuffix {

	private final boolean separatedPrefix;

	private final WordFormType type;

	private final String formSuffix;

	private final String userSpecialForm;

	private List<Pos> pos;
	
	public WordSuffix(WordFormType type) {
		this(false, type, null, null);
	}

	public WordSuffix(boolean separatedPrefix, WordFormType type, String formSuffix, String userSpecialForm, Pos... pos) {
		this(separatedPrefix, type, formSuffix, userSpecialForm, Arrays.asList(pos));
	}
	
	public WordSuffix(boolean separatedPrefix, WordFormType type, String formSuffix, String userSpecialForm, List<Pos> pos) {
		this.separatedPrefix = separatedPrefix;
		this.type = type;
		this.formSuffix = formSuffix;
		this.userSpecialForm = userSpecialForm;
		this.pos = Collections.unmodifiableList(pos);
	}

	public WordFormType getType() {
		return type;
	}

	public String getUserSpecialForm() {
		return userSpecialForm;
	}

	public List<Pos> getPos() {
		return this.pos;
	}

	public String getFormSuffix() {
		return formSuffix;
	}

	public boolean isSeparatedPrefix() {
		return separatedPrefix;
	}

	public List<Pos> getWordPos() {
		return pos;
	}

	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		if(separatedPrefix)
			buffer.append("#");
		if(type != null)
			buffer.append(type.getCode());
		if(formSuffix != null && formSuffix.length() > 0)
			buffer.append("-").append(formSuffix);
		if(userSpecialForm != null && userSpecialForm.length() > 0)
			buffer.append("@z:").append(userSpecialForm);
		for(Pos pos: pos) {
			buffer.append("$").append(pos);
		}
		return buffer.toString();
	}

}
