package ca.phon.orthography;

public class WordPrefix {
	
	private WordPrefixType type;
	
	public WordPrefix(WordPrefixType type) {
		this.type = type;
	}
	
	public WordPrefixType getType() {
		return type;
	}
	
	public void setType(WordPrefixType type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return getType().getCode();
	}

}
