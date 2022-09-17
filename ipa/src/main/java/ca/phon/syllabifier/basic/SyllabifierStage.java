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
package ca.phon.syllabifier.basic;

import ca.phon.ipa.IPAElement;

import java.util.List;

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
