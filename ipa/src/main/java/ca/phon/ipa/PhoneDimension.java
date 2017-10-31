package ca.phon.ipa;

/**
 * Phonetic dimensions
 *
 */
public enum PhoneDimension {
	PLACE(2, new String[]{ 
			"{labial}",
			"{interdental}", 
			"{alveolar}", 
			"{alveopalatal}", 
			"{lateral}", 
			"{retroflex}", 
			"{palatal}",
			"{velar}", 
			"{uvular}",
			"{pharyngeal}", 
			"{laryngeal}" },
		new String[]{
			"Labial",
			"Interdental",
			"Alveolar",
			"Alveopalatal",
			"Lateral",
			"Retroflex",
			"Palatal",
			"Velar",
			"Uvular",
			"Pharyngeal",
			"Laryngeal"}
		),
	MANNER(2, new String[]{ 
			"{stop, -nasal}",
			"{fricative}", 
			"{affricate}", 
			"{nasal}",
			"{lateral}", 
			"{rhotic}", 
			"{glide}", 
			"{vowel}" },
		new String[]{
			"Stop",
			"Fricative",
			"Affricate",
			"Nasal",
			"Lateral",
			"Rhotic",
			"Glide",
			"Vowel"
		}),
	VOICING(1, new String[]{ 
			"{voiceless, -aspirated}",
			"{voiced, -aspirated}",
			"{voiceless, aspirated}", 
			"{voiced, aspirated}" },
		new String[]{
			"Voiceless",
			"Voiced",
			"VoicelessAsp",
			"VoicedAsp"
		}),
	
	/* Vowels */
	HEIGHT(3, new String[]{ "{high}", "{mid}", "{low}" },
			new String[]{"High", "Mid", "Low"}),
	BACKNESS(2, new String[]{ "{front}", "{central}", "{back}" },
			new String[]{"Front", "Central", "Back"}),
	TENSENESS(2, new String[]{ "{tense}", "{lax}" },
			new String[]{"Tense", "Lax"}),
	ROUNDING(1, new String[]{ "{round}", "{-round}" },
			new String[]{"Round", "Not round"});
	
	private int weight;
	
	private String[] categories;
	
	private String[] labels;
	
	private PhoneDimension(int weight, String[] categories, String[] labels) {
		this.weight = weight;
		this.categories = categories;
		this.labels = labels;
	}
	
	public String[] getCategories() {
		return this.categories;
	}
	
	public String[] getCategoryLabels() {
		return this.labels;
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
	
	public int getWeight() {
		return this.weight;
	}
	
	public String getCategory(IPAElement ele) {
		int idx = getCategoryIndex(ele);
		if(idx >= 0)
			return categories[idx];
		else
			return null;
	}
	
	public String getCategoryLabel(IPAElement ele) {
		int idx = getCategoryIndex(ele);
		if(idx >= 0)
			return labels[idx];
		else
			return null;
	}
	
	@Override
	public String toString() {
		final String name = super.toString();
		return name.charAt(0) + name.substring(1).toLowerCase();
	}
	
}
