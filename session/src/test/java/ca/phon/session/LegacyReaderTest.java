package ca.phon.session;

import java.net.URI;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.orthography.Orthography;
import ca.phon.session.io.SessionInputFactory;
import ca.phon.session.io.SessionReader;
import ca.phon.session.io.xml.XmlSessionReader;

/**
 * JUnit test for legacy session reader
 *
 */
@RunWith(JUnit4.class)
public class LegacyReaderTest {

	@Test
	public void testReader() throws Exception {
		final URI uri = getClass().getClassLoader().getResource("tests/TestWordGroupPreservation.xml").toURI();
		
		final SessionInputFactory inputFactory = new SessionInputFactory();
		final SessionReader reader = inputFactory.createReader(uri);
		Assert.assertNotNull(reader);
		Assert.assertEquals(XmlSessionReader.class, reader.getClass());
		
		final Session session = reader.readSession(uri);
		Assert.assertNotNull(session);
		
		// check header info
		Assert.assertEquals("Corpus1", session.getCorpus());
		Assert.assertEquals("TestWordGroupPreservation", session.getName());
		
		// check participant info
		Assert.assertEquals(1, session.getParticipantCount());
		final Participant participant = session.getParticipant(0);
		Assert.assertEquals("Testy", participant.getName());
		Assert.assertEquals("eng", participant.getLanguage());
		Assert.assertEquals(ParticipantRole.CHILD, participant.getRole());
		
		// check record information
		Assert.assertEquals(1, session.getRecordCount());
		final Record record = session.getRecord(0);
		Assert.assertEquals(participant, record.getSpeaker());
		
		// orthography
		final Tier<Orthography> ortho = record.getOrthography();
		Assert.assertEquals(2, ortho.numberOfGroups());
		Assert.assertEquals("this is", ortho.getGroup(0).toString());
		Assert.assertEquals("a test .", ortho.getGroup(1).toString());
		
		// TODO test remainder of record data
	}
	
}
