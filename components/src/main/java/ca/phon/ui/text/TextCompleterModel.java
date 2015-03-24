package ca.phon.ui.text;

import java.util.List;

public interface TextCompleterModel<T> {
	
	/**
	 * Add completion to model with given data.
	 * 
	 * @param key
	 * @param completion
	 */
	public void addCompletion(String completion, T value);
	
	/**
	 * Remove completion from model.
	 * 
	 * @param completion
	 */
	public void removeCompletion(String key);
	
	public void clearCompletions();
	
	/**
	 * Get data for completion key.
	 * 
	 * @param completion
	 */
	public T getData(String completion);
	
	
	/**
	 * Get text used for display of completion.
	 * 
	 * @return text
	 */
	public String getDisplayText(String completion);
	
	/**
	 * Return completions for given text.
	 * 
	 * @param text
	 */
	public List<String> getCompletions(String text);
	
	/**
	 * Perform completion and return new text.
	 * 
	 * @param text
	 * @param completion
	 * 
	 * @return new text
	 */
	public String completeText(String text, String completion);

}
