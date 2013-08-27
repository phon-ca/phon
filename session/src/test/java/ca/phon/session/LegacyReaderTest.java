package ca.phon.session;

import java.io.InputStream;
import java.net.URI;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.orthography.Orthography;
import ca.phon.session.io.SessionInputFactory;
import ca.phon.session.io.SessionOutputFactory;
import ca.phon.session.io.SessionReader;
import ca.phon.session.io.SessionWriter;
import ca.phon.util.Base64.OutputStream;

/**
 * JUnit test for legacy session reader
 *
 */
@RunWith(JUnit4.class)
public class LegacyReaderTest {

	@Test
	public void testReader() throws Exception {
		final InputStream stream = getClass().getClassLoader().getResourceAsStream("tests/TestWordGroupPreservation.xml");
		
		final SessionInputFactory inputFactory = new SessionInputFactory();
		final SessionReader reader = inputFactory.createReader("phonbank", "1.2");
		Assert.assertNotNull(reader);
		
		final Session session = reader.readSession(stream);
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
		
		final SessionOutputFactory outputFactory = new SessionOutputFactory();
		final SessionWriter writer = outputFactory.createWriter("1.2");
		writer.writeSession(session, System.out);
	}
	
}
