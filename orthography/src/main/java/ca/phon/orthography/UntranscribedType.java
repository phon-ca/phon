package ca.phon.orthography;

/**
 * Word which have not been transcribed.
 *
 */
public enum UntranscribedType {
	UNINTELLIGIBLE("xxx", "unintelligible"),
	UNINTELLIGIBLE_WORD_WITH_PHO("yyy", "unintelligible-with-pho"),
	UNTRANSCRIBED("www", "untranscribed");
	
	private String code;
	
	private String displayName;
	
	private UntranscribedType(String code, String displayName) {
		this.code = code;
		this.displayName = displayName;
	}

	public String getCode() {
		return this.code;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}

	public static UntranscribedType fromCode(String code) {
		UntranscribedType retVal = null;
		
		for(UntranscribedType v:values()) {
			if(v.getCode().equals(code)) {
				retVal = v;
				break;
			}
		}
		
		return retVal;
	}

}
