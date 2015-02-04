package ca.phon.extensions;

import java.text.ParseException;

/**
 * <p>A container for unvalidated data.  This class
 * may be attached to a tier which the data
 * entered by the user or data read from disk
 * is not parse-able.</p>
 * 
 */
public class UnvalidatedValue {
	
	private String unvalidatedData;
	
	private ParseException parseErr;
	
	public UnvalidatedValue() {
		super();
	}
	
	public UnvalidatedValue(String value) {
		super();
		this.unvalidatedData = value;
	}
	
	public UnvalidatedValue(String value, ParseException pe) {
		super();
		this.unvalidatedData = value;
		this.parseErr = pe;
	}
	
	public String getValue() {
		return this.unvalidatedData;
	}
	
	public void setValue(String value) {
		this.unvalidatedData = value;
	}
	
	public ParseException getParseError() {
		return this.parseErr;
	}
	
	public void setParseError(ParseException pe) {
		this.parseErr = pe;
	}

}
