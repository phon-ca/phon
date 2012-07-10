package ca.phon.ipa.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;

import ca.phon.ipa.IPATranscript;

import junit.framework.TestCase;

public class TestIPATranscriptXMLSerializer extends TestCase {
	
	public void testOutput() {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			final IPATranscript transcript = IPATranscript.parseTranscript("ˈæt ˈbʰʌɹθˌdeɪ ˈpɑɹtiːz");
			final IPATranscriptXMLSerializer serializer = new IPATranscriptXMLSerializer();
			serializer.write(IPATranscript.class, transcript, os);
			
			final ByteArrayInputStream bin = new ByteArrayInputStream(os.toByteArray());
			final IPATranscript newTranscript = serializer.read(IPATranscript.class, bin);
			assertEquals(transcript.toString(), newTranscript.toString());
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
