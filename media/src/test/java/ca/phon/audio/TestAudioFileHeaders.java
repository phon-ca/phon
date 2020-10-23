package ca.phon.audio;

import java.io.*;
import java.net.*;
import java.util.*;

import org.junit.Test;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Parameterized.*;

import junit.framework.Assert;

@RunWith(Parameterized.class)
public class TestAudioFileHeaders {
	
	private static Object[][] _testData = {
		// filename, AudioFileType, AudioFileEncoding, channels, sample rate, 23493L, Optional exception type
		
		// WAV
		{"wav/M1F1-Alaw-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.ALAW, 2, 8000.0, 23493L,Optional.empty()},
		{"wav/M1F1-AlawWE-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.ALAW, 2, 8000.0, 23493L, Optional.empty()},
		{"wav/M1F1-mulaw-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.MULAW, 2, 8000.0, 23493L, Optional.empty()},
		{"wav/M1F1-mulawWE-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.MULAW, 2, 8000.0, 23493L, Optional.empty()},
		{"wav/M1F1-float32-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.IEEE_FLOAT_32_LITTLE_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		{"wav/M1F1-float32WE-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.IEEE_FLOAT_32_LITTLE_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		{"wav/M1F1-float64-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.IEEE_FLOAT_64_LITTLE_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		{"wav/M1F1-float64WE-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.IEEE_FLOAT_64_LITTLE_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		{"wav/M1F1-uint8-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.LINEAR_8_UNSIGNED, 2, 8000.0, 23493L, Optional.empty()},
		{"wav/M1F1-uint8WE-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.LINEAR_8_UNSIGNED, 2, 8000.0, 23493L, Optional.empty()},
		{"wav/M1F1-int12-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.LINEAR_16_LITTLE_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		{"wav/M1F1-int12WE-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.LINEAR_16_LITTLE_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		{"wav/M1F1-int16-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.LINEAR_16_LITTLE_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		{"wav/M1F1-int16WE-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.LINEAR_16_LITTLE_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		{"wav/M1F1-int24-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.LINEAR_24_LITTLE_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		{"wav/M1F1-int24WE-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.LINEAR_24_LITTLE_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		{"wav/M1F1-int32-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.LINEAR_32_LITTLE_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		{"wav/M1F1-int32WE-AFsp.wav", AudioFileType.WAV, AudioFileEncoding.LINEAR_32_LITTLE_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		
		// bad data chunk size
		{"wav/cl_F1_2b_egg.wav", AudioFileType.WAV, AudioFileEncoding.LINEAR_16_LITTLE_ENDIAN, 1, 44100.0, 156049L, Optional.empty()},
		
		// AIFF
		{"aif/M1F1-int8-AFsp.aif", AudioFileType.AIFF, AudioFileEncoding.LINEAR_8_SIGNED, 2, 8000.0, 23493L, Optional.empty()},
		{"aif/M1F1-int12-AFsp.aif", AudioFileType.AIFF, AudioFileEncoding.LINEAR_16_BIG_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		{"aif/M1F1-int16-AFsp.aif", AudioFileType.AIFF, AudioFileEncoding.LINEAR_16_BIG_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		{"aif/M1F1-int24-AFsp.aif", AudioFileType.AIFF, AudioFileEncoding.LINEAR_24_BIG_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		{"aif/M1F1-int32-AFsp.aif", AudioFileType.AIFF, AudioFileEncoding.LINEAR_32_BIG_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		
		// AIFC
		{"aif/M1F1-int8C-AFsp.aif", AudioFileType.AIFC, AudioFileEncoding.LINEAR_8_SIGNED, 2, 8000.0, 23493L, Optional.empty()},
		{"aif/M1F1-int12C-AFsp.aif", AudioFileType.AIFC, AudioFileEncoding.LINEAR_16_BIG_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		{"aif/M1F1-int16C-AFsp.aif", AudioFileType.AIFC, AudioFileEncoding.LINEAR_16_BIG_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		{"aif/M1F1-int24C-AFsp.aif", AudioFileType.AIFC, AudioFileEncoding.LINEAR_24_BIG_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		{"aif/M1F1-int32C-AFsp.aif", AudioFileType.AIFC, AudioFileEncoding.LINEAR_32_BIG_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		{"aif/M1F1-int16s-AFsp.aif", AudioFileType.AIFC, AudioFileEncoding.LINEAR_16_LITTLE_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		{"aif/M1F1-float32C-AFsp.aif", AudioFileType.AIFC, AudioFileEncoding.IEEE_FLOAT_32_BIG_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		{"aif/M1F1-float64C-AFsp.aif", AudioFileType.AIFC, AudioFileEncoding.IEEE_FLOAT_64_BIG_ENDIAN, 2, 8000.0, 23493L, Optional.empty()},
		{"aif/M1F1-AlawC-AFsp.aif", AudioFileType.AIFC, AudioFileEncoding.ALAW, 2, 8000.0, 23493L, Optional.empty()},
		{"aif/M1F1-mulawC-AFsp.aif", AudioFileType.AIFC, AudioFileEncoding.MULAW, 2, 8000.0, 23493L, Optional.empty()},
		
		// AIFF peverse files
		{"aif/Pmiscck.aif", AudioFileType.AIFC, AudioFileEncoding.MULAW, 1, 8000.0, 9L, Optional.empty()},
		{"aif/Pnossnd.aif", AudioFileType.AIFC, AudioFileEncoding.MULAW, 1, 8000.0, 23493L, Optional.of(InvalidHeaderException.class)},
		{"aif/Poffset.aif", AudioFileType.AIFC, AudioFileEncoding.MULAW, 1, 8000.0, 9L, Optional.empty()},
		{"aif/Porder.aif", AudioFileType.AIFC, AudioFileEncoding.MULAW, 1, 8000.0, 23493L, Optional.of(InvalidHeaderException.class)},
		{"aif/Ptjunk.aif", AudioFileType.AIFC, AudioFileEncoding.MULAW, 1, 8000.0, 9L, Optional.empty()},
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
	private long numSamples;
	private Optional<Throwable> expectedException;
	
	
	public TestAudioFileHeaders(String filename, AudioFileType audioFileType, AudioFileEncoding audioFileEncoding, int channels, double sampleRate, long numSamples, Optional<Throwable> expectedException) {
		super();
		
		this.filename = filename;
		this.audioFileType = audioFileType;
		this.audioFileEncoding = audioFileEncoding;
		this.channels = channels;
		this.sampleRate = sampleRate;
		this.expectedException = expectedException;
		this.numSamples = numSamples;
	}
	
	@Test
	public void voidTestAiffFile() throws Exception {
		URL audioFileURL = getClass().getResource(filename);
		Assert.assertNotNull(audioFileURL);
		
		try {
			AudioFile audioFile = AudioIO.openAudioFile(new File(audioFileURL.toURI()));
			
			Assert.assertEquals(this.audioFileType, audioFile.getAudioFileType());
			Assert.assertEquals(this.audioFileEncoding, audioFile.getAudioFileEncoding());
			Assert.assertEquals(this.channels, audioFile.getNumberOfChannels());
			Assert.assertEquals(this.sampleRate, audioFile.getSampleRate());
			Assert.assertEquals(this.numSamples, audioFile.getNumberOfSamples());
			
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
