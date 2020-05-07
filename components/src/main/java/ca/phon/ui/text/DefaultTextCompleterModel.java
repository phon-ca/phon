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
	
	public boolean containsCompletion(String completion) {
		return (getData(completion) != null);
	}

}
