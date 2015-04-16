/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
