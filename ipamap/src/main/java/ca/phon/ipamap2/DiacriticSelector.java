package ca.phon.ipamap2;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import ca.phon.ipa.*;

public class DiacriticSelector extends IPAMapSelector {
	
	private final static String DIACRITICS_FILE = "diacritics.xml";
	
	public DiacriticSelector() {
		super();

		setSectionVisible("Other Consonants", false);
		setSectionVisible("Other Vowels", false);
		setSectionVisible("Other Symbols", false);
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
