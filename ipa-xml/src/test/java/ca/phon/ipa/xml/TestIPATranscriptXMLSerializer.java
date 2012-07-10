package ca.phon.ipa.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;

import ca.phon.ipa.IPATranscript;
import ca.phon.xml.XMLSerializer;
import ca.phon.xml.XMLSerializerFactory;

import junit.framework.TestCase;

public class TestIPATranscriptXMLSerializer extends TestCase {
	
	public void testRoundTrip() {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final XMLSerializerFactory serializerFactory = new XMLSerializerFactory();
		try {
			
			final IPATranscript transcript = IPATranscript.parseTranscript("ˈæt ˈbʰʌɹθˌdeɪ ˈpɑɹtiːz");
			final XMLSerializer serializer = serializerFactory.newSerializerForType(IPATranscript.class);
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
