package ca.phon.ipa;

/**
 * Phonetic dimensions
 *
 */
public enum PhoneDimension {
	PLACE(new String[]{ 
		"{labial}",
		"{coronal, anterior}", 
		"{coronal, posterior}", 
		"{interdental}", 
		"{alveolar}", 
		"{alveopalatal}", 
		"{lateral}", 
		"{retroflex}", 
		"{palatal}",
		"{velar}", 
		"{uvular}",
		"{pharyngeal}", 
		"{laryngeal}" }),
	MANNER(new String[]{ 
		"{stop, -nasal}", 
		"{fricative}", 
		"{affricate}", 
		"{nasal}",
		"{liquid, lateral}", 
		"{rhotic}", 
		"{glide}", 
		"{vowel}" }),
	VOICING(new String[]{ 
		"{voiceless, -aspirated}", 
		"{voiceless, aspirated}", 
		"{voiced}" }),
	
	/* Vowels */
	HEIGHT(new String[]{ "{high}", "{mid}", "{low}" }),
	BACKNESS(new String[]{ "{front}", "{central}", "{back}" }),
	TENSENESS(new String[]{ "{tense}", "{lax}" }),
	ROUNDING(new String[]{ "{round}", "{-round}" });
	
	private String[] categories;
	
	private PhoneDimension(String[] categories) {
		this.categories = categories;
	}
	
	public String[] getCategories() {
		return this.categories;
	}
	
	public int getCategoryIndex(IPAElement ele) {
		int retVal = -1;
		
		if((ele.getFeatureSet().hasFeature("Consonant") && ordinal() < 3) || 
				(ele.getFeatureSet().hasFeature("Vowel") && ordinal() >= 3)) {
			for(int i = 0; i < categories.length; i++) {
				final IPATranscript t = (new IPATranscriptBuilder()).append(ele).toIPATranscript();
				if(t.matches(categories[i])) {
					retVal = i;
					break;
				}
			}
		}
		
		return retVal;
	}
	
	public String getCategory(IPAElement ele) {
		int idx = getCategoryIndex(ele);
		if(idx >= 0)
			return categories[idx];
		else
			return null;
	}
	
}
