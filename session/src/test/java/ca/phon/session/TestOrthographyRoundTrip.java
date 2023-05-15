package ca.phon.session;

import ca.phon.orthography.Orthography;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.Parameterized;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    public void testRoundTrip() throws ParseException {
        final Orthography orthography = Orthography.parseOrthography(chat);
        Assert.assertEquals(chat, orthography.toString());
    }

}
