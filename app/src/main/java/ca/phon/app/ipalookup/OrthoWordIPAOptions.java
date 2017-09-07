/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.ipalookup;

import java.util.*;

import ca.phon.extensions.Extension;
import ca.phon.orthography.OrthoWord;
import ca.phon.util.Language;

/**
 * Extension for {@link OrthoWord} objects providing
 * a list of transcriptions for the given Orthography
 *
 */
@Extension(value=OrthoWord.class)
public class OrthoWordIPAOptions {
	
	private int selectedOption = 0;
	
	private final List<String> options = Collections.synchronizedList(new ArrayList<String>());
	
	private Language dictLang;
	

	public OrthoWordIPAOptions() {
		super();
	}
	
	public OrthoWordIPAOptions(String[] opts) {
		this(Arrays.asList(opts));
	}
	
	public OrthoWordIPAOptions(List<String> opts) {
		super();
		this.options.addAll(opts);
	}

	public Language getDictLang() {
		return dictLang;
	}
	
	public void setDictLang(Language dictLang) {
		this.dictLang = dictLang;
	}
	
	public List<String> getOptions() {
		return Collections.unmodifiableList(options);
	}

	public void setOptions(List<String> options) {
		this.options.clear();
		this.options.addAll(options);
	}
	
	public int getSelectedOption() {
		return this.selectedOption;
	}
	
	public void setSelectedOption(int option) {
		this.selectedOption = option;
	}
	
}
