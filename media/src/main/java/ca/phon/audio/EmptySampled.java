package ca.phon.audio;

import java.io.IOException;
import java.util.Arrays;

/**
 * Empty audio samples of specified length and format.
 *
 */
public final class EmptySampled extends AbstractSampled {

	private float length;

	private int numChannels;

	private float sampleRate;

	public EmptySampled(int numChannels, float sampleRate, float length) {
		this.numChannels = numChannels;
		this.sampleRate = sampleRate;
		this.length = length;
	}

	@Override
	public int getNumberOfChannels() {
		return this.numChannels;
	}

	@Override
	public float getSampleRate() {
		return this.sampleRate;
	}

	@Override
	public int getNumberOfSamples() {
		return (int)Math.ceil(getSampleRate() * getLength());
	}

	@Override
	public double valueForSample(int channel, int sample) {
		return 0.0;
	}

	@Override
	public int sampleForTime(float time) {
		final double frameRate = getSampleRate();
		final int sampleIdx = (int)Math.round(frameRate * time);
		return sampleIdx;
	}

	@Override
	public double valueForTime(int channel, float time) {
		return 0.0;
	}

	@Override
	public float getStartTime() {
		return 0.0f;
	}

	@Override
	public float getEndTime() {
		return this.length;
	}

	@Override
	public float getLength() {
		return this.length;
	}

	public void close() throws IOException {}

	@Override
	public int loadSampleData(double[][] buffer, int offset, int firstSample, int numSamples) {
		for(int i = 0;  i < buffer.length; i++)
			Arrays.fill(buffer[i], 0.0);
		return numSamples;
	}

}