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

import java.util.*;
import java.util.Map.*;

import ca.hedlund.tst.*;

public class TreeTextCompleterModel<T> implements TextCompleterModel<T> {
	
	private TernaryTree<T> tree;
	
	private boolean caseSensitive = false;
	
	private boolean includeInfixEntries = false;
	
	private String separator = null;
	
	public TreeTextCompleterModel() {
		super();
		
		this.tree = new TernaryTree<T>();
	}
	
	public String getSeparator() {
		return this.separator;
	}
	
	public void setSeparator(String separator) {
		this.separator = separator;
	}
	
	public boolean isIncludeInfixEntries() {
		return includeInfixEntries;
	}

	public void setIncludeInfixEntries(boolean includeInfixEntries) {
		this.includeInfixEntries = includeInfixEntries;
	}
	
	@Override
	public void addCompletion(String completion, T value) {
		tree.put(completion, value);
	}

	@Override
	public void removeCompletion(String completion) {
		tree.remove(completion);
	}

	@Override
	public void clearCompletions() {
		tree.clear();
	}

	@Override
	public T getData(String completion) {
		return tree.get(completion);
	}

	@Override
	public String getDisplayText(String completion) {
		return completion;
	}
	
	public boolean containsCompletion(String completion) {
		return (tree.containsKey(completion));
	}
	
	@Override
	public List<String> getCompletions(String text) {
		String prefix = "";
		if(separator != null) {
			int lastIdx = text.lastIndexOf(separator);
			if(lastIdx >= 0) {
				prefix = text.substring(0, lastIdx+1);
				text = text.substring(lastIdx+1);
			}
		}
		
		if(!caseSensitive) text = text.toLowerCase();
		final Set<Entry<String, T>> entries = tree.entriesWithPrefix(text.trim(), caseSensitive);
		
		final List<String> retVal = new ArrayList<>(entries.size());
		for(Entry<String, T> entry:entries) {
			retVal.add(prefix + entry.getKey());
		}
		Collections.sort(retVal);
		
		if(isIncludeInfixEntries()) {
			final List<String> otherCompletions = new ArrayList<>();
			final Set<Entry<String, T>> infixEntries = tree.entriesForKeysContaining(text, caseSensitive);
			for(Entry<String, T> entry:infixEntries) {
				if(!retVal.contains(prefix + entry.getKey()))
					otherCompletions.add(prefix + entry.getKey());
			}
			Collections.sort(otherCompletions);
			retVal.addAll(otherCompletions);
		}
		return retVal;
	}
	
	@Override
	public String completeText(String text, String completion) {
		return completion;
	}
	
}
