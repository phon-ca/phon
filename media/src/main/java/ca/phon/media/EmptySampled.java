package ca.phon.media;

import ca.phon.audio.Sampled;

import java.io.IOException;
import java.util.Arrays;

/**
 * Empty audio samples of specified length and format.
 *
 */
public class EmptySampled implements Sampled {

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
	public double maximumValue(int channel, int firstSample, int lastSample) {
		return 0.0;
	}

	@Override
	public double maximumValue(int channel, float startTime, float endTime) {
		return 0.0;
	}

	@Override
	public double minimumValue(int channel, int firstSample, int lastSample) {
		return 0.0;
	}

	@Override
	public int loadSampleData(double[][] buffer, int offset, int firstSample, int numSamples) {
		for(int i = 0;  i < buffer.length; i++)
			Arrays.fill(buffer[i], 0.0);
		return numSamples;
	}

	@Override
	public double[][] getWindowExtrema(int firstSample, int lastSample) {
		return new double[0][];
	}

	@Override
	public double[][] getWindowExtrema(float startTime, float endTime) {
		return new double[0][];
	}

	@Override
	public double[] getWindowExtrema(int channel, int firstSample, int lastSample) {
		return new double[0];
	}

	@Override
	public void getWindowExtrema(int channel, int firstSample, int lastSample, double[] extrema) {
		Arrays.fill(extrema, 0.0);
	}

	@Override
	public double[] getWindowExtrema(int channel, float startTime, float endTime) {
		return new double[0];
	}

	@Override
	public void getWindowExtrema(int channel, float startTime, float endTime, double[] extrema) {
		Arrays.fill(extrema, 0.0);
	}

}