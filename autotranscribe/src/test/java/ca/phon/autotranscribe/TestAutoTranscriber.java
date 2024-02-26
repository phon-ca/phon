package ca.phon.autotranscribe;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestAutoTranscriber {

    @Test
    public void testAutoTranscriber() throws Exception {
        final String text = "this 0omitted is a test";
        final AutoTranscriber transcriber = new AutoTranscriber();
        final IPADictionaryAutoTranscribeSource source = new IPADictionaryAutoTranscribeSource("eng");
        transcriber.addSource(source);
        final AutomaticTranscription transcription = transcriber.transcribe(text);
        transcription.setSelectedTranscription(transcription.getWords().get(1), 1);

        System.out.println("Transcriptions " + transcription.getTranscription());
    }

}
