/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.syllabifier.basic;

import java.util.List;

import ca.phon.ipa.IPAElement;

public interface SyllabifierStage {

	/**
	 * Run syllabifier stage on given list of phones.
	 * 
	 * @param phones
	 * @return <code>true</code> if any {@link IPAElement}s have been
	 *  marked, <code>false</code> otherwise
	 */
	public boolean run(List<IPAElement> phones);
	
	/**
	 * Tells the syllabifier if this stage should be executed until
	 * run() returns false.
	 * 
	 * @return <code>true</code> if stage should be repeated, <code>false</code>
	 *  otherwise
	 */
	public boolean repeatWhileChanges();
	
	/**
	 * Return name of the stage.
	 * 
	 * @return stage name
	 */
	public String getName();
	
}
