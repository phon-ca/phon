package ca.phon.media.sampled;

import java.io.IOException;

import ca.phon.audio.AudioFile;

public class AudioFileSampled implements Sampled {

	private final AudioFile audioFile;
	
	private float startTime = 0.0f;
	
	private float endTime = 0.0f;
	
	public AudioFileSampled(AudioFile audioFile) {
		this.audioFile = audioFile;
		this.startTime = 0.0f;
		this.endTime = audioFile.getLength();
	}
	
	public AudioFileSampled(AudioFile audioFile, float startTime, float endTime) {
		this.audioFile = audioFile;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	@Override
	public int getNumberOfChannels() {
		return audioFile.getNumberOfChannels();
	}

	@Override
	public long getNumberOfSamples() {
		return audioFile.getNumberOfSamples();
	}

	@Override
	public float getSampleRate() {
		return (float)audioFile.getSampleRate();
	}

	@Override
	public double valueForSample(int channel, long sample) {
		double[][] buffer = new double[getNumberOfChannels()][];
		for(int i = 0; i < getNumberOfChannels(); i++) {
			buffer[i] = new double[1];
		}
		try {
			audioFile.seekToSample(sample);
			audioFile.readSamples(buffer, 0, 1);
			
			return buffer[channel][0];
		} catch (IOException e) {
			return 0.0;
		}
	}

	@Override
	public long sampleForTime(float time) {
		return audioFile.sampleIndexForTime(time);
	}

	@Override
	public double valueForTime(int channel, float time) {
		return valueForSample(channel, sampleForTime(time));
	}

	@Override
	public float getStartTime() {
		return startTime;
	}

	@Override
	public float getEndTime() {
		return endTime;
	}

	@Override
	public float getLength() {
		return getEndTime() - getStartTime();
	}

	@Override
	public double maximumValue(int channel, long firstSample, long lastSample) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double maximumValue(int channel, float startTime, float endTime) {
		double[] extrema = getWindowExtrema(channel, startTime, endTime);
		return extrema[1];
	}

	@Override
	public double minimumValue(int channel, long firstSample, long lastSample) {
		double[] extrema = getWindowExtrema(channel, startTime, endTime);
		return extrema[0];
	}

	@Override
	public double[] getWindowExtrema(int channel, long firstSample, long lastSample) {
		double[] retVal = new double[2];
		getWindowExtrema(channel, firstSample, lastSample, retVal);
		return retVal;
	}

	@Override
	public void getWindowExtrema(int channel, long firstSample, long lastSample, double[] extrema) {
		int numSamples = (int)(lastSample - firstSample);
		if(numSamples < 0) throw new ArrayIndexOutOfBoundsException();
		
		double[][] data = new double[getNumberOfChannels()][];
		for(int i = 0; i < getNumberOfChannels(); i++) {
			data[i] = new double[numSamples];
		}
		
		try {
			audioFile.seekToSample(firstSample);
			audioFile.readSamples(data, 0, numSamples);
			
			for(int i = 0; i < numSamples; i++) {
				extrema[0] = Math.min(extrema[0], data[channel][i]);
				extrema[1] = Math.max(extrema[1], data[channel][i]);
			}
		} catch (IOException e) {
		}
	}

	public double[][] getWindowExtrema(float firstTime, float endTime) {
		return getWindowExtrema(sampleForTime(firstTime), sampleForTime(endTime));
	}
	
	@Override
	public double[][] getWindowExtrema(long firstSample, long lastSample) {
		int numSamples = (int)(lastSample - firstSample);
		double[][] retVal = new double[getNumberOfChannels()][];
		for(int i = 0; i < getNumberOfChannels(); i++) {
			retVal[i] = new double[2];
		}
		
		double[][] data = new double[getNumberOfChannels()][];
		for(int i = 0; i < getNumberOfChannels(); i++) {
			data[i] = new double[numSamples];
		}
		
		try {
			audioFile.seekToSample(firstSample);
			audioFile.readSamples(data, 0, numSamples);
			
			for(int isamp = 0; isamp < numSamples; isamp++) {
				for(int ichan = 0; ichan < getNumberOfChannels(); ichan++) {
					retVal[ichan][0] = Math.min(retVal[ichan][0], data[ichan][isamp]);
					retVal[ichan][1] = Math.max(retVal[ichan][1], data[ichan][isamp]);
				}
			}
		} catch (IOException e) {
		}
		
		return retVal;
	}
	
	@Override
	public double[] getWindowExtrema(int channel, float startTime, float endTime) {
		return getWindowExtrema(channel, sampleForTime(startTime), sampleForTime(endTime));
	}

	@Override
	public void getWindowExtrema(int channel, float startTime, float endTime, double[] extrema) {
		getWindowExtrema(channel, sampleForTime(startTime), sampleForTime(endTime), extrema);
	}

}
