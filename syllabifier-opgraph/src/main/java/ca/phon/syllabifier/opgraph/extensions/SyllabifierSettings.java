package ca.phon.syllabifier.opgraph.extensions;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

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
