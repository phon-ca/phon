package ca.phon.ipamap2;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ca.phon.ipa.Diacritic;
import ca.phon.ipa.IPAElementFactory;
import ca.phon.ui.tristatecheckbox.TristateCheckBox;

public class DiacriticSelector extends IPAMapSelector {
	
	public DiacriticSelector() {
		super();

		init();
	}
	
	private void init() {
		// remove all non-diacritic sections
		setSectionVisible("Consonants", false);
		setSectionVisible("Clicks and Implosives", false);
		setSectionVisible("Vowels", false);
		setSectionVisible("Suprasegmentals", false);
		setSectionVisible("ExtIPA", false);
		setSectionVisible("Other Consonants", false);
		setSectionVisible("Other Vowels", false);
		setSectionVisible("Other Symbols", false);
		
		
	}
	
	public Set<Diacritic> getSelectedDiacritics() {
		final IPAElementFactory factory = new IPAElementFactory();
		return getSelected().stream()
				.map( (str) -> {
					var ch = str.replaceAll("\u25cc", "").charAt(0);
					return factory.createDiacritic(ch);
				}).collect(Collectors.toSet());
	}
	
}
