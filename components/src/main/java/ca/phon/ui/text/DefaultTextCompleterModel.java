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
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import ca.hedlund.tst.TernaryTree;

/**
 * Default model which will replace the entire string with a given completion.
 *
 */
public class DefaultTextCompleterModel implements TextCompleterModel<String> {
	
	private TernaryTree<String> tree = new TernaryTree<>();
	
	private boolean caseSensitive = false;
	
	public void addCompletion(String completion) {
		if(!caseSensitive) completion = completion.toLowerCase();
		addCompletion(completion, completion);
	}

	@Override
	public void addCompletion(String completion, String display) {
		if(!caseSensitive) completion = completion.toLowerCase();
		tree.put(completion, display);
	}

	@Override
	public void removeCompletion(String completion) {
		if(!caseSensitive) completion = completion.toLowerCase();
		tree.remove(completion);
	}

	@Override
	public void clearCompletions() {
		tree.clear();
	}

	@Override
	public List<String> getCompletions(String text) {
		if(!caseSensitive) text = text.toLowerCase();
		final Set<Entry<String, String>> entries = tree.entriesWithPrefix(text);
		
		final List<String> retVal = new ArrayList<>(entries.size());
		for(Entry<String, String> entry:entries) {
			retVal.add(entry.getKey());
		}
		Collections.sort(retVal);
		return retVal;
	}

	@Override
	public String completeText(String text, String completion) {
		return completion;
	}

	@Override
	public String getData(String completion) {
		if(!caseSensitive) completion = completion.toLowerCase();
		return tree.get(completion);
	}

	@Override
	public String getDisplayText(String completion) {
		return getData(completion);
	}

}
