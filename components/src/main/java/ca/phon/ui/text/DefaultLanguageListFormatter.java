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
package ca.phon.ui.text;

import ca.phon.formatter.Formatter;
import ca.phon.util.Language;

import java.text.ParseException;
import java.util.*;


public class DefaultLanguageListFormatter implements Formatter<List<Language>> {

	private char tokenChar = ' ';
	
	public DefaultLanguageListFormatter() {
		this(' ');
	}
	
	public DefaultLanguageListFormatter(char tokenChar) {
		this.tokenChar = tokenChar;
	}
	
	@Override
	public String format(List<Language> langs) {
		StringBuilder builder = new StringBuilder();
		for(Language lang:langs) {
			if(builder.length() > 0)
				builder.append(tokenChar);
			builder.append(lang.toString());
		}
		return builder.toString();
	}

	@Override
	public List<Language> parse(String text) throws ParseException {
		List<Language> retVal = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(text, tokenChar+"");
		int idx = 0;
		while(st.hasMoreTokens()) {
			String token = st.nextToken();
			try {
				Language lang = Language.parseLanguage(token);
				retVal.add(lang);
			} catch (IllegalArgumentException pe) {
				throw new ParseException(pe.getLocalizedMessage(), idx);
			}
			idx += token.length() + 1;
		}
		return retVal;
	}

}
