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
