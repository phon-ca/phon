package ca.phon.orthography;

public class WordSuffix {
	
	private WordSuffixType type;
	
	private String formSuffix;
	
	private String code;
	
	public WordSuffix(WordSuffixType type) {
		this(type, null, null);
	}
	
	public WordSuffix(WordSuffixType type, String formSuffix, String code) {
		this.type = type;
		this.formSuffix = formSuffix;
		this.code = code;
	}

	public WordSuffixType getType() {
		return type;
	}

	public void setType(WordSuffixType type) {
		this.type = type;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append(type.getCode());
		if(formSuffix != null && formSuffix.length() > 0)
			buffer.append("-").append(formSuffix);
		if(code != null && code.length() > 0)
			buffer.append(":").append(code);
		return buffer.toString();
	}

}
