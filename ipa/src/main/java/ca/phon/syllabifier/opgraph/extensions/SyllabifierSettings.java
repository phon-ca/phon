/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.syllabifier.opgraph.extensions;

import java.beans.*;

import ca.phon.util.Language;

/**
 * Name and language settings for a syllabifier.
 */
public class SyllabifierSettings {
	
	private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
	
	private final String NAME_PROP = 
			SyllabifierSettings.class.getName() + ".name";
	
	private final String LANG_PROP = 
			SyllabifierSettings.class.getName() + ".lang";
	
	/**
	 * Name
	 */
	private String name = "";
	
	/**
	 * Language
	 */
	private Language language = new Language();
	
	public SyllabifierSettings() {
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		final String oldName = this.name;
		this.name = name;
		propSupport.firePropertyChange(NAME_PROP, oldName, name);
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		final Language oldLang = this.language;
		this.language = language;
		propSupport.firePropertyChange(LANG_PROP, oldLang, language);
	}

	public void addPropertyChangeListener(PropertyChangeListener arg0) {
		propSupport.addPropertyChangeListener(arg0);
	}

	public void addPropertyChangeListener(String arg0,
			PropertyChangeListener arg1) {
		propSupport.addPropertyChangeListener(arg0, arg1);
	}

	public void removePropertyChangeListener(PropertyChangeListener arg0) {
		propSupport.removePropertyChangeListener(arg0);
	}

	public void removePropertyChangeListener(String arg0,
			PropertyChangeListener arg1) {
		propSupport.removePropertyChangeListener(arg0, arg1);
	}

	
}
