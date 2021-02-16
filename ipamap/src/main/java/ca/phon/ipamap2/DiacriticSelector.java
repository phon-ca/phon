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
package ca.phon.ipamap2;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import ca.phon.ipa.*;

public class DiacriticSelector extends IPAMapSelector {

	private final static float DEFAULT_FONT_SIZE = 20.0f;
	
	private final static String DIACRITICS_FILE = "diacritics.xml";
	
	public DiacriticSelector() {
		super();

		setSectionVisible("Other Consonants", false);
		setSectionVisible("Other Vowels", false);
		setSectionVisible("Other Symbols", false);

		getMapGridContainer().setFont(IPAMap.getDefaultIPAMapFont().deriveFont(DEFAULT_FONT_SIZE));
		revalidate();
	}
	
	@Override
	protected void loadGrids() {
		ipaGrids = new IPAGrids();
		try {
			ipaGrids.loadGridData(DiacriticSelector.class.getResourceAsStream(DIACRITICS_FILE));
		} catch (IOException e) {
			e.printStackTrace();
		}
		ipaGrids.generateMissingGrids();
		
		addGrids(ipaGrids);
	}
	
	public Set<Diacritic> getSelectedDiacritics() {
		final IPAElementFactory factory = new IPAElementFactory();
		return getSelected().stream()
				.map( (str) -> {
					var ch = str.replaceAll("\u25cc", "").charAt(0);
					return factory.createDiacritic(ch);
				}).collect(Collectors.toSet());
	}
	
	public void setSelectedDiacritics(Collection<Diacritic> diacritics) {
		setSelected(diacritics.stream().map(Diacritic::toString).collect(Collectors.toList()));
	}
	
}
