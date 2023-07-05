package ca.phon.session;

import ca.phon.orthography.Orthography;
import ca.phon.session.io.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.Parameterized;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Test round trip of various CHAT test strings
 */
@RunWith(Parameterized.class)
public class TestOrthographyRoundTrip {

    private final static String TEST_FILE = "test_main_line.txt";

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> testData() {
        List<Object[]> retVal = new ArrayList<>();
        try(BufferedReader in = new BufferedReader(
                new InputStreamReader(TestOrthographyRoundTrip.class.getResourceAsStream(TEST_FILE)))) {
            String line = null;
            while((line = in.readLine()) != null) {
                retVal.add(new String[]{line});
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return retVal;
    }

    private String chat;

    public TestOrthographyRoundTrip(String chat) {
        super();
        this.chat = chat;
    }

    @Test
    public void testRoundTrip() throws IOException {
        final SessionFactory factory = SessionFactory.newFactory();
        final Session testSession = factory.createSession();
        final Record record = factory.createRecord();
        testSession.addRecord(record);
        record.getOrthographyTier().setText(this.chat);
        Assert.assertEquals(this.chat, record.getOrthographyTier().toString());
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final SessionOutputFactory outputFactory = new SessionOutputFactory();
        final SessionWriter writer = outputFactory.createWriter();
        writer.writeSession(testSession, bout);
        System.out.println(bout.toString(StandardCharsets.UTF_8));
        final ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        final SessionInputFactory inputFactory = new SessionInputFactory();
        final SessionReader reader = inputFactory.createReader(writer.getClass().getAnnotation(SessionIO.class));
        final Session roundTripSession = reader.readSession(bin);
        Assert.assertEquals(this.chat, roundTripSession.getRecord(0).getOrthographyTier().toString());
    }

}
