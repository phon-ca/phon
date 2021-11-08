package ca.phon.audio;

import java.io.IOException;

/**
 * A wrapper around a multi-channel sampled which returns
 * samples as an average of all channels.
 */
public class MonoSampled extends AbstractSampled {

	private Sampled sampled;

	public MonoSampled(Sampled sampled) {
		super();

		this.sampled = sampled;
	}


	@Override
	public int getNumberOfChannels() {
		return 1;
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
		if(channel > 0) throw new IllegalArgumentException("Mono sampled only has one channel");
		double sampleValue = 0.0;
		for(int ch = 0; ch < sampled.getNumberOfChannels(); ch++) {
			sampleValue += sampled.valueForSample(ch, sample);
		}
		return sampleValue / sampled.getNumberOfChannels();
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
	public void close() throws IOException {
	}

	@Override
	public int loadSampleData(double[][] buffer, int offset, int firstSample, int numSamples) {
		int idx = 0;
		for(int sample = firstSample; sample < firstSample + numSamples && sample < getNumberOfSamples(); sample++) {
			buffer[0][offset+idx] = valueForSample(0, sample);
			++idx;
		}
		return idx;
	}

}
