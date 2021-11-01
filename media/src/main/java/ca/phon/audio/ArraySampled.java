package ca.phon.audio;

import java.io.IOException;

public final class ArraySampled extends AbstractSampled {

	private double[][] samples;

	private float sampleRate;

	/**
	 * Create a new sampled from supplied data and sample rate
	 *
	 * @param sampleRate
	 * @param samples
	 */
	public ArraySampled(float sampleRate, double[][] samples) {
		this.sampleRate = sampleRate;
		this.samples = samples;
	}

	@Override
	public int getNumberOfChannels() {
		return samples.length;
	}

	@Override
	public int getNumberOfSamples() {
		return (samples.length > 0 ? samples[0].length : 0);
	}

	@Override
	public float getSampleRate() {
		return this.sampleRate;
	}

	@Override
	public double valueForSample(int channel, int sample) {
		return samples[channel][sample];
	}

	@Override
	public int sampleForTime(float time) {
		final double frameRate = getSampleRate();
		final int sampleIdx = (int)Math.round(frameRate * time);
		return sampleIdx;
	}

	@Override
	public double valueForTime(int channel, float time) {
		return valueForSample(channel, sampleForTime(time));
	}

	@Override
	public float getStartTime() {
		return 0;
	}

	@Override
	public float getEndTime() {
		return (float)(getNumberOfSamples() / getSampleRate());
	}

	@Override
	public float getLength() {
		return getEndTime() - getStartTime();
	}

	@Override
	public void close() throws IOException {

	}

	@Override
	public int loadSampleData(double[][] buffer, int offset, int firstSample, int numSamples) {
		for(int i = firstSample; i < firstSample + numSamples; i++) {
			for(int channel = 0; channel < getNumberOfChannels(); channel++) {
				buffer[channel][i-firstSample + offset] = samples[channel][i];
			}
		}
		return numSamples;
	}

}
