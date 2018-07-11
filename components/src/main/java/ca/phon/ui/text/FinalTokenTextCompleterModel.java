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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Text completion model which replaces the final token
 * in a string.
 */
public class FinalTokenTextCompleterModel extends DefaultTextCompleterModel {

	@Override
	public List<String> getCompletions(String text) {
		final StringTokenizer st = new StringTokenizer(text);
		String lastToken = null;
		while(st.hasMoreTokens()) lastToken = st.nextToken();
		
		final List<String> retVal = new ArrayList<>();
		if(lastToken != null) {
			retVal.addAll(super.getCompletions(lastToken));
		}
		return retVal;
	}

	@Override
	public String completeText(String text, String completion) {
		final StringTokenizer st = new StringTokenizer(text);
		final StringBuilder sb = new StringBuilder();
		
		while(st.hasMoreTokens()) {
			String token = st.nextToken();
			if(st.hasMoreTokens()) {
				if(sb.length() > 0) sb.append(" ");
				sb.append(token);
			}
		}
		if(sb.length() > 0) sb.append(" ");
		sb.append(completion);
		
		return sb.toString();
	}

}
