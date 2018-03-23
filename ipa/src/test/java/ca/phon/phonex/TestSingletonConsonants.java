package ca.phon.phonex;

import ca.phon.ipa.IPATranscript;

public class TestSingletonConsonants {

	public static void main(String[] args) throws Exception {
		final String phonex = "(?<\\S)(\\c:O)(?>\\v)";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);

		final String ipa = "ˈb:Oe:Nʃ̟:Oə:N";
		final IPATranscript t = IPATranscript.parseIPATranscript(ipa);

		final PhonexMatcher matcher = pattern.matcher(t);
		while(matcher.find()) {
			System.out.println(matcher.group(0));
		}
	}

}
