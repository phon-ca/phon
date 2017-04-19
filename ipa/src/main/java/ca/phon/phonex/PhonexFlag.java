package ca.phon.phonex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Phonex flags
 */
public enum PhonexFlag {
	ALLOW_OVERLAPPING_MATCHES('o', 0x01)
	// currently not implemented
	/*
	IGNORE_DIACRITICS('d', 0x02),
	IGNORE_PUNCTUATION('p', 0x04)
	*/
	;

	private char flagChar;

	private int bitmask;

	public static List<PhonexFlag> getFlags(int flags) {
		List<PhonexFlag> retVal = new ArrayList<>();
		for(PhonexFlag flag:values()) {
			if(flag.checkFlag(flags))
				retVal.add(flag);
		}
		return Collections.unmodifiableList(retVal);
	}

	public static PhonexFlag fromChar(char flagChar) {
		PhonexFlag retVal = null;
		for(PhonexFlag flag:values()) {
			if(flag.getFlagChar() == flagChar) {
				retVal = flag;
				break;
			}
		}
		return retVal;
	}

	private PhonexFlag(char flagChar, int bitmask) {
		this.flagChar = flagChar;
		this.bitmask = bitmask;
	}

	public int getBitmask() {
		return this.bitmask;
	}

	public char getFlagChar() {
		return this.flagChar;
	}

	public boolean checkFlag(int flags) {
		return ((flags & getBitmask()) == getBitmask());
	}

}
