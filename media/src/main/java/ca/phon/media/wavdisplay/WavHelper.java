/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.media.wavdisplay;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import ca.phon.media.exceptions.PhonMediaException;

/**
 * Helper methods for working with (16-bit)
 * wav audio input streams.
 *
 */
public class WavHelper {
	
	private static final Logger LOGGER = Logger.getLogger(WavHelper.class
			.getName());
	
	// using 8-bit bytes
	private final static int BITS_PER_BYTE = 8;
	
//	private AudioInputStream audioInputStream;
	private File wavFile;
	
	// if not using file we are using cached data
	private byte[] wavData;
	private AudioFormat format;
	private long wavLen;
    
    //cached values
    protected int sampleMax = 0;
    protected int sampleMin = 0;
//    protected double biggestSample;
    protected int numberOfChannels = -1;
    
    /** 
     * Constructor
     */
    public WavHelper(File file) {
    	wavFile = file;
//    	initCachedValues();
    }
    
    public WavHelper(byte[] data, AudioFormat format, long len) {
    	this.wavData = data;
    	this.format = format;
    	this.wavLen = len;
    	initCachedValues();
    }
    
    public WavHelper(byte[] data, AudioFormat format, long len, int sampleMin, int sampleMax) {
    	this.wavData = data;
    	this.format = format;
    	this.wavLen = len;
//    	initCachedValues();
    	this.sampleMax = sampleMax;
    	this.sampleMin = sampleMin;
    }
    
    /**
     * Get the audio input stream from either the
     * given file or byte array.
     */
    public AudioInputStream getAudioInputStream() {
    	AudioInputStream retVal = null;
    	if(wavFile != null) {
    		// build audio stream from file
    		try {
    			retVal = 
    				AudioSystem.getAudioInputStream(wavFile);
    			format = retVal.getFormat();
    		} catch (IOException e) {
    			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
    		}  catch (UnsupportedAudioFileException e) {
    			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
    		}
    	} else {
    		ByteArrayInputStream bin = new ByteArrayInputStream(wavData);
    		// used cached data
    		retVal = 
    			new AudioInputStream(bin, format, (int)wavLen);
    	}
    	return retVal;
    }
   
    /**
     * Read through file and fills cached values.
     * 
     */
    protected void initCachedValues() {
    	// setup sample container
    	// load a single frame at a time
    	AudioInputStream audioInputStream = getAudioInputStream();
    	if(audioInputStream == null) return;
		
//    	synchronized(audioInputStream) {
    		byte[] bc = new byte[audioInputStream.getFormat().getFrameSize() * 1];
    		numberOfChannels = audioInputStream.getFormat().getChannels();
	    	int numFrame = 0;
	    	try {
		    	while(numFrame < audioInputStream.getFrameLength()) {
		    		int bytesRead = audioInputStream.read(bc, 
		    				0, bc.length);
		    		if(bytesRead > 0) {
		    			int[][] samples = frameToSampleArray(bc);
		    			
		    			for(int c = 0; c < numberOfChannels; c++) {
		    				for(int i = 0; i < samples[0].length; i++) {
		    					int sample = samples[c][i];
		    					if(sample < sampleMin) {
		    	    				sampleMin = sample;
		    	    			}
		    	    			if(sample > sampleMax) {
		    	    				sampleMax = sample;
		    	    			}
		    				}
		    			}
		    		} else {
		    			break;
		    		}
		    		numFrame++;
		    	}
		    	
//		    	// reset stream
//		    	audioInputStream.reset();
	    	} catch (IOException e) {
	    		LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
	    	}
//    	}
    }
    
    protected int[][] frameToSampleArray(byte[] frameContainer) {
    	int retVal[][] = new int[0][];
    	
//    	System.out.println(format);
    	
    	if(format.getEncoding() == Encoding.PCM_SIGNED
    			|| format.getEncoding() == Encoding.PCM_UNSIGNED) {
	    	if(format.getSampleSizeInBits() == 8) {
	    		retVal = frameTo8bitSampleArray(frameContainer);
	    	} else if(format.getSampleSizeInBits() == 12) { 
	    		retVal = frameTo12bitSampleArray(frameContainer);
	    	} else if(format.getSampleSizeInBits() == 16) {
	    		retVal = frameTo16bitSampleArray(frameContainer);
	    	} else if(format.getSampleSizeInBits() == 24) {
	    		retVal = frameTo24bitSampleArray(frameContainer);
	    	} else if(format.getSampleSizeInBits() == 32) {
	    		retVal = frameTo32bitSampleArray(frameContainer);
	    	} else {
	    		LOGGER.warning("Unsupported wav format: " + format.toString());
	    	}
    	} else {
    		LOGGER.warning("Unsupported wav format: " + format.toString());
    	}
    	
    	return retVal;
    }
    
