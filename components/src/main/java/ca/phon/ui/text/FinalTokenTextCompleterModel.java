/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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

import java.util.*;

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
