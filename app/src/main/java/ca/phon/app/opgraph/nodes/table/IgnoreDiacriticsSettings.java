package ca.phon.app.opgraph.nodes.table;

import java.util.Collection;

import ca.phon.ipa.Diacritic;

/**
 * Implemented by inventory settings objects which use the ignore diacritics
 * option set.
 */
public interface IgnoreDiacriticsSettings {

	public boolean isIgnoreDiacritics();
	
	public void setIgnoreDiacritics(boolean ignoreDiacritics);
	
	public boolean isOnlyOrExcept();
	
	public void setOnlyOrExcept(boolean onlyOrExcept);
	
	public Collection<Diacritic> getSelectedDiacritics();
	
	public void setSelectedDiacritics(Collection<Diacritic> selectedDiacritics);
	
}