    /**
     * Convert frames to samples. (PCM_UNSIGNED 8-bit)
     */
    protected int[][] frameTo8bitSampleArray(byte[] frameContainer) {
    	int numberOfSamples = frameContainer.length / (getNumberOfChannels());
    	int[][] retVal = new int[getNumberOfChannels()][numberOfSamples];
    	
    	int idx = 0;
    	for(int t = 0; t < frameContainer.length;) {
    		for(int a = 0; a < getNumberOfChannels(); a++) {
    			int low = frameContainer[t];
    			t++;
    			
    			int sample = (low & 0x00ff);
    			retVal[a][idx] = sample - 128;
    		}
    		idx++;
    	}
    	
    	return retVal;
    }
    
    /**
     * Convert frames to samples. (PCM_SIGNED 12-bit)
     */
    protected int[][] frameTo12bitSampleArray(byte[] frameContainer) {
    	int numberOfSamples = frameContainer.length / (2 * getNumberOfChannels());
    	int[][] retVal = new int[getNumberOfChannels()][numberOfSamples];
    	
    	int idx = 0;
    	for(int t = 0; t < frameContainer.length;) {
    		for(int a = 0; a < getNumberOfChannels(); a++) {
    			int low = frameContainer[t];
    			t++;
    			int high = frameContainer[t];
    			t++;
    			
    			int sample = ((high << 8) & 0x0F00) + (low & 0x00FF);
    			retVal[a][idx] = sample;
    		}
    		idx++;
    	}
    	
    	return retVal;
    }
    
    /**
     * Convert frames to samples. (PCM_SIGNED 16-bit)
     */
    protected int[][] frameTo16bitSampleArray(byte[] frameContainer) {
    	int numberOfSamples = frameContainer.length / (2 * getNumberOfChannels());
    	int[][] retVal = new int[getNumberOfChannels()][numberOfSamples];
    	
    	int idx = 0;
    	for(int t = 0; t < frameContainer.length;) {
    		for(int a = 0; a < getNumberOfChannels(); a++) {
    			int low = frameContainer[t];
    			t++;
    			int high = frameContainer[t];
    			t++;
    			
    			int sample = (high << 8) + (low & 0x00ff);
    			retVal[a][idx] = sample;
    		}
    		idx++;
    	}
    	
    	return retVal;
    }
    
    /**
     * Convert frames to samples. (PCM_SIGNED 24-bit)
     */
    protected int[][] frameTo24bitSampleArray(byte[] frameContainer) {
    	int numberOfSamples = frameContainer.length / (3 * getNumberOfChannels());
    	int[][] retVal = new int[getNumberOfChannels()][numberOfSamples];
    	
    	int idx = 0;
    	for(int t = 0; t < frameContainer.length;) {
    		for(int a = 0; a < getNumberOfChannels(); a++) {
	    		int low = frameContainer[t];
	    		t++;
	    		int mid = frameContainer[t];
	    		t++;
	    		int high = frameContainer[t];
	    		t++;
	    		
	    		int sample = (high << 16) + (mid << 8) + (low & 0x00ff);
	    		retVal[a][idx] = sample;
    		}
    		idx++;
    	}
    	
    	return retVal;
    }
    
