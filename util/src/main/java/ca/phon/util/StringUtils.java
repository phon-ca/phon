/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.phon.util;

import java.awt.Font;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private StringUtils() {
    }

    /**
     * An empty immutable <code>String</code> array.
     */
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    
    /**
     * Tests if this string starts with the specified prefix (Ignoring whitespaces) 
     * @param prefix
     * @param string
     * @return boolean
     */ 
    public static boolean startsWithIgnoreWhitespaces(String prefix, String string) {
        int index1 = 0;
        int index2 = 0;
        int length1 = prefix.length();
        int length2 = string.length();
        char ch1 = ' ';
        char ch2 = ' ';
        while (index1 < length1 && index2 < length2) {
            while (index1 < length1 && Character.isWhitespace(ch1 = prefix.charAt(index1))) {
                index1++;
            }
            while (index2 < length2 && Character.isWhitespace(ch2 = string.charAt(index2))) {
                index2++;
            }
            if (index1 == length1 && index2 == length2) {
                return true;
            }
            if (ch1 != ch2) {
                return false;
            }
            index1++;
            index2++;
        }
        if(index1 < length1 && index2 >= length2)
            return false;
        return true;
    }

    /**
     * <p>Splits the provided text into an array, separator specified.
     * This is an alternative to using StringTokenizer.</p>
     *
     * <p>The separator is not included in the returned String array.
     * Adjacent separators are treated as one separator.</p>
     *
     * <p>A <code>null</code> input String returns <code>null</code>.</p>
     *
     * <pre>
     * StringUtils.split(null, *)         = null
     * StringUtils.split("", *)           = []
     * StringUtils.split("a.b.c", '.')    = ["a", "b", "c"]
     * StringUtils.split("a..b.c", '.')   = ["a", "b", "c"]
     * StringUtils.split("a:b:c", '.')    = ["a:b:c"]
     * StringUtils.split("a\tb\nc", null) = ["a", "b", "c"]
     * StringUtils.split("a b c", ' ')    = ["a", "b", "c"]
     * </pre>
     *
     * @param str  the String to parse, may be null
     * @param separatorChar  the character used as the delimiter,
     *  <code>null</code> splits on whitespace
     * @return an array of parsed Strings, <code>null</code> if null String input
     */
    public static String[] split(String str, char separatorChar) {
        if (str == null) {
            return null;
        }
        int len = str.length();
        if (len == 0) {
            return EMPTY_STRING_ARRAY;
        }
        List list = new ArrayList();
        int i = 0, start = 0;
        boolean match = false;
        while (i < len) {
            if (str.charAt(i) == separatorChar) {
                if (match) {
                    list.add(str.substring(start, i));
                    match = false;
                }
                start = ++i;
                continue;
            }
            match = true;
            i++;
        }
        if (match) {
            list.add(str.substring(start, i));
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    // Empty checks

    //-----------------------------------------------------------------------

    /**
     * <p>Checks if a String is empty ("") or null.</p>
     *
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     *
     * <p>NOTE: This method changed in Lang version 2.0.
     * It no longer trims the String.
     * That functionality is available in isBlank().</p>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is empty or null
     */
    public static boolean isEmpty(String str) {
        return (str == null || str.length() == 0);
    }
    
    // Stripping

    //-----------------------------------------------------------------------

    /**
     * <p>Strips whitespace from the start and end of a String.</p>
     *
     * <p>This removes whitespace. Whitespace is defined by 
     * {@link Character#isWhitespace(char)}.</p>
     *
     * <p>A <code>null</code> input String returns <code>null</code>.</p>
     *
     * <pre>
     * StringUtils.strip(null)     = null
     * StringUtils.strip("")       = ""
     * StringUtils.strip("   ")    = ""
     * StringUtils.strip("abc")    = "abc"
     * StringUtils.strip("  abc")  = "abc"
     * StringUtils.strip("abc  ")  = "abc"
     * StringUtils.strip(" abc ")  = "abc"
     * StringUtils.strip(" ab c ") = "ab c"
     * </pre>
     *
     * @param str  the String to remove whitespace from, may be null
     * @return the stripped String, <code>null</code> if null String input
     */
    public static String strip(String str) {
        return strip(str, null);
    }
    
    /**
     * <p>Strips any of a set of characters from the start and end of a String.
     * This is similar to {@link String#trim()} but allows the characters
     * to be stripped to be controlled.</p>
     *
     * <p>A <code>null</code> input String returns <code>null</code>.
     * An empty string ("") input returns the empty string.</p>
     *
     * <p>If the stripChars String is <code>null</code>, whitespace is
     * stripped as defined by {@link Character#isWhitespace(char)}.
     * Alternatively use {@link #strip(String)}.</p>
     *
     * <pre>
     * StringUtils.strip(null, *)          = null
     * StringUtils.strip("", *)            = ""
     * StringUtils.strip("abc", null)      = "abc"
     * StringUtils.strip("  abc", null)    = "abc"
     * StringUtils.strip("abc  ", null)    = "abc"
     * StringUtils.strip(" abc ", null)    = "abc"
     * StringUtils.strip("  abcyx", "xyz") = "  abc"
     * </pre>
     *
     * @param str  the String to remove characters from, may be null
     * @param stripChars  the characters to remove, null treated as whitespace
     * @return the stripped String, <code>null</code> if null String input
     */
    public static String strip(String str, String stripChars) {
        if (isEmpty(str)) {
            return str;
        }
        str = stripStart(str, stripChars);
        return stripEnd(str, stripChars);
    }

    /**
     * <p>Strips any of a set of characters from the start of a String.</p>
     *
     * <p>A <code>null</code> input String returns <code>null</code>.
     * An empty string ("") input returns the empty string.</p>
     *
     * <p>If the stripChars String is <code>null</code>, whitespace is
     * stripped as defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.stripStart(null, *)          = null
     * StringUtils.stripStart("", *)            = ""
     * StringUtils.stripStart("abc", "")        = "abc"
     * StringUtils.stripStart("abc", null)      = "abc"
     * StringUtils.stripStart("  abc", null)    = "abc"
     * StringUtils.stripStart("abc  ", null)    = "abc  "
     * StringUtils.stripStart(" abc ", null)    = "abc "
     * StringUtils.stripStart("yxabc  ", "xyz") = "abc  "
     * </pre>
     *
     * @param str  the String to remove characters from, may be null
     * @param stripChars  the characters to remove, null treated as whitespace
     * @return the stripped String, <code>null</code> if null String input
     */
    public static String stripStart(String str, String stripChars) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        int start = 0;
        if (stripChars == null) {
            while ((start != strLen) && Character.isWhitespace(str.charAt(start))) {
                start++;
            }
        } else if (stripChars.length() == 0) {
            return str;
        } else {
            while ((start != strLen) && (stripChars.indexOf(str.charAt(start)) != -1)) {
                start++;
            }
        }
        return str.substring(start);
    }

    /**
     * <p>Strips any of a set of characters from the end of a String.</p>
     *
     * <p>A <code>null</code> input String returns <code>null</code>.
     * An empty string ("") input returns the empty string.</p>
     *
     * <p>If the stripChars String is <code>null</code>, whitespace is
     * stripped as defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.stripEnd(null, *)          = null
     * StringUtils.stripEnd("", *)            = ""
     * StringUtils.stripEnd("abc", "")        = "abc"
     * StringUtils.stripEnd("abc", null)      = "abc"
     * StringUtils.stripEnd("  abc", null)    = "  abc"
     * StringUtils.stripEnd("abc  ", null)    = "abc"
     * StringUtils.stripEnd(" abc ", null)    = " abc"
     * StringUtils.stripEnd("  abcyx", "xyz") = "  abc"
     * </pre>
     *
     * @param str  the String to remove characters from, may be null
     * @param stripChars  the characters to remove, null treated as whitespace
     * @return the stripped String, <code>null</code> if null String input
     */
    public static String stripEnd(String str, String stripChars) {
        int end;
        if (str == null || (end = str.length()) == 0) {
            return str;
        }

        if (stripChars == null) {
            while ((end != 0) && Character.isWhitespace(str.charAt(end - 1))) {
                end--;
            }
        } else if (stripChars.length() == 0) {
            return str;
        } else {
            while ((end != 0) && (stripChars.indexOf(str.charAt(end - 1)) != -1)) {
                end--;
            }
        }
        return str.substring(0, end);
    }
    
//    /**
//     * Strip diacritics from a UTF-8 transcript.
//     */
//    public static String stripDiacritics(String transcript) {
//		String retVal = new String();
//		
//		FeatureMatrix fm = FeatureMatrix.getInstance();
//		
//		for(char c:transcript.toCharArray()) {
//			FeatureSet fs = fm.getFeatureSet(c);
//			
//			if(fs == null)
//				System.out.println(Integer.toHexString(c));
//			
//			if(!fs.hasFeature("Diacritic"))
//				retVal += c;
//		}
//		
//		return retVal;
//	}

	/**
	 * Returns weather the given index is withing a group (e.g., a set of '[' ']')
	 *
	 * @param text
	 * @param pos
	 * @returns true if the given position is within a group, false otherwise
	 */
	public static boolean isWithinGroup(String text, int pos) {
		boolean retVal = false;

		int nextGEnd = text.indexOf("]", pos);
		if(nextGEnd < 0) {
			retVal = false;
		} else {
			int nextGStart = text.indexOf("[", pos);
			if(nextGStart < 0) {
				retVal = true;
			} else {
				if(nextGStart < nextGEnd) {
					retVal = false;
				} else {
					retVal = true;
				}
			}
		}

		return retVal;
	}


    /**
     * Returns a list of string contained withing '[' ']'.
     * This method only supports a embed index of 0.
     * 
     * @param data
     * @return ArrayList<String> the list of strings found or
     * null if the brackets are unbalanced.
     * 
     * 
     */
    public static ArrayList<String> extractedBracketedStrings(String rawText) {
		// scan the string for matching brackets first
		int bracketIndex = 0;
		for(int i = 0; i < rawText.length(); i++) {
			if(rawText.charAt(i) == '[')
				bracketIndex++;
			else if(rawText.charAt(i) == ']')
				bracketIndex--;
			
			// nested brackets are invalid
			if(bracketIndex > 1)
				return null;
		}
		
		// brackets not matched, return null
		if(bracketIndex != 0)
			return null;
		
		ArrayList<String> words = new ArrayList<String>();
		
		String currentWord = null;
		for(int i = 0; i < rawText.length(); i++) {
			if(rawText.charAt(i) == '[') {
				currentWord = new String();
			} else if(rawText.charAt(i) == ']') {
				words.add(currentWord);
				currentWord = null;
			} else {
				if(currentWord != null)
					currentWord += rawText.charAt(i);
			}
		}
		
		if(words.size() == 0) words.add(rawText);
//			return rawText;
//		else
			return words;
	}
    
    /**
     * Shortens a string by replacing a portion of the string
     * with the specified token.  The return string will be no
     * longer thatn maxLength.
     * 
     * If the source string is less the maxLength, the source 
     * string is returned unchanged.
     * 
     * @param source
     * @param token
     * @param maxLength
     * @return the modified string
     */
    public static String shortenStringUsingToken(String source, String token, int maxLength) {
    	if(maxLength <= 0) return "";
    	String retVal = new String();
    	int sourceStringLength = source.length();
    	
    	if(sourceStringLength <= maxLength)
    		retVal += source;
    	else {
    		int tokenLength = token.length();
    		
    		int numCharactersFromSource = maxLength-tokenLength;
    		
    		int numCharactersFromBeginning = numCharactersFromSource/2;
    		int numCharactersFromEnd = numCharactersFromSource/2 + numCharactersFromSource%2;
    		
    		int endStartIndex = sourceStringLength-numCharactersFromEnd;
    		
    		retVal += source.substring(0, numCharactersFromBeginning);
    		retVal += token;
    		retVal += source.substring(endStartIndex, endStartIndex+numCharactersFromEnd);
    	}
    	
    	return retVal;
    }
    
    /**
	 * Converts the specified font into a string that can be used by
	 * Font.decode.
	 * @param font  the Font to convert to a String
	 * @return      a String
	 */
	public static String fontToString(Font font) {
		StringBuilder ret = new StringBuilder();
		ret.append(font.getFamily());
		ret.append("-");
		
		if(font.isBold()) {
			if(font.isItalic())
				ret.append("BOLDITALIC");
			else
				ret.append("BOLD");
		} else if(font.isItalic()) {
			ret.append("ITALIC");
		} else {
			ret.append("PLAIN");
		}
		ret.append("-");
		
		ret.append(font.getSize());
		
		return ret.toString();
	}
	
	/**
	 * Reverse a string
	 * 
	 * @param text
	 * @return the reversed text
	 */
	public static String reverse(String text) {
		String retVal = new String();
		
		for(int i = text.length()-1; i >= 0; i--) {
			retVal += text.charAt(i);
		}
		
		return retVal;
	}
	
	public static String substringFromRange(String s, Range r) {
		String retVal = "";
		
		for(int i:r) {
			if(i < 0 || i >= s.length()) {
				break;
			}
			retVal += s.charAt(i);
		}
		
		return retVal;
	}
	
	
	
	public static String msToDisplayString(long ms) 
		throws IllegalArgumentException {
		if(ms < 0)
			throw new IllegalArgumentException("Time cannot be negative.");
		
		long numSeconds = ms / 1000;
		long numMSecondsLeft = ms % 1000;
		
		long numMinutes = numSeconds / 60;
		long numSecondsLeft = numSeconds % 60;
		
		NumberFormat nf = NumberFormat.getIntegerInstance();
		nf.setMinimumIntegerDigits(2);
		
		NumberFormat msNf = NumberFormat.getIntegerInstance();
		msNf.setMinimumIntegerDigits(3);
		
		String minuteString = msNf.format(numMinutes) + ":";
		
		String secondString =
			(numMinutes == 0 
					? (nf.format(numSeconds) + ".")
					: (nf.format(numSecondsLeft) + ".")
			);
		
		String msString = 
			(msNf.format(numMSecondsLeft));
		
		String timeString = 
			minuteString + secondString + msString;
		
		return timeString;
	}

	public static long msFromDisplayString(String txt) {
		Pattern timePattern =
			Pattern.compile("([0-9]{0,3}):([0-9]{1,2}).([0-9]{1,3})");
		Matcher m = timePattern.matcher(txt);
		if(!m.matches()) {
			throw new IllegalArgumentException("Invalid segment time format - " + txt);
		} else {
			int min1Val = Integer.parseInt(m.group(1));
			int sec1Val = Integer.parseInt(m.group(2));
			int ms1Val = Integer.parseInt(m.group(3));

			long m1Val = (min1Val * 60 * 1000) + (sec1Val * 1000) + ms1Val;
			return m1Val;
		}
	}
	
	public static String msToWrittenString(long ms) 
		throws IllegalArgumentException {
		if(ms < 0)
			throw new IllegalArgumentException("Time cannot be negative.");

		long numSeconds = ms / 1000;
		long numMSecondsLeft = ms % 1000;

		long numMinutes = numSeconds / 60;
		long numSecondsLeft = numSeconds % 60;

	//	NumberFormat nf = NumberFormat.getIntegerInstance();
	//	nf.setMinimumIntegerDigits(2);
	//
	//	NumberFormat msNf = NumberFormat.getIntegerInstance();
	//	msNf.setMinimumIntegerDigits(3);

		String minuteString = numMinutes + "m";

		String secondString = numSecondsLeft + "s";

		String msString = numMSecondsLeft + "ms";

		String timeString =
			(numMinutes > 0 ? minuteString + " ": "")
			+
			(numMinutes == 0 && numSeconds == 0 ? "" :  secondString + " ") + msString;

		return timeString;
	}

	/**
	 * Helper method to replace
	 * word at given index with the given
	 * string.  If the given index is greater
	 * than the size of the words in the given
	 * string, '.' is inserted to mark the word.
	 *
	 * @param wIdx
	 * @param text
	 * @param replacement
	 * @return the result of replacing word wIdx in text
	 * with replacement.
	 */
	public static String replaceWordAtIndex(int wIdx, String text, String replacement) {
		String retVal = "";

		String[] words =
				(StringUtils.strip(text).length() > 0
					? text.split("\\p{Space}")
					: new String[0]);

		for(int i = 0; i < wIdx; i++) {
			String w = ".";
			if(i < words.length) {
				w = words[i];
			}
			retVal += (retVal.length() > 0 ? " " : "") + w;
		}
		retVal += (retVal.length() > 0 ? " " : "") + replacement;
		// add remaining words from text
		for(int i = wIdx+1; i < words.length; i++) {
			String w = words[i];
			retVal += (retVal.length() > 0 ? " " : "") + w;
		}

		return retVal;
	}
}
