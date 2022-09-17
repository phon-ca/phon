package ca.phon.audio;

import java.io.IOException;

/**
 * Wrapper around a sampled object allowing access to a filtered list of channels
 *
 */
public class ChannelFilteredSampled extends AbstractSampled {

	private int[] channels;

	private final Sampled sampled;

	public ChannelFilteredSampled(Sampled sampled, int[] channels) {
		super();

		this.sampled = sampled;
		this.channels = channels;
	}

	public int[] getChannels() {
		return this.channels;
	}

	public void setChannels(int[] channels) {
		this.channels = channels;
	}

	public Sampled getSampled() {
		return this.sampled;
	}

	@Override
	public int getNumberOfChannels() {
		return this.channels.length;
	}

	@Override
	public int getNumberOfSamples() {
		return sampled.getNumberOfSamples();
	}

	@Override
	public float getSampleRate() {
		return sampled.getSampleRate();
	}

	@Override
	public double valueForSample(int channel, int sample) {
		if(channel < 0 || channel >= getNumberOfChannels()) throw new IndexOutOfBoundsException("channel");
		return sampled.valueForSample(this.channels[channel], sample);
	}

	@Override
	public int sampleForTime(float time) {
		return sampled.sampleForTime(time);
	}

	@Override
	public double valueForTime(int channel, float time) {
		return valueForSample(channel, sampleForTime(time));
	}

	@Override
	public float getStartTime() {
		return sampled.getStartTime();
	}

	@Override
	public float getEndTime() {
		return sampled.getEndTime();
	}

	@Override
	public float getLength() {
		return sampled.getLength();
	}

	@Override
	public void close() throws IOException {	}

	@Override
	public int loadSampleData(double[][] buffer, int offset, int firstSample, int numSamples) {
		int idx = 0;
		for(int sample = firstSample; sample < firstSample + numSamples && sample < getNumberOfSamples(); sample++) {
			for(int channel = 0; channel < getNumberOfChannels(); channel++) {
				buffer[channel][offset + idx] = valueForSample(channel, sample);
				++idx;
			}
		}
		return idx;
	}

}
