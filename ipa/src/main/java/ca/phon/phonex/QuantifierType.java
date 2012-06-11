package ca.phon.phonex;

/**
 * Quantifier types. 
 *
 */
public enum QuantifierType {
	ZERO_OR_MORE("*"),
	ZERO_OR_ONE("?"),
	ONE_OR_MORE("+"),
	BOUNDED("<x,y>");
	
	private String value;
	
	private QuantifierType(String v) {
		this.value = v;
	}
	
	public String getValue() {
		return this.value;
	}
	
	@Override
	public String toString() {
		return getValue();
	}
	
	public static QuantifierType fromString(String txt) {
		QuantifierType retVal = null;
		for(QuantifierType q:QuantifierType.values()) {
			if(q.getValue().equals(txt)) {
				retVal = q;
				break;
			}
		}
		return retVal;
	}
	
}
