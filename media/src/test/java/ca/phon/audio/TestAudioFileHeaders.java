package ca.phon.audio;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ca.phon.audio.AudioFile;
import ca.phon.audio.AudioFileEncoding;
import ca.phon.audio.AudioFileType;
import ca.phon.audio.AudioFiles;
import ca.phon.audio.InvalidHeaderException;
import ca.phon.audio.UnsupportedFormatException;
import junit.framework.Assert;

@RunWith(Parameterized.class)
public class TestAudioFileHeaders {
	
	private static Object[][] _testData = {
			// filename, AudioFileType, AudioFileEncoding, channels, sample rate, optional exception type
			
			// WAV
			{"wav/M1F1-Alaw-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.ALAW, 2, 8000.0, Optional.empty()},
			{"wav/M1F1-AlawWE-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.ALAW, 2, 8000.0, Optional.empty()},
			{"wav/M1F1-mulaw-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.MULAW, 2, 8000.0, Optional.empty()},
			{"wav/M1F1-mulawWE-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.MULAW, 2, 8000.0, Optional.empty()},
			{"wav/M1F1-float32-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.IEEE_FLOAT_32_LITTLE_ENDIAN, 2, 8000.0, Optional.empty()},
			{"wav/M1F1-float32WE-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.IEEE_FLOAT_32_LITTLE_ENDIAN, 2, 8000.0, Optional.empty()},
			{"wav/M1F1-float64-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.IEEE_FLOAT_64_LITTLE_ENDIAN, 2, 8000.0, Optional.empty()},
			{"wav/M1F1-float64WE-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.IEEE_FLOAT_64_LITTLE_ENDIAN, 2, 8000.0, Optional.empty()},
			{"wav/M1F1-uint8-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.LINEAR_8_UNSIGNED, 2, 8000.0, Optional.empty()},
			{"wav/M1F1-uint8WE-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.LINEAR_8_UNSIGNED, 2, 8000.0, Optional.empty()},
			{"wav/M1F1-int12-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.LINEAR_16_LITTLE_ENDIAN, 2, 8000.0, Optional.empty()},
			{"wav/M1F1-int12WE-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.LINEAR_16_LITTLE_ENDIAN, 2, 8000.0, Optional.empty()},
			{"wav/M1F1-int16-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.LINEAR_16_LITTLE_ENDIAN, 2, 8000.0, Optional.empty()},
			{"wav/M1F1-int16WE-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.LINEAR_16_LITTLE_ENDIAN, 2, 8000.0, Optional.empty()},
			{"wav/M1F1-int24-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.LINEAR_24_LITTLE_ENDIAN, 2, 8000.0, Optional.empty()},
			{"wav/M1F1-int24WE-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.LINEAR_24_LITTLE_ENDIAN, 2, 8000.0, Optional.empty()},
			{"wav/M1F1-int32-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.LINEAR_32_LITTLE_ENDIAN, 2, 8000.0, Optional.empty()},
			{"wav/M1F1-int32WE-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.LINEAR_32_LITTLE_ENDIAN, 2, 8000.0, Optional.empty()},
			
			// AIFF
			{"aif/M1F1-int8-AFsp.aif", AudioFileType.AIFF, AudioFileEncoding.LINEAR_8_SIGNED, 2, 8000.0, Optional.empty()},
			{"aif/M1F1-int12-AFsp.aif", AudioFileType.AIFF, AudioFileEncoding.LINEAR_16_BIG_ENDIAN, 2, 8000.0, Optional.empty()},
			{"aif/M1F1-int16-AFsp.aif", AudioFileType.AIFF, AudioFileEncoding.LINEAR_16_BIG_ENDIAN, 2, 8000.0, Optional.empty()},
			{"aif/M1F1-int24-AFsp.aif", AudioFileType.AIFF, AudioFileEncoding.LINEAR_24_BIG_ENDIAN, 2, 8000.0, Optional.empty()},
			{"aif/M1F1-int32-AFsp.aif", AudioFileType.AIFF, AudioFileEncoding.LINEAR_32_BIG_ENDIAN, 2, 8000.0, Optional.empty()},
			
			// AIFC
			{"aif/M1F1-int8C-AFsp.aif", AudioFileType.AIFC, AudioFileEncoding.LINEAR_8_SIGNED, 2, 8000.0, Optional.empty()},
			{"aif/M1F1-int12C-AFsp.aif", AudioFileType.AIFC, AudioFileEncoding.LINEAR_16_BIG_ENDIAN, 2, 8000.0, Optional.empty()},
			{"aif/M1F1-int16C-AFsp.aif", AudioFileType.AIFC, AudioFileEncoding.LINEAR_16_BIG_ENDIAN, 2, 8000.0, Optional.empty()},
			{"aif/M1F1-int24C-AFsp.aif", AudioFileType.AIFC, AudioFileEncoding.LINEAR_24_BIG_ENDIAN, 2, 8000.0, Optional.empty()},
			{"aif/M1F1-int32C-AFsp.aif", AudioFileType.AIFC, AudioFileEncoding.LINEAR_32_BIG_ENDIAN, 2, 8000.0, Optional.empty()},
			{"aif/M1F1-int16s-AFsp.aif", AudioFileType.AIFC, AudioFileEncoding.LINEAR_16_LITTLE_ENDIAN, 2, 8000.0, Optional.empty()},
			{"aif/M1F1-float32C-AFsp.aif", AudioFileType.AIFC, AudioFileEncoding.IEEE_FLOAT_32_BIG_ENDIAN, 2, 8000.0, Optional.empty()},
			{"aif/M1F1-float64C-AFsp.aif", AudioFileType.AIFC, AudioFileEncoding.IEEE_FLOAT_64_BIG_ENDIAN, 2, 8000.0, Optional.empty()},
			{"aif/M1F1-AlawC-AFsp.aif", AudioFileType.AIFC, AudioFileEncoding.ALAW, 2, 8000.0, Optional.empty()},
			{"aif/M1F1-mulawC-AFsp.aif", AudioFileType.AIFC, AudioFileEncoding.MULAW, 2, 8000.0, Optional.empty()},
			
			// AIFF peverse files
			{"aif/Pmiscck.aif", AudioFileType.AIFC, AudioFileEncoding.MULAW, 1, 8000.0, Optional.empty()},
			{"aif/Pnossnd.aif", AudioFileType.AIFC, AudioFileEncoding.MULAW, 1, 8000.0, Optional.of(InvalidHeaderException.class)},
			{"aif/Poffset.aif", AudioFileType.AIFC, AudioFileEncoding.MULAW, 1, 8000.0, Optional.empty()},
			{"aif/Porder.aif", AudioFileType.AIFC, AudioFileEncoding.MULAW, 1, 8000.0, Optional.of(InvalidHeaderException.class)},
			{"aif/Ptjunk.aif", AudioFileType.AIFC, AudioFileEncoding.MULAW, 1, 8000.0, Optional.empty()},
	};

	@Parameters
	public static Collection<Object[]> testData() {
		return Arrays.asList(_testData);
	}
	
	private String filename;
	private AudioFileType audioFileType;
	private AudioFileEncoding audioFileEncoding;
	private int channels;
	private double sampleRate;
	private Optional<Throwable> expectedException;
	
	public TestAudioFileHeaders(String filename, AudioFileType audioFileType, AudioFileEncoding audioFileEncoding, int channels, double sampleRate, Optional<Throwable> expectedException) {
		super();
		
		this.filename = filename;
		this.audioFileType = audioFileType;
		this.audioFileEncoding = audioFileEncoding;
		this.channels = channels;
		this.sampleRate = sampleRate;
		this.expectedException = expectedException;
	}
	
	@Test
	public void voidTestAiffFile() throws Exception {
		URL audioFileURL = getClass().getResource(filename);
		Assert.assertNotNull(audioFileURL);
		
		try {
			AudioFile audioFile = AudioFiles.openAudioFile(new File(audioFileURL.toURI()));
			
			Assert.assertEquals(this.audioFileType, audioFile.getAudioFileType());
			Assert.assertEquals(this.audioFileEncoding, audioFile.getAudioFileEncoding());
			Assert.assertEquals(this.channels, audioFile.getNumberOfChannels());
			Assert.assertEquals(this.sampleRate, audioFile.getSampleRate());
			
			Assert.assertTrue(expectedException.isEmpty());
		} catch (Exception e) {
			if(expectedException.isPresent()) {
				Assert.assertEquals(expectedException.get(), e.getClass());
				return;
			} else {
				throw e;
			}
		}
		
	}
	
	
}
