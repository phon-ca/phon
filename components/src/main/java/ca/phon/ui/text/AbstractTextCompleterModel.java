package ca.phon.ui.text;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractTextCompleterModel<T> implements TextCompleterModel<T> {
	
	private Map<String, T> completions = new LinkedHashMap<>();

	@Override
	public void addCompletion(String completion, T value) {
		completions.put(completion, value);
	}

	@Override
	public void removeCompletion(String key) {
		completions.remove(key);
	}

	@Override
	public void clearCompletions() {
		completions.clear();
	}

	@Override
	public T getData(String completion) {
		return completions.get(completion);
	}

	@Override
	public String getDisplayText(String completion) {
		return completion;
	}

}
