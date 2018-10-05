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
