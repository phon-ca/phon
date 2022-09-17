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
package ca.phon.syllabifier.opgraph.extensions;

import ca.phon.util.Language;

import java.beans.*;

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
