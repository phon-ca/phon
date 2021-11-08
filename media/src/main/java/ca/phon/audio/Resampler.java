package ca.phon.audio;

import com.ibm.icu.util.Output;

import java.io.*;
import java.util.stream.IntStream;

/**
 * Resample given sampled.
 *
 */
public final class Resampler {

	// samples, divided by channel
	private int[][] samples;

	public Sampled resample(Sampled sampled, float newSampleRate) {
		return resample(sampled, newSampleRate, 10);
	}

	/**
	 * Resample given sampled with provided sample rate and quality.
	 *
	 * @param sampled
	 * @param newSampleRate
	 * @param windowSize Hann window size in samples
	 * @return Sampled a new sampled with given sample rate
	 * @throws IOException
	 */
	public Sampled resample(Sampled sampled, float newSampleRate, int windowSize) {
		int srcLength = sampled.getNumberOfSamples();
		int destLength = (int)Math.ceil((srcLength * newSampleRate) / sampled.getSampleRate());
		float dx = (float)srcLength / (float)destLength;

		float fmaxDivSR = 0.5f;
		float r_g = fmaxDivSR  * 2;

		int wndWidth2 = windowSize;
		int wndWidth = windowSize * 2;

		double[][] samples = new double[sampled.getNumberOfChannels()][];
		for(int i = 0; i < sampled.getNumberOfChannels(); i++)
			samples[i] = new double[destLength];

		float x = sampled.sampleForTime(sampled.getStartTime());
		int tau = 0;
		double r_y = 0.0f;
		double r_w = 0.0f;
		double r_a = 0.0f;
		double r_snc = 0.0f;

		for(int channel = 0; channel < sampled.getNumberOfChannels(); channel++) {
			for (int i = 0; i < destLength; ++i) {
				r_y = 0.0f;
				for (tau = -wndWidth2; tau < wndWidth2; ++tau) {
					int j = (int) (x + tau);

					// hann window
					r_w = 0.5 - 0.5 * Math.cos(2 * Math.PI * (0.5 + (j - x) / wndWidth));
					r_a = 2 * Math.PI * (j - x) * fmaxDivSR;
					r_snc = 1.0;
					if (r_a != 0)
						r_snc = Math.sin(r_a) / r_a;

					if ((j >= 0) && (j < srcLength)) {
						r_y += r_g * r_w * r_snc * sampled.valueForSample(channel, j);
					}
				}
				samples[channel][i] = r_y;
				x += dx;
			}
		}

		return new ArraySampled(newSampleRate, samples);
	}

}
