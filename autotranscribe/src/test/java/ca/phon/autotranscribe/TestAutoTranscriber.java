package ca.phon.autotranscribe;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestAutoTranscriber {

    @Test
    public void testAutoTranscriber() throws Exception {
        final String text = "this 0omitted is a tɛ@u test asdf test_is is+test .";
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

        // test transcribing the entire text with selections
        final AutomaticTranscription transcription = transcriber.transcribe(text);
        Assert.assertEquals("ðɪs ɪz eɪ tɛ tɛst * tɛst⁀ɪz ɪz+tɛst", transcription.getTranscription().toString()) ;
        transcription.setSelectedTranscription(transcription.getWords().get(1), 1);
        Assert.assertEquals("ðɪs əz eɪ tɛ tɛst * tɛst⁀ɪz ɪz+tɛst", transcription.getTranscription().toString());
        transcription.setSelectedTranscription(transcription.getWords().get(6), 1);
        Assert.assertEquals("ðɪs əz eɪ tɛ tɛst * tɛst⁀əz ɪz+tɛst", transcription.getTranscription().toString());
        transcription.setSelectedTranscription(transcription.getWords().get(7), 1);
        Assert.assertEquals("ðɪs əz eɪ tɛ tɛst * tɛst⁀əz əz+tɛst", transcription.getTranscription().toString());

        // test transcribing from a specific word
        final AutomaticTranscription transcription2 = transcriber.transcribe(text, 1);
        Assert.assertEquals("ɪz eɪ tɛ tɛst * tɛst⁀ɪz ɪz+tɛst", transcription2.getTranscription().toString());
    }

}