    /**
     * Convert frames to samples. (PCM_SIGNED 32-bit)
     */
    protected int[][] frameTo32bitSampleArray(byte[] frameContainer) {
    	int numberOfSamples = frameContainer.length / (4 * getNumberOfChannels());
    	int[][] retVal = new int[getNumberOfChannels()][numberOfSamples];
    	
    	int idx = 0;
    	for(int t = 0; t < frameContainer.length;) {
    		for(int a = 0; a < getNumberOfChannels(); a++) {
	    		int low = frameContainer[t];
	    		t++;
	    		int lowmid = frameContainer[t];
	    		t++;
	    		int midhigh = frameContainer[t];
	    		t++;
	    		int high = frameContainer[t];
	    		t++;
	    		
	    		int sample = ((high << 24) & 0xFF000000) + 
	    						((midhigh << 16) & 0x00FF0000) + 
	    						((lowmid << 8) & 0x0000FF00) + 
	    						(low & 0x000000FF);
	    		retVal[a][idx] = sample;
    		}
    		idx++;
    	}
    	
    	return retVal;
    }
    
    public WavHelper getSegment(double startMs, double lengthMs) {
    	AudioInputStream audioInputStream = getAudioInputStream();
    	if(audioInputStream == null) return null;
		
    	float frameRate = audioInputStream.getFormat().getFrameRate();
    	long segOffset = Math.round(frameRate * ((float)startMs/1000.0f)) * audioInputStream.getFormat().getFrameSize();
    	long segFrames = Math.round(frameRate * ((float)lengthMs/1000.0f));
    	long segLen = segFrames * audioInputStream.getFormat().getFrameSize();
    	
    	WavHelper retVal = null;
//    	synchronized(audioInputStream) {
    		// reset audio position to 0
    		try {
//				audioInputStream.reset();
				
				byte[] bc = new byte[(int)segLen];
				
				audioInputStream.skip(segOffset);
				int bytesRead = audioInputStream.read(bc, 0, (int)segLen);
				
				if(bytesRead != segLen) {
					LOGGER.warning("Failed to read bytes\n  Requested: " + segLen + ", Got: " + bytesRead);
				}
				
//				ByteArrayInputStream bin = new
//					ByteArrayInputStream(bc);
				
				retVal = new WavHelper(bc, audioInputStream.getFormat(), segLen / audioInputStream.getFormat().getFrameSize());
//				retVal = new WavHelper(ais);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
//    	}
    	return retVal;
    }
    
    public void playStream()
    	throws PhonMediaException {
    	playStream(null);
    }
    
    public void playStream(LineListener listener) 
    	throws PhonMediaException {
    	if(wavData == null) return;
    	
//    	AudioInputStream stream = getAudioInputStream();
    	
    	
    	Clip audioClip;
		try {
			audioClip = AudioSystem.getClip();
			
		} catch (LineUnavailableException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new PhonMediaException(e.toString());
		}
    	try {
			audioClip.open(format, wavData, 0, wavData.length);
		} catch (LineUnavailableException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new PhonMediaException(e.toString());
		}
    	
		if(listener != null) {
			audioClip.addLineListener(listener);
		}
		audioClip.start();
//		System.out.println("playing");
//		AudioFormat format = stream.getFormat();
//		SourceDataLine line = null;
//		DataLine.Info	info = new DataLine.Info(SourceDataLine.class,
//				 format);
//		try
//		{
//			line = (SourceDataLine) AudioSystem.getLine(info);
//			
//			/*
//			The line is there, but it is not yet ready to
//			receive audio data. We have to open the line.
//			*/
//			line.open(format);
//		}
//		catch (LineUnavailableException e)
//		{
//			e.printStackTrace();
//			System.exit(1);
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//			System.exit(1);
//		}
//		
//		if(listener != null) {
//			line.addLineListener(listener);
//		}
////		line.addLineListener(new AudioLineListener());
//		
//		/*
//		Still not enough. The line now can receive data,
//		but will not pass them on to the audio output device
//		(which means to your sound card). This has to be
//		activated.
//		*/
//		line.start();
//		
//		int framesPerPass = 32;
//		int	nBytesRead = 0;
//		byte[]	abData = new byte[framesPerPass*stream.getFormat().getFrameSize()];
//		while (nBytesRead != -1)
//		{
//			try
//			{
//				nBytesRead = stream.read(abData, 0, abData.length);
//			}
//			catch (IOException e)
//			{
//				e.printStackTrace();
//			}
//			if (nBytesRead > 0)
//			{
//				line.write(abData, 0, nBytesRead);
//			}
//		}
//		
//		if(line.available() > 0) {
//			PhonLogger.info("Draining audio");
//			line.drain();
//		}
//		
//		PhonLogger.info("Closing audio line.");
//		line.stop();
//		line.close();
	}
    
    public double getYScaleFactor(int panelHeight){
    	double biggestSample = 0.0;
    	if (sampleMax > sampleMin) {
            biggestSample = sampleMax;
        } else {
            biggestSample = Math.abs(((double) sampleMin));
        }
    	
        return (panelHeight / (biggestSample * 2 * 1.2));
    }
    
    public double timeForFile() {
    	AudioInputStream audioInputStream = getAudioInputStream();
    	if(audioInputStream == null) return 0.0;
    	return (audioInputStream.getFrameLength() / audioInputStream.getFormat().getFrameRate() * 1000.0);
    }
    
//    /**
//     * Load a segment of audio into memory and return the input stream.
//     * 
//     * @param startMs
//     * @param lengthMs
//     * @return
//     */
//    public AudioInputStream getSegment(double startMs, double lengthMs) {
//    	
//    	// convert the time values to sample indicies
//    	int startSample = sampleForTime(startMs);
//    	int endSample = sampleForTime(startMs + lengthMs);
//    	
//    	audioInputStream.rea
//    	
//    	long segLength = endSample - startSample;
//    	
//    	byte data[] = new byte[(int)(getNumberOfChannels() * (2 * segLength))];
//    	int index = 0;
//    	for(long i = startSample; i < endSample; i++) {
//    		for(int c = 0; c < getNumberOfChannels(); c++) {
//    			int sample = samplesContainer[c][(int)i];
//    			byte low = (byte)(sample & 0x00ff);
//    			data[index++] = low;
//    			byte high = (byte)((sample & 0xff00) >> 8);
//    			data[index++] = high;
//    		}
//    	}
//    	
//    	ByteArrayInputStream bin = new ByteArrayInputStream(data);
//    	
//    	AudioInputStream retVal = 
//    		new AudioInputStream(bin, audioInputStream.getFormat(), segLength);
//    	return retVal;
//    }
    
//    public long numberOfSamples() {
//    	long numberOfSamples = (audioInputStream.getFrameLength() * audioInputStream.getFormat().getFrameSize())
//			/ (2 * getNumberOfChannels());
//    	return numberOfSamples;
//    }
//    
//    public double samplesPerMs() {
//    	long numberOfSamples = numberOfSamples();
//    	double time = timeForFile();
//    	return (double)(numberOfSamples / time);
//    }
    
//    public double timeForFile() {
//    	return (audioInputStream.getFrameLength() / audioInputStream.getFormat().getFrameRate() * 1000.0);
//    }
//
//    public double timeForSample(int sampleIndex) {
//    	double time = sampleIndex * samplesPerMs();
////    	double timeMS = timeS * 1000.0;
//    	return time;
//    }
//    
//    public int sampleForTime(double ms) {
//    	double sample = 
//    		ms * samplesPerMs();
//    	return (int)Math.min(numberOfSamples(), Math.round(sample));
//    }
//
    public int getNumberOfChannels(){
        int retVal = numberOfChannels;
        
        if(retVal < 0) {
	        AudioInputStream ais = getAudioInputStream();
	        if(ais != null) {
	        	retVal = ais.getFormat().getChannels();
	        	numberOfChannels = retVal;
	        }
        }
        
        if(retVal < 0) 
        	retVal = 0;
        
        return retVal;
    }
    
    /**
     * Get the audio samples for this helper.
     * Warning: the method is NOT suitable for
     * large files.
     * 
     * @return the audio samples
     */
    public int[][] getAudioSamples() {
    	int[][] retVal = new int[0][];
    	if(wavData == null) {
    		// read from file
    	} else {
    		retVal = frameToSampleArray(wavData);
    	}
    	return retVal;
    }
    
    /**
     * Write the loaded wavData to a new file.
     * 
     */
    public void saveToFile(String file)
    	throws IOException {
    	saveToFile(new File(file));
    }
    
    public void saveToFile(File f)
    	throws IOException {
    	// create an audio output stream with
    	// the correct format
    	try {
	    	AudioInputStream stream = getAudioInputStream();
	    	AudioSystem.write(stream, Type.WAVE, f);
    	} catch (IllegalArgumentException e) {
    		LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
    	}
    }

}
