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
package ca.phon.ui.text;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ca.phon.formatter.Formatter;
import ca.phon.util.Language;


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
