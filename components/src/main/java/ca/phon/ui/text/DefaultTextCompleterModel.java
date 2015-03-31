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
	
	public void addCompletion(String completion) {
		addCompletion(completion, completion);
	}

	@Override
	public void addCompletion(String completion, String display) {
		tree.put(completion, display);
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
	public List<String> getCompletions(String text) {
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
		return tree.get(completion);
	}

	@Override
	public String getDisplayText(String completion) {
		return getData(completion);
	}

}
