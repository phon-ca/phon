/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.syllable.pattern;

import java.util.ArrayList;

import ca.phon.syllable.Syllable;

/**
 * Interface for mathching syllables.
 *
 */
public interface SyllableMatcher {
	
	/**
	 * Tells if the given syllable 'matches.'
	 * 
	 * The definition of 'matches' is up to the
	 * implementing class.
	 * 
	 * @param syllable
	 * @return boolean
	 */
	public boolean matches(Syllable syllable);

	/**
	 * If the expression used in this matcher had optional 
	 * groups (e.g., groups of features seperated by '|'
	 * then the matched values will apper in this list.  The
	 * order of the values is the ordering of the optional groups
	 * as read from left to right.
	 * 
	 * @return the matched optional features
	 */
	public ArrayList<ArrayList<String>> getMatchedOptionalFeatures();

	/**
	 * If this matcher is related to a previous matcher, then
	 * the matched optional features can be given here.  Each
	 * value can be referenced by $i where 'i' is the index of
	 * the matched feature.
	 * 
	 * @param matchedOptionalFeatures
	 */
	public void setReplacementTable(ArrayList<ArrayList<String>> matchedOptionalFeatures);
	
}
