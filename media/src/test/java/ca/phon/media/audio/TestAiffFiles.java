package ca.phon.media.audio;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.audio.AudioFile;
import ca.phon.audio.AudioFileEncoding;
import ca.phon.audio.AudioFileType;
import ca.phon.audio.AudioFiles;
import ca.phon.audio.UnsupportedFormatException;
import junit.framework.Assert;

@RunWith(JUnit4.class)
public class TestAiffFiles {
	
	@Test
	public void voidTestAiffFile() throws IOException, UnsupportedFormatException {
		String filepath = "/Users/ghedlund/Documents/phon/gitprojects/phon/media/src/test/resources/M1F1-int16-AFsp.aif";
		AudioFile audioFile = AudioFiles.openAudioFile(new File(filepath));
		
		Assert.assertEquals(AudioFileType.AIFF, audioFile.getAudioFileType());
		Assert.assertEquals(AudioFileEncoding.LINEAR_16_BIG_ENDIAN, audioFile.getAudioFileEncoding());
		Assert.assertEquals(2, audioFile.getNumberOfChannels());
		Assert.assertEquals(8000.0, audioFile.getSampleRate());		
	}
	
}
