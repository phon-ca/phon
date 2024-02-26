package ca.phon.autotranscribe;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestAutoTranscriber {

    @Test
    public void testAutoTranscriber() throws Exception {
        final String text = "this 0omitted is a test";
        final AutoTranscriber transcriber = new AutoTranscriber();
        transcriber.addSource( (t) -> {
            switch (t) {
                case "this":
                    return new String[] { "ðɪs", "ðəs" };
                case "is":
                    return new String[] { "ɪz", "əz" };
                case "a":
                    return new String[] { "eɪ", "ə" };
                case "test":
                    return new String[] { "tɛst" };
                default:
                    return new String[0];
            }
        });
        final AutomaticTranscription transcription = transcriber.transcribe(text);
        Assert.assertEquals("ðɪs ɪz eɪ tɛst", transcription.getTranscription().toString()) ;
        transcription.setSelectedTranscription(transcription.getWords().get(1), 1);
        Assert.assertEquals("ðɪs əz eɪ tɛst", transcription.getTranscription().toString());
    }

}
